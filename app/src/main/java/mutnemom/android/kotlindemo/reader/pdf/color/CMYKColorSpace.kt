package mutnemom.android.kotlindemo.reader.pdf.color

import android.graphics.Color

class CMYKColorSpace : PdfColorSpace() {

    override fun getNumComponents(): Int = 4

    override fun getType(): Int = COLOR_SPACE_CMYK

    override fun getName(): String = "CMYK"

    override fun toColor(arr: FloatArray): Int {
        val k: Float = arr[3]
        val w = 255 * (1 - k)
        val r: Float = w * (1 - arr[0])
        val g: Float = w * (1 - arr[1])
        val b: Float = w * (1 - arr[2])
        return Color.rgb(r.toInt(), g.toInt(), b.toInt())
    }

    override fun toColor(arr: IntArray): Int {
        val k: Int = arr[3]
        val w = 255 - k
        val r: Int = w * (255 - arr[0]) / 255
        val g: Int = w * (255 - arr[1]) / 255
        val b: Int = w * (255 - arr[2]) / 255
        return Color.rgb(r, g, b)
    }

}
