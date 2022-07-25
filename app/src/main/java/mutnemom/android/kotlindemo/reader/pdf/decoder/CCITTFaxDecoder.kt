package mutnemom.android.kotlindemo.reader.pdf.decoder

import mutnemom.android.kotlindemo.reader.pdf.PdfObject
import java.nio.ByteBuffer

class CCITTFaxDecoder(
    private var fillOrder: Int,
    private var height: Int,
    private var width: Int
) {

    companion object {

        fun decode(dict: PdfObject, buffer: ByteBuffer): ByteBuffer {
            val bytes = ByteArray(buffer.remaining())
            buffer.get(bytes, 0, bytes.size)
            return ByteBuffer.wrap(decode(dict, bytes))
        }

        fun decode(dict: PdfObject, source: ByteArray): ByteArray {
            var width = 1728
            val widthDef = dict.getDictRef("Width") ?: dict.getDictRef("W")
            widthDef?.also { width = widthDef.getIntValue() }

            var height = 0
            val heightDef = dict.getDictRef("Height") ?: dict.getDictRef("H")
            heightDef?.also { height = heightDef.getIntValue() }

            val columns: Int = getOptionFieldInt(dict, "Columns", width)
            val rows: Int = getOptionFieldInt(dict, "Rows", height)
            val k: Int = getOptionFieldInt(dict, "K")
            val size = rows * (columns + 7 shr 3)
            val destination = ByteArray(size)
            val align: Boolean = getOptionFieldBoolean(dict, "EncodedByteAlign")

            val decoder = CCITTFaxDecoder(1, rows, columns)

            return destination
        }

        private fun getOptionFieldBoolean(
            dict: PdfObject,
            name: String,
            defaultValue: Boolean = false
        ): Boolean {
            val dictParams = dict.getDictRef("DecodeParms") ?: return defaultValue
            val value = dictParams.getDictRef(name) ?: return defaultValue
            return value.getBooleanValue()
        }

        private fun getOptionFieldInt(dict: PdfObject, name: String, defaultValue: Int = 0): Int {
            val dictParams = dict.getDictRef("DecodeParms") ?: return defaultValue
            val value = dictParams.getDictRef(name) ?: return defaultValue
            return value.getIntValue()
        }
    }

}
