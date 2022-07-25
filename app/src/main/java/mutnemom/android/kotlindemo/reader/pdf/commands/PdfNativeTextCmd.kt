package mutnemom.android.kotlindemo.reader.pdf.commands

import android.graphics.Matrix
import android.graphics.RectF
import mutnemom.android.kotlindemo.reader.pdf.PdfCmd
import mutnemom.android.kotlindemo.reader.pdf.PdfRendererSync

class PdfNativeTextCmd(private val text: String, private val matrix: Matrix) : PdfCmd() {

    private var x = 0f
    private var y = 0f
    private var w = 0f
    private var h = 0f
    private var bounds: RectF

    init {
        val values = FloatArray(9)
        matrix.getValues(values)
        this.x = values[2]
        this.y = values[5]
        this.w = values[0]
        this.h = values[4]
        bounds = RectF(x, y, x + w, y + h)
    }

    override fun execute(state: PdfRendererSync): RectF = state.drawNativeText(text, bounds)

}
