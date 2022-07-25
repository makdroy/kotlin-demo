package mutnemom.android.kotlindemo.reader.pdf.commands

import android.graphics.RectF
import mutnemom.android.kotlindemo.reader.pdf.PdfCmd
import mutnemom.android.kotlindemo.reader.pdf.PdfPaint
import mutnemom.android.kotlindemo.reader.pdf.PdfRendererSync

class PdfFillPaintCmd(private val paint: PdfPaint) : PdfCmd() {

    override fun execute(state: PdfRendererSync): RectF? {
        state.setFillPaint(paint)
        return null
    }

}
