package mutnemom.android.kotlindemo.reader.pdf

import android.graphics.*
import android.graphics.Paint.Cap
import android.graphics.Paint.Join
import android.util.Log
import java.lang.ref.WeakReference
import java.util.*

class PdfRendererSync(
    private var page: PdfPage?,
    private val imageInfo: ImageInfo,
    private val bitmap: Bitmap
) : BaseWatchable() {

    companion object {
        /* how long (in milliseconds) to wait between image updates  */
        const val UPDATE_DURATION: Long = 200
        const val NOPHASE = -1000f
        const val NOWIDTH = -1000f
        const val NOLIMIT = -1000f
        val NOCAP: Cap? = null
        val NODASH: FloatArray? = null
        val NOJOIN: Join? = null
    }

    private val logTag: String
        get() = this::class.java.simpleName

    private var imageRef: WeakReference<BiCa>? = null
    private var commandCount = 0
    private var graphic: Canvas? = null

    /* the current graphics state  */
    private var state: GraphicsState? = null

    /* the stack of push()ed graphics states  */
    private var stack: Stack<GraphicsState>? = null

    /* the last shape we drew (to check for overlaps)  */
    var lastShape: Path? = null

    /* the total region of this image that has been written to  */
    private var globalDirtyRegion: RectF? = null

    /* where we are in the page's command list  */
    private var currentCommand = 0

    init {
        graphic = Canvas(bitmap)
        imageRef = WeakReference(BiCa(bitmap, graphic!!))
        commandCount = 0
    }

    /* Setup rendering.  Called before iteration begins */
    override fun setup() {
        Log.d(logTag, "setup() called")

        val canvas = imageRef?.get()?.canvas ?: graphic
        canvas?.let { setupRendering(it) }
    }

    override fun iterate(): Int {
        Log.d(logTag, "iterate() called")

        // make sure we have a page to render
        page?.also { page ->

            // check if this renderer is based on a weak reference to a graphics object.
            // If it is, and the graphics is no longer valid, then just quit
            imageRef?.get()
                ?.also { graphic = it.canvas }
                ?: run {
                    Log.i(logTag, "Image went away.  Stopping")
                    return Watchable.STOPPED
                }

            // check if there are any commands to parse.
            // If there aren't, just return,
            // but check if we're return really finished or not
            if (currentCommand >= page.commandCount) {
                return if (page.finished) Watchable.COMPLETED else Watchable.NEEDS_DATA
            }

            // find the current command
            val cmd: PdfCmd = page.getCommand(currentCommand++)
                ?: throw IllegalStateException("Command not found!")

            if (!PdfParser.RELEASE) {
                commandCount++
                Log.i(logTag, "CMD[$commandCount]: $cmd")
            }


        } ?: return Watchable.COMPLETED


//        var dirtyRegion: RectF? = null
//        // execute the command
//        try {
//            dirtyRegion = cmd.execute(this)
//        } catch (e: Exception) {
//            e.printStackTrace()
//        }
//
//        // append to the global dirty region
//        globalDirtyRegion = addDirtyRegion(dirtyRegion, globalDirtyRegion)
//        unupdatedRegion = addDirtyRegion(dirtyRegion, unupdatedRegion)
//
//        val now = System.currentTimeMillis()
//        if (now > then || rendererFinished()) {
//            // now tell any observers, so they can repaint
////            notifyObservers(bi, unupdatedRegion);
//            unupdatedRegion = null
//            then = now + com.sun.pdfview.PDFRenderer.UPDATE_DURATION
//        }
//
//        // if we are based on a reference to a graphics, don't hold on to it
//        // since that will prevent the image from being collected.
//        if (imageRef != null) {
//            g = null
//        }

        // if we need to stop, it will be caught at the start of the next
        // iteration.
        return Watchable.RUNNING
    }

    /* Called when iteration has stopped */
    override fun cleanup() {
        page = null
        state = null
        stack = null
        globalDirtyRegion = null
        lastShape = null
    }

    fun push() {
        state?.cliprgn = graphic?.clipBounds
        stack?.push(state)
        state = state!!.clone() as GraphicsState
    }

    fun pop() {
        state = stack!!.pop() as GraphicsState
        state!!.xform?.also { setTransform(it) }
        state!!.cliprgn?.also { setClip(it) }
    }

    fun fill(path: Path): RectF? = graphic?.let {
        state?.fillPaint?.fill(this, it, path)
    }

    fun stroke(path: Path): RectF? = graphic?.let {
        state?.strokePaint?.fill(this, it, path)
    }

    fun clip(path: Path) {
        graphic?.clipPath(path)
    }

    fun drawNativeText(text: String, bounds: RectF): RectF {
        val paint: Paint = state!!.fillPaint!!.paint

        graphic?.also {
            it.save()
            val m: Matrix
            val mOrig: Matrix = it.getMatrix()

            m = Matrix(mOrig)
            m.preScale(1.0f, -1.0f, bounds.left, bounds.top)
            it.setMatrix(m)
            it.drawText(text, bounds.left, bounds.top, paint)

            it.restore()
        }

        return bounds
    }

    fun transform(matrix: Matrix) {
        state?.xform?.also {
            it.preConcat(matrix)
            graphic?.setMatrix(it)
        }
    }

    fun setFillPaint(paint: PdfPaint) {
        state?.fillPaint = paint
    }

    fun setStrokePaint(paint: PdfPaint) {
        state?.strokePaint = paint
    }

    fun setStrokeParts(
        w: Float,
        cap: Cap?,
        join: Join?,
        limit: Float,
        ary: FloatArray?,
        phase: Float
    ) {
        var w = w
        var cap = cap
        var join = join
        var limit = limit
        var ary = ary

        if (w == NOWIDTH) {
            w = state!!.lineWidth
        }

        if (cap == NOCAP) {
            cap = state!!.cap
        }

        if (join == NOJOIN) {
            join = state!!.join
        }

        if (limit == NOLIMIT) {
            limit = state!!.miterLimit
        }

        if (ary != null && ary.isEmpty()) {
            ary = null
        }

        state!!.lineWidth = w
        state!!.cap = cap
        state!!.join = join
        state!!.miterLimit = limit
    }

    private fun getInitialTransform(): Matrix? {
        return page?.getInitialTransform(
            imageInfo.width,
            imageInfo.height,
            imageInfo.clip
        )
    }

    private fun setupRendering(destGraphic: Canvas) {
        val paint = Paint()
        imageInfo.apply {
            paint.color = bgColor
            destGraphic.drawRect(0f, 0f, width.toFloat(), height.toFloat(), paint)
        }

        paint.color = Color.BLACK

        // set the initial clip and transform on the graphics
        getInitialTransform()?.also { destGraphic.setMatrix(it) }

        // set up the initial graphics state
        state = GraphicsState()
        state!!.cliprgn = null
        state!!.strokePaint = PdfPaint.getColorPaint(Color.BLACK)
        state!!.fillPaint = PdfPaint.getPaint(Color.BLACK)
        state!!.xform = destGraphic.matrix

        // initialize the stack
        stack = Stack()

        // initialize the current command
        currentCommand = 0
    }

    private fun setTransform(matrix: Matrix) {
        state?.xform = matrix
        graphic?.setMatrix(matrix)
    }

    private fun setClip(clip: Rect) {
        state?.cliprgn = clip
        graphic?.clipRect(clip, Region.Op.REPLACE)
    }

    internal class GraphicsState : Cloneable {
        var cliprgn: Rect? = null
        var cap: Cap? = null
        var join: Join? = null
        var lineWidth = 0f
        var miterLimit = 0f
        var strokePaint: PdfPaint? = null
        var fillPaint: PdfPaint? = null
        var xform: Matrix? = null

        public override fun clone(): Any {
            val cState = GraphicsState()
            cState.cliprgn = null
            cState.cap = cap
            cState.join = join
            cState.strokePaint = strokePaint
            cState.fillPaint = fillPaint
            cState.xform = Matrix(xform)
            cState.lineWidth = lineWidth
            cState.miterLimit = miterLimit
            return cState
        }
    }

}
