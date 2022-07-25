package mutnemom.android.kotlindemo.reader.pdf

/* An interface for rendering or parsing, which can be stopped and started */
interface Watchable {

    companion object {
        /* the possible statuses  */
        const val UNKNOWN = 0
        const val NOT_STARTED = 1
        const val PAUSED = 2
        const val NEEDS_DATA = 3
        const val RUNNING = 4
        const val STOPPED = 5
        const val COMPLETED = 6
        const val ERROR = 7
    }

    /* Get the status of this watchable
     *
     * @return one of the well-known statuses
     */
    fun getStatus(): Int

    /* Stop this watchable.
     * Stop will cause all processing to cease,
     * and the watchable to be destroyed.
     */
    fun stop()

    /* Start this watchable and run until it is finished or stopped.
     * Note the watchable may be stopped if go() with a
     * different time is called during execution.
     */
    fun go()

    /* Start this watchable and run for the given number of steps or until
     * finished or stopped.
     *
     * @param steps the number of steps to run for
     */
    fun go(steps: Int)

    /* Start this watchable and run for the given amount of time,
     * or until finished or stopped.
     *
     * @param millis the number of milliseconds to run for
     */
    fun go(millis: Long)

}
