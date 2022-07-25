package mutnemom.android.kotlindemo.reader.pdf.color

import android.graphics.Color

class GrayColorSpace : PdfColorSpace() {

    override fun getNumComponents(): Int = 1

    override fun getType(): Int = COLOR_SPACE_GRAY

    override fun getName(): String = "G"

    override fun toColor(arr: FloatArray): Int {
        return Color.rgb(
            (arr[0] * 255).toInt(),
            (arr[0] * 255).toInt(),
            (arr[0] * 255).toInt()
        )
    }

    override fun toColor(arr: IntArray): Int {
        return Color.rgb(arr[0], arr[0], arr[0])
    }

}
