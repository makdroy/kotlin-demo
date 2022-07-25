package mutnemom.android.kotlindemo.reader.pdf

import android.graphics.*
import android.graphics.Paint.Cap
import android.graphics.Paint.Join
import android.util.Log
import mutnemom.android.kotlindemo.reader.pdf.commands.*
import java.lang.ref.WeakReference
import java.util.*
import java.util.concurrent.locks.ReentrantLock
import kotlin.concurrent.withLock

/**
 * A PdfPage encapsulates the parsed commands required to render a single page from a PdfFile.
 * The PdfPage is not itself drawable;
 * instead, create a PdfImage to display something on the screen.
 * <p>
 * This file also contains all of the PdfCmd commands
 * that might be a part of the command stream in a PdfPage.
 * They probably should be inner classes of PdfPage instead of separate non-public classes.
 *
 * @author Mike Wessler
 * @author Ferenc Hechler (ferenc@hechler.de)
 * @author Joerg Jahnke (joergjahnke@users.sourceforge.net)
 */
class PdfPage(
    private val pageNumber: Int,
    private var boundingBox: RectF, /* the bounding box of the page, in page coordinates  */
    private var rotation: Int,
    private val cache: PdfPageCache
) {

    val height: Float
        get() = boundingBox.height()

    val width: Float
        get() = boundingBox.width()

    private var lastRenderedCommand = 0

    /* whether this page has been finished.
     * If true, there will be no more commands added to the commands list.  */
    var finished = false

    /* a map from image info to weak references to parsers that are active  */
    private val renderers: MutableMap<ImageInfo, WeakReference<*>> =
        Collections.synchronizedMap(HashMap())

    /* the array of commands.
     * The length of this array will always
     * be greater than or equal to the actual number of commands.  */
    private val commands: MutableList<PdfCmd> = Collections.synchronizedList(ArrayList(250))
    val commandCount: Int
        get() = commands.size

    private val commandLock = ReentrantLock()

    private var parsedCommands = 0

    init {
        if (rotation < 0) {
            rotation += 360
        }

        if (rotation == 90 || rotation == 270) {
            boundingBox = RectF(
                boundingBox.left,
                boundingBox.top,
                (boundingBox.left + boundingBox.height()),
                (boundingBox.top + boundingBox.width())
            )
        }
    }

    fun getCommand(index: Int): PdfCmd? {
        lastRenderedCommand = index
        return commands.getOrNull(index)
    }

    /* Get the initial transform to map from a specified clip rectangle in
     * pdf coordinates to an image of the specified width and
     * height in device coordinates
     *
     * @param width the width of the image
     * @param height the height of the image
     * @param clip the desired clip rectangle (in PDF space) or null to use
     * the page's bounding box
     */
    fun getInitialTransform(width: Int, height: Int, clip: RectF?): Matrix {
        var destHeight = height
        var destWidth = width
        var destClip: RectF? = clip
        val mat = Matrix()

        when (rotation) {
            0 -> mat.setMatValues(1, 0, 0, -1, 0, destHeight)
            90 -> mat.setMatValues(0, 1, 1, 0, 0, 0)
            180 -> mat.setMatValues(-1, 0, 0, 1, destWidth, 0)
            270 -> mat.setMatValues(0, -1, -1, 0, destWidth, destHeight)
        }

        if (destClip == null) {
            destClip = boundingBox
        } else if (rotation == 90 || rotation == 270) {
            val tmp = width
            destWidth = height
            destHeight = tmp
        }

        // now scale the image to be the size of the clip
        val scaleX = destWidth / destClip.width()
        val scaleY = destHeight / destClip.height()
        mat.preScale(scaleX, scaleY)

        // create a transform that moves the top left corner of the clip region
        // (minX, minY) to (0,0) in the image
        mat.preTranslate(-destClip.left, -destClip.top)
        return mat
    }

    /* Add a single command to the page list. */
    fun addCommand(cmd: PdfCmd) {
        commandLock.withLock {
            commands.add(cmd)
        }

        // notify any outstanding images
        updateImages()
    }

    fun addCommands(page: PdfPage, extra: Matrix?) {
        commandLock.withLock {
            addPush()
            extra?.let { addXForm(it) }
            commands.addAll(page.commands)
            addPop()
        }

        // notify any outstanding images
        updateImages()
    }

    /* pop the graphics state  */
    fun addPop() {
        addCommand(PdfPopCmd())
    }

    /* push the graphics state  */
    fun addPush() {
        addCommand(PdfPushCmd())
    }

    fun addPath(path: Path, style: Int) {
        addCommand(PdfShapeCmd(path, style))
    }

    fun addXForm(matrix: Matrix) {
        addCommand(PdfXFormCmd(Matrix(matrix)))
    }

    /* set the stroke width
     * @param w the width of the stroke
     */
    fun addStrokeWidth(w: Float) {
        val sc = PdfChangeStrokeCmd()
        sc.w = w
        addCommand(sc)
    }

    fun addStrokePaint(paint: PdfPaint) {
        addCommand(PdfStrokePaintCmd(paint))
    }

    /* set the end cap style
     * @param capStyle the cap style:  0 = BUTT, 1 = ROUND, 2 = SQUARE
     */
    fun addEndCap(capStyle: Int) {
        val sc = PdfChangeStrokeCmd()
        var cap = Cap.BUTT
        when (capStyle) {
            0 -> cap = Cap.BUTT
            1 -> cap = Cap.ROUND
            2 -> cap = Cap.SQUARE
        }
        sc.cap = cap
        addCommand(sc)
    }

    /* set the line join style
     * @param joinStyle the join style: 0 = MITER, 1 = ROUND, 2 = BEVEL
     */
    fun addLineJoin(joinStyle: Int) {
        val sc = PdfChangeStrokeCmd()
        var join = Join.MITER
        when (joinStyle) {
            0 -> join = Join.MITER
            1 -> join = Join.ROUND
            2 -> join = Join.BEVEL
        }
        sc.join = join
        addCommand(sc)
    }

    /* set the miter limit */
    fun addMiterLimit(limit: Float) {
        val sc = PdfChangeStrokeCmd()
        sc.limit = limit
        addCommand(sc)
    }

    fun addFillPaint(paint: PdfPaint) {
        addCommand(PdfFillPaintCmd(paint))
    }

    fun addFillAlpha(alpha: Float) {
        addCommand(PdfFillAlphaCmd(alpha))
    }

    fun addStrokeAlpha(alpha: Float) {
        addCommand(PdfStrokeAlphaCmd(alpha))
    }

    fun getImage(zoom: Float, clip: RectF?, drawBG: Boolean, wait: Boolean): Bitmap? {
        Log.d(
            this::class.java.simpleName,
            "getImage() called with: zoom = $zoom, clip = $clip, drawBG = $drawBG, wait = $wait"
        )

        val zoomedHeight = (height * zoom).toInt()
        val zoomedWidth = (width * zoom).toInt()

        val info = ImageInfo(zoomedWidth, zoomedHeight, clip, Color.WHITE)
        if (drawBG) {
            info.bgColor = Color.WHITE
        }

        val image = Bitmap.createBitmap(zoomedWidth, zoomedHeight, Bitmap.Config.RGB_565)
        val renderer = PdfRendererSync(this, info, image)
        renderers[info] = WeakReference(renderer)

//         val renderer = PDFRenderer(this, info, image)
//         renderers[info] = WeakReference<PDFRenderer>(renderer)

        // the renderer may be null if we are getting this image from the
        // cache and rendering has completed.
        if (!renderer.isFinished) {
            renderer.go(wait)
        }

        Log.e(this::class.java.simpleName, "-> image: $image")

        return image


//        // mock
//        val bitmap = Bitmap.createBitmap(700, 1000, Bitmap.Config.ARGB_8888)
//        val canvas = Canvas(bitmap)
//        canvas.drawARGB(255, 78, 168, 186)
//
//        val paint = Paint()
//        paint.color = Color.parseColor("#FFFFFF")
//        paint.strokeWidth = 30F
//        paint.style = Paint.Style.STROKE
//        paint.isAntiAlias = true
//        paint.isDither = true
//
//        // circle center
//        val center_x = 100f
//        val center_y = 100f
//        val radius = 50f
//
//        // draw circle
//        canvas.drawCircle(center_x, center_y, radius, paint)
//        return bitmap

    }

    /* Notify all images we know about that a command has been added */
    private fun updateImages() {
        parsedCommands = commands.size
        val i: Iterator<*> = renderers.values.iterator()
        while (i.hasNext()) {
            val ref: WeakReference<PdfRendererSync> = i.next() as WeakReference<PdfRendererSync>
            val renderer: PdfRendererSync? = ref.get()
            if (renderer != null) {
                if (renderer.getStatus() == Watchable.NEEDS_DATA) {
                    // there are watchers.  Set the state to paused and
                    // let the watcher decide when to start.
                    renderer.setStatus(Watchable.PAUSED)
                }
            }
        }
    }

}
