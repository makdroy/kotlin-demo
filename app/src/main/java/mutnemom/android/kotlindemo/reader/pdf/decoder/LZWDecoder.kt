package mutnemom.android.kotlindemo.reader.pdf.decoder

import mutnemom.android.kotlindemo.reader.pdf.PdfObject
import mutnemom.android.kotlindemo.reader.pdf.Predictor
import okio.IOException
import java.io.ByteArrayOutputStream
import java.nio.ByteBuffer

class LZWDecoder(private var buf: ByteBuffer) {

    companion object {
        private const val CLEAR_DICT = 256
        private const val STOP = 257

        fun decode(buf: ByteBuffer, params: PdfObject?): ByteBuffer? {
            // decode the array
            val me = LZWDecoder(buf)
            var outBytes: ByteBuffer? = me.decode()

            // undo a predictor algorithm, if any was used
            params?.also {
                if (it.getDictionary()?.containsKey("Predictor") == true) {
                    Predictor.getPredictor(it)?.also { predictor ->
                        outBytes = outBytes
                            ?.let { bytes -> predictor.unPredict(bytes) }
                            ?: outBytes
                    }
                }
            }

            return outBytes
        }

    }

    private var bitsPerCode: Int
    private var dictLength: Int
    private var bytePos: Int
    private var bitPos: Int

    private var dict = arrayOfNulls<ByteArray>(4096)

    init {
        for (i in 0 until 256) {
            dict[i] = ByteArray(1)
            dict[i]?.set(0, i.toByte())
        }

        dictLength = 258
        bitsPerCode = 9
        bytePos = 0
        bitPos = 0
    }

    private fun resetDict() {
        dictLength = 258
        bitsPerCode = 9
    }

    private fun nextCode(): Int {
        var fillBits = bitsPerCode
        var value = 0

        if (bytePos >= buf.limit() - 1) {
            value = -1
        } else {

            while (fillBits > 0) {
                val nextBits = buf.get(bytePos)
                var bitsFromHere = 8 - bitPos

                if (bitsFromHere > fillBits) {
                    bitsFromHere = fillBits
                }

                value = value or (
                        (nextBits.toInt() shr (8 - bitPos - bitsFromHere)) and
                                ((0xff shr 8 - bitsFromHere) shl fillBits - bitsFromHere)
                        )

                fillBits -= bitsFromHere
                bitPos += bitsFromHere
                if (bitPos >= 8) {
                    bitPos = 0
                    bytePos++
                }
            }
        }

        return value
    }

    /* algorithm derived from:
     * http://www.rasip.fer.hr/research/compress/algorithms/fund/lz/lzw.html
     * and the PDFReference
     */
    private fun decode(): ByteBuffer? {
        var cW = CLEAR_DICT
        ByteArrayOutputStream().use { outStream ->
            while (true) {
                val pW = cW
                cW = nextCode()

                if (cW == -1) {
                    throw IOException("Missed the stop code in LZWDecode!")
                }

                if (cW == STOP) {
                    break
                } else if (cW == CLEAR_DICT) {
                    resetDict()
                } else if (pW == CLEAR_DICT) {
                    dict[cW]?.also { outStream.write(it, 0, it.size) }
                } else {

                    if (cW < dictLength) {
                        // it's a code in the dictionary
                        dict[cW]?.also {
                            outStream.write(it, 0, it.size)
                            val p = ByteArray(it.size + 1)
                            it.copyInto(p, 0, 0, it.size)

                            p[it.size] = it[0]
                            dict[dictLength++] = p
                        }
                    } else {
                        // not in the dictionary (should == dictLength)
                        dict[pW]?.also {
                            val p = ByteArray(it.size + 1)
                            it.copyInto(p, 0, 0, it.size)

                            p[it.size] = p[0]
                            outStream.write(p, 0, p.size)
                            dict[dictLength++] = p
                        }
                    }

                    if (dictLength >= ((1 shl bitsPerCode) - 1) && (bitsPerCode < 12)) {
                        bitsPerCode++
                    }
                }
            }

            return ByteBuffer.wrap(outStream.toByteArray())
        }
    }

}
