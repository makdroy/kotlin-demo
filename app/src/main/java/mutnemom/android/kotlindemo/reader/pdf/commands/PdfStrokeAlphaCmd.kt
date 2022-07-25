package mutnemom.android.kotlindemo.reader.pdf.commands

import android.graphics.RectF
import mutnemom.android.kotlindemo.reader.pdf.PdfCmd
import mutnemom.android.kotlindemo.reader.pdf.PdfRendererSync

class PdfStrokeAlphaCmd(private val alpha: Float) : PdfCmd() {

    override fun execute(state: PdfRendererSync): RectF? {
        // state.setStrokeAlpha(alpha)
        return null
    }

}
