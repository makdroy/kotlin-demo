package mutnemom.android.kotlindemo.reader.pdf

import android.util.Log
import java.util.concurrent.locks.ReentrantLock
import kotlin.concurrent.withLock

/**
 * An abstract implementation of the watchable interface, that is extended
 * by the parser and renderer to do their thing.
 */
abstract class BaseWatchable(
    private var status: Int = Watchable.UNKNOWN
) : Watchable, Runnable {

    private val logTag: String
        get() = this::class.java.simpleName

    /* a lock for status-related operations  */
    private val statusLock = ReentrantLock()
    private val statusLockCondition = statusLock.newCondition()

    /* a lock for parsing operations  */
    private val parserLock = ReentrantLock()

    /* when to stop  */
    private var gate: Gate? = null

    /* the thread we are running in  */
    private var thread: Thread? = null

    private val isExecutable: Boolean
        get() = (isPaused || isRunning) && (gate == null || !gate!!.stop())

    private val isRunning: Boolean
        get() = status == Watchable.RUNNING

    private val isPaused: Boolean
        get() = status == Watchable.PAUSED

    val isFinished: Boolean
        get() = status == Watchable.COMPLETED || status == Watchable.ERROR

    init {
        setStatus(Watchable.NOT_STARTED)
    }

    /* Perform a single iteration of this watchable.
     * This is the minimum granularity which the go() commands operate over.
     *
     * @return one of three values: <ul>
     *         <li> Watchable.RUNNING if there is still data to be processed
     *         <li> Watchable.NEEDS_DATA if there is no data to be processed but
     *              the execution is not yet complete
     *         <li> Watchable.COMPLETED if the execution is complete
     *  </ul>
     */
    @Throws(Exception::class)
    protected abstract fun iterate(): Int

    /**
     * Prepare for a set of iterations.  Called before the first iterate() call
     * in a sequence.  Subclasses should extend this method if they need to do
     * anything to setup.
     */
    protected open fun setup() {
        // do nothing
    }

    /**
     * Clean up after a set of iterations. Called after iteration has stopped
     * due to completion, manual stopping, or error.
     */
    protected open fun cleanup() {
        // do nothing
    }

    override fun run() {
        Log.d(logTag, "run() called")

        if (status == Watchable.NOT_STARTED) {
            setup()
        }

        setStatus(Watchable.PAUSED)
        parserLock.withLock {
            while (!isFinished && (status != Watchable.STOPPED)) {
                if (isExecutable) {
                    // set the status to running
                    setStatus(Watchable.RUNNING)

                    try {
                        // keep going until the status is no longer running,
                        // our gate tells us to stop, or no-one is watching
                        while (isRunning && (gate == null || !gate!!.iterate())) {
                            // update the status based on this iteration
                            setStatus(iterate())
                        }

                        if (isRunning) {
                            // make sure we are paused
                            setStatus(Watchable.PAUSED)
                        }
                    } catch (ex: java.lang.Exception) {
                        setError(ex)
                    }
                } else {
                    // wait for our status to change
                    statusLock.withLock {
                        if (!isExecutable) {
                            try {
                                statusLockCondition.await()
                            } catch (e: InterruptedException) {
                                e.printStackTrace()
                            }
                        }
                    }
                }
            }
        }

        // call cleanup when we are done
        if (isFinished) {
            cleanup()
        }

        // notify that we are no longer running
        thread = null
    }

    /* Get the status of this watchable
     *
     * @return one of the well-known statuses
     */
    override fun getStatus(): Int = status

    /* Stop this watchable.
     * Stop will cause all processing to cease,
     * and the watchable to be destroyed.
     */
    override fun stop() {
        setStatus(Watchable.STOPPED)
    }

    override fun go() {
        gate = null
        execute(false)
    }

    /* Start this watchable and run until it is finished or stopped.
     * Note the watchable may be stopped if go() with a
     * different time is called during execution.
     *
     * @param synchronous if true, run in this thread
     */
    @Synchronized
    fun go(synchronous: Boolean) {
        Log.d(logTag, "go() called with: synchronous = $synchronous")

        gate = null
        execute(synchronous)
    }

    /* Start this watchable and run for the given number of steps or until
     * finished or stopped.
     *
     * @param steps the number of steps to run for
     */
    @Synchronized
    override fun go(steps: Int) {
        gate = Gate()
        gate!!.setStopIterations(steps)
        execute(false)
    }

    /* Start this watchable and run for the given amount of time, or until
     * finished or stopped.
     *
     * @param millis the number of milliseconds to run for
     */
    @Synchronized
    override fun go(millis: Long) {
        gate = Gate()
        gate!!.setStopTime(millis)
        execute(false)
    }

    /* Start executing this watchable
     *
     * @param synchronous if true, run in this thread
     */
    @Synchronized
    fun execute(synchronous: Boolean) {
        Log.d(logTag, "execute() called with: synchronous = $synchronous")

        // see if we're already running
        if (thread != null) {
            // we're already running. Make sure we wake up on any change.
            statusLock.withLock {
                statusLockCondition.signalAll()
            }

            return
        } else if (isFinished) {
            // we're all finished
            return
        }

        // we're turn not running. Start up
        if (synchronous) {
            thread = Thread.currentThread()
            run()
        } else {
            thread = Thread(this)
            thread!!.name = javaClass.name
            thread!!.start()
        }
    }

    private fun setError(error: Exception) {
//        if (!BaseWatchable.SuppressSetErrorStackTrace) {
//            error.printStackTrace()
//        }

        error.printStackTrace()
        setStatus(Watchable.ERROR)
    }

    /* Set the status of this watchable */
    fun setStatus(status: Int) {
        statusLock.withLock {
            this.status = status
            statusLockCondition.signalAll()
        }
    }

    /* A class that lets us give it a target time or number of steps,
     * and will tell us to stop after that much time or that many steps
     */
    internal class Gate {
        /* whether this is a time-based (true) or step-based (false) gate  */
        private var timeBased = false

        /* the next gate, whether time or iterations  */
        private var nextGate: Long = 0

        /* set the stop time  */
        fun setStopTime(millisFromNow: Long) {
            timeBased = true
            nextGate = System.currentTimeMillis() + millisFromNow
        }

        /* set the number of iterations until we stop  */
        fun setStopIterations(iterations: Int) {
            timeBased = false
            nextGate = iterations.toLong()
        }

        /* check whether we should stop. */
        fun stop(): Boolean {
            return if (timeBased) {
                System.currentTimeMillis() >= nextGate
            } else {
                nextGate < 0
            }
        }

        /* Notify the gate of one iteration.
         * Returns true if we should stop or false if not
         */
        fun iterate(): Boolean {
            if (!timeBased) {
                nextGate--
            }
            return stop()
        }
    }

}
