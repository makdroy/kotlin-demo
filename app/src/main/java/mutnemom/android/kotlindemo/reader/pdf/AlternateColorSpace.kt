package mutnemom.android.kotlindemo.reader.pdf

import android.graphics.Color
import mutnemom.android.kotlindemo.reader.pdf.color.PdfColorSpace
import mutnemom.android.kotlindemo.reader.pdf.function.PdfFunction

class AlternateColorSpace(
    private val alternate: PdfColorSpace?,
    private val function: PdfFunction?
) : PdfColorSpace() {

    override fun getNumComponents(): Int {
        return function?.numInputs ?: (alternate?.getNumComponents() ?: 0)
    }

    override fun getType(): Int = COLOR_SPACE_ALTERNATE

    override fun getName(): String = "ALTERNATE"

    override fun toColor(arr: FloatArray): Int {
        val fComp = function?.calculate(arr) ?: arr
        val k: Float = fComp[3]
        val w = 255 * (1 - k)
        val r: Float = w * (1 - fComp[0])
        val g: Float = w * (1 - fComp[1])
        val b: Float = w * (1 - fComp[2])

        return Color.rgb(r.toInt(), g.toInt(), b.toInt())
    }

    override fun toColor(arr: IntArray): Int {
        val fArr = FloatArray(arr.size)
        for (i in fArr.indices) {
            fArr[i] = arr[i].toFloat() / 255
        }

        return toColor(fArr)
    }

}
