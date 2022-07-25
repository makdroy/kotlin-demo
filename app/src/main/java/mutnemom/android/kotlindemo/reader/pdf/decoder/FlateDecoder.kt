package mutnemom.android.kotlindemo.reader.pdf.decoder

import android.util.Log
import mutnemom.android.kotlindemo.reader.pdf.PdfObject
import mutnemom.android.kotlindemo.reader.pdf.Predictor
import okio.IOException
import java.io.ByteArrayOutputStream
import java.nio.ByteBuffer
import java.util.zip.DataFormatException
import java.util.zip.Inflater

object FlateDecoder {

    /* Inflater is a built-in Java algorithm.
     * It's part of the java.util.zip package.
     *
     * @param buf the deflated input buffer
     * @param params parameters to the decoder (unused)
     *
     * @return the decoded (inflated) bytes
     */
    fun decode(buf: ByteBuffer, params: PdfObject?): ByteBuffer? {
        Log.e("tt", "-> stream size: ${buf.remaining()}")
        val first100 = ByteArray(100)
        buf.get(first100)
        String(first100, Charsets.UTF_8)
            .also { Log.e("tt", "-> first100: $it") }


        val inflater= Inflater(false)
        val bufSize = buf.remaining()
        var outBytes: ByteBuffer? = null

        if (buf.hasArray()) {
            val data = buf.array()
            inflater.setInput(data, buf.arrayOffset() + buf.position(), bufSize)
            buf.position(buf.position() + bufSize)
        } else {
            // copy the data, since the array() method is not supported
            // on raf-based ByteBuffers
            val data = ByteArray(bufSize)
            buf.get(data)
            inflater.setInput(data)
        }

        // output to a byte-array output stream,
        // since we don't know how big the output will be
        ByteArrayOutputStream().use { outStream ->
            val decompressed = ByteArray(bufSize)
            var read: Int

            try {
                while (!inflater.finished()) {
                    read = inflater.inflate(decompressed)
                    if (read <= 0) {
                        if (inflater.needsDictionary()) {
                            throw IOException(
                                "Don't know how to ask for a dictionary in FlateDecode"
                            )
                        } else {
                            outBytes = ByteBuffer.allocate(0)
                        }
                    }

                    outStream.write(decompressed, 0, read)
                }

                if (outBytes == null) {
                    outBytes = ByteBuffer.wrap(outStream.toByteArray())
                    if (params?.getDictionary()?.containsKey("Predictor") == true) {
                        val predictor = Predictor.getPredictor(params)
                        if (predictor != null) {
                            outBytes = predictor.unPredict(outBytes!!)
                        }
                    }
                }
            } catch (e: DataFormatException) {
                throw IOException("Data format exception: ${e.message}")
            }
        }

        return outBytes
    }

}
