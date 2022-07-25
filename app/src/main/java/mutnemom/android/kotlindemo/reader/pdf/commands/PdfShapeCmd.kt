package mutnemom.android.kotlindemo.reader.pdf.commands

import android.graphics.Path
import android.graphics.RectF
import mutnemom.android.kotlindemo.reader.pdf.PdfCmd
import mutnemom.android.kotlindemo.reader.pdf.PdfRendererSync

class PdfShapeCmd(private val graphicPath: Path, private val style: Int) : PdfCmd() {

    companion object {
        /* stroke the outline of the path with the stroke paint  */
        const val STROKE = 1

        /* fill the path with the fill paint  */
        const val FILL = 2

        /* perform both stroke and fill  */
        const val BOTH = 3

        /* set the clip region to the path  */
        const val CLIP = 4
    }

    /* the bounding box of the path  */
    private var bounds: RectF? = null

    override fun execute(state: PdfRendererSync): RectF? {
        var rect: RectF? = null
        if (style and FILL != 0) {
            rect = state.fill(graphicPath)
            state.lastShape = graphicPath
        }

        if (style and STROKE != 0) {
            val strokeRect: RectF? = state.stroke(graphicPath)
            rect?.also {
                strokeRect?.also { stroke -> it.union(stroke) }
            } ?: run { rect = strokeRect }
        }

        if (style and CLIP != 0) {
            state.clip(graphicPath)
        }

        return rect
    }

    override fun toString(): String {
        val sb = StringBuffer()
        val b = RectF()
        graphicPath.computeBounds(b, false)

        sb.append("ShapeCommand at: ${b.left}, ${b.top}")
        sb.append("Size: ${b.width()} x ${b.height()}")
        sb.append("Mode: ")

        if (style and FILL != 0) {
            sb.append("FILL ")
        }

        if (style and STROKE != 0) {
            sb.append("STROKE ")
        }

        if (style and CLIP != 0) {
            sb.append("CLIP")
        }

        return sb.toString()
    }

}
