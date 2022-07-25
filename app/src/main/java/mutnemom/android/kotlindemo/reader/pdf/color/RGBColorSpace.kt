package mutnemom.android.kotlindemo.reader.pdf.color

import android.graphics.Color

class RGBColorSpace : PdfColorSpace() {

    override fun getNumComponents(): Int = 3

    override fun getType(): Int = COLOR_SPACE_RGB

    override fun getName(): String = "RGB"

    override fun toColor(arr: FloatArray): Int {
        return Color.rgb(
            (arr[0] * 255).toInt(),
            (arr[1] * 255).toInt(),
            (arr[2] * 255).toInt()
        )
    }

    override fun toColor(arr: IntArray): Int {
        return Color.rgb(arr[0], arr[1], arr[2])
    }

}
