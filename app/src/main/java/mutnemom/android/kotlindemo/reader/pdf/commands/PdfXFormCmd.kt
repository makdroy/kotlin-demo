package mutnemom.android.kotlindemo.reader.pdf.commands

import android.graphics.Matrix
import android.graphics.RectF
import mutnemom.android.kotlindemo.reader.pdf.PdfCmd
import mutnemom.android.kotlindemo.reader.pdf.PdfRendererSync

class PdfXFormCmd(private val matrix: Matrix) : PdfCmd() {

    override fun execute(state: PdfRendererSync): RectF? {
        state.transform(matrix)
        return null
    }

}
