package mutnemom.android.kotlindemo.reader.pdf

import mutnemom.android.kotlindemo.reader.pdf.color.PdfColorSpace

class IndexedColor(
    private var count: Int = 0,
    var table: IntArray? = null,
    base: PdfColorSpace? = null,
    stream: PdfObject? = null
) : PdfColorSpace() {

    init {
        if (table == null) {
            count++
            table = IntArray(count)

            stream?.also {
                val data = it.getStream()

                base?.also { baseColorSpace ->
                    val comps = FloatArray(baseColorSpace.getNumComponents())

                    var loc = 0
                    for (i in 0 until count) {
                        for (j in comps.indices) {
                            comps[j] = if (loc < data!!.size) {
                                (data[loc++].toInt() and 0xff) / 255f
                            } else {
                                1.0f
                            }
                        }

                        table!![i] = baseColorSpace.toColor(comps)
                    }
                }
            }
        } else {
            count = table!!.size
        }
    }

    override fun getNumComponents(): Int = 1

    override fun getName(): String = "I"

    override fun getType(): Int = COLOR_SPACE_INDEXED

    override fun toColor(arr: FloatArray): Int {
        return table!![(255 * arr[0]).toInt()]
    }

    override fun toColor(arr: IntArray): Int {
        return table!![arr[0]]
    }

}
