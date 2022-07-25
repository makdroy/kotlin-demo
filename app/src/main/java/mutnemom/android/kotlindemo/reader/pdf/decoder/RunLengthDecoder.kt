package mutnemom.android.kotlindemo.reader.pdf.decoder

import java.io.ByteArrayOutputStream
import java.nio.ByteBuffer

class RunLengthDecoder(private val buffer: ByteBuffer) {

    companion object {

        /* the end of data in the RunLength encoding. */
        private const val RUN_LENGTH_EOD = 128

        /* decode an array of bytes in RunLength format.
         *
         * RunLength format consists of a sequence of a byte-oriented format based on run length.
         * There are a series of "runs",
         * where a run is a length byte followed by 1 to 128 bytes of data.
         * If the length is 0-127, the following length+1 (1 to 128) bytes are to be copied.
         * If the length is 129 through 255, the following
         * single byte is copied 257-length (2 to 128) times.
         * A length value of 128 means and End of Data (EOD).
         *
         * @param buf the RunLength encoded bytes in a byte buffer
         * @return the decoded bytes
         */
        fun decode(buffer: ByteBuffer): ByteBuffer = RunLengthDecoder(buffer).decode()
    }

    /* decode the array
     * @return the decoded bytes
     */
    private fun decode(): ByteBuffer {
        // start at the beginning of the buffer
        buffer.rewind()

        // allocate the output buffer
        val outStream = ByteArrayOutputStream()

        val copy = ByteArray(128)
        var dupAmount: Int = buffer.get().toInt()
        while (dupAmount != -1 && dupAmount != RUN_LENGTH_EOD) {
            if (dupAmount <= 127) {
                val amountToCopy = dupAmount + 1
                while (amountToCopy > 0) {
                    buffer.get(copy, 0, amountToCopy)
                    outStream.write(copy, 0, amountToCopy)
                }

            } else {
                val dupByte: Byte = buffer.get()
                for (i in 0 until (257 - (dupAmount and 0xFF))) {
                    outStream.write(dupByte.toInt())
                }
            }

            dupAmount = buffer.get().toInt()
        }

        return ByteBuffer.wrap(outStream.toByteArray())
    }

}
