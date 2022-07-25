package mutnemom.android.kotlindemo.reader.pdf.decoder

import mutnemom.android.kotlindemo.reader.pdf.isWhiteSpace
import okio.IOException
import java.io.ByteArrayOutputStream
import java.nio.ByteBuffer

class ASCIIHexDecoder(private val buffer: ByteBuffer) {

    companion object {

        /* decode an array of bytes in ASCIIHex format.
         *
         * ASCIIHex format consists of a sequence of Hexadecimal digits,
         * with possible whitespace, ending with the '&gt;' character.
         *
         * @param buf the encoded ASCII85 characters in a byte buffer
         * @return the decoded bytes
         */
        fun decode(buffer: ByteBuffer): ByteBuffer = ASCIIHexDecoder(buffer).decode()
    }

    /* decode the array
     * @return the decoded bytes
     */
    private fun decode(): ByteBuffer {
        // start at the beginning of the buffer
        buffer.rewind()

        // allocate the output buffer
        val outStream = ByteArrayOutputStream()

        while (true) {
            val first: Int = readHexDigit()
            val second: Int = readHexDigit()

            when {
                first == -1 -> break
                second == -1 -> {
                    outStream.write(first shl 4)
                    break
                }
                else -> outStream.write((first shl 4) + second)
            }
        }

        return ByteBuffer.wrap(outStream.toByteArray())
    }

    /* get the next character from the input
     * @return a number from 0-15, or -1 for the end character
     */
    private fun readHexDigit(): Int {
        // read until we hit a non-whitespace character or the
        // end of the stream
        while (buffer.remaining() > 0) {
            var charInt = buffer.get().toInt()

            // see if we found a useful character
            if (!charInt.toByte().isWhiteSpace()) {
                when {
                    charInt >= '0'.code && charInt <= '9'.code -> charInt -= '0'.code
                    charInt >= 'a'.code && charInt <= 'f'.code -> charInt -= 'a'.code - 10
                    charInt >= 'A'.code && charInt <= 'F'.code -> charInt -= 'A'.code - 10
                    charInt == '>'.code -> charInt = -1
                    else -> throw IOException("Bad character $charInt in ASCIIHex decode")
                }

                return charInt
            }
        }

        // end of stream reached
        throw IOException("Short stream in ASCIIHex decode")
    }

}
