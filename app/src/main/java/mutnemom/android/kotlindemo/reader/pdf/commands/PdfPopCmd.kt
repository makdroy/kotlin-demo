package mutnemom.android.kotlindemo.reader.pdf.commands

import android.graphics.RectF
import mutnemom.android.kotlindemo.reader.pdf.PdfCmd
import mutnemom.android.kotlindemo.reader.pdf.PdfRendererSync

class PdfPopCmd : PdfCmd() {

    override fun execute(state: PdfRendererSync): RectF? {
        state.pop()
        return null
    }

}
