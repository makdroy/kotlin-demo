package mutnemom.android.kotlindemo.reader.pdf

import android.graphics.Matrix
import android.graphics.Path
import android.graphics.PointF
import mutnemom.android.kotlindemo.reader.pdf.commands.PdfShapeCmd

class PdfGlyph(
    val src: Char = 0.toChar(),
    private val name: String?,
    private val advance: PointF?,
    private val shape: Path? = null,
    private val page: PdfPage? = null
) {

    fun addCommands(cmd: PdfPage, transform: Matrix?, mode: Int): PointF? {
        if (shape != null) {
            val outline = Path()
            shape.transform(transform!!, outline)
            cmd.addCommand(PdfShapeCmd(outline, mode))
        } else {
            page?.also { cmd.addCommands(it, transform) }
        }

        return advance
    }

    override fun toString(): String {
        val str = StringBuffer()
        str.append(name)
        return str.toString()
    }

}
