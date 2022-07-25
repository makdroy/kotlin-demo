package mutnemom.android.kotlindemo.reader.pdf.decoder

import okio.IOException
import java.io.ByteArrayOutputStream
import java.nio.ByteBuffer

class ASCII85Decoder(private val buffer: ByteBuffer) {

    companion object {

        /* decode an array of bytes in ASCII85 format.
         *
         * In ASCII85 format, every 5 characters represents 4 decoded
         * bytes in base 85.  The entire stream can contain whitespace,
         * and ends in the characters '~&gt;'.
         *
         * @param buf the encoded ASCII85 characters in a byte buffer
         * @return the decoded bytes
         */
        fun decode(buffer: ByteBuffer): ByteBuffer = ASCII85Decoder(buffer).decode()
    }

    /* decode the bytes
     * @return the decoded bytes
     */
    private fun decode(): ByteBuffer {
        // start from the beginning of the data
        buffer.rewind()

        // allocate the output buffer
        val outStream = ByteArrayOutputStream()

        // decode the bytes
        var hasNext = decode5(outStream)
        while (hasNext) {
            hasNext = decode5(outStream)
        }

        return ByteBuffer.wrap(outStream.toByteArray())
    }

    /* decode the next five ASCII85 characters into up to four decoded bytes.
     *
     * @param outStream the ByteArrayOutputStream to write output to, set to the correct position
     * @return false when finished, or true otherwise.
     */
    private fun decode5(outStream: ByteArrayOutputStream): Boolean {
        // stream ends in ~>
        val five = IntArray(5)
        var roundCounter = 0
        for (i in five.indices) {
            roundCounter = i

            five[i] = nextChar()

            if (five[i] == '~'.code) {
                when (nextChar()) {
                    '>'.code -> break
                    else -> throw IOException("Bad character in ASCII85Decode: not ~>")
                }

            } else if (five[i] >= '!'.code && five[i] <= 'u'.code) {
                five[i] -= '!'.code

            } else if (five[i] == 'z'.code) {
                when (i) {
                    0 -> {
                        five[i] = 0
                        roundCounter = 4
                        break
                    }
                    else -> throw IOException("Inappropriate 'z' in ASCII85Decode")
                }

            } else {
                throw IOException(
                    "Bad character in ASCII85Decode: "
                            + five[i] + " (" + five[i].toChar() + ")"
                )
            }
        }

        if (roundCounter > 0) {
            roundCounter = roundCounter.dec()
        }

        val valueInt = five[0] * 85 * 85 * 85 * 85 +
                five[1] * 85 * 85 * 85 +
                five[2] * 85 * 85 +
                five[3] * 85 +
                five[4]

        for (j in 0 until roundCounter) {
            val shiftCounter = 8 * (3 - j)
            val writeValue = ((valueInt shr shiftCounter) and 0xff)
            outStream.write(writeValue)
        }

        return (roundCounter == 4)
    }

    /* get the next character from the input.
     * @return the next character, or -1 if at end of stream
     */
    private fun nextChar(): Int {
        // skip whitespace
        // returns next character, or -1 if end of stream

        var nextChar = -1
        while (buffer.remaining() > 0) {
            val char = buffer.get().toInt().toChar()
            if (!char.isWhitespace()) {
                nextChar = char.code
            }
        }

        return nextChar
    }

}
