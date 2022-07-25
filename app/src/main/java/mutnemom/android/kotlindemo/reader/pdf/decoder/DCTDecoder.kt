package mutnemom.android.kotlindemo.reader.pdf.decoder

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import okio.IOException
import java.nio.ByteBuffer

object DCTDecoder {

    /* decode an array of bytes in DCT format.
     *
     * DCT is the format used by JPEG images,
     * so this class simply loads the DCT-format bytes as an image,
     * then reads the bytes out of the image to create the array.
     * Unfortunately, their most likely use is to get turned BACK into an image,
     * so this isn't terribly efficient... but is is general... don't hit, please.
     *
     * The DCT-encoded stream may have 1, 3 or 4 samples per pixel,
     * depending on the colorspace of the image.
     * In decoding, we look for the colorspace
     * in the stream object's dictionary to decide how to decode this image.
     * If no colorspace is present, we guess 3 samples per pixel.
     *
     * @param buffer the DCT-encoded buffer
     * @return the decoded buffer
     */
    fun decode(buffer: ByteBuffer): ByteBuffer {
        buffer.rewind()

        // copy the data into a byte array required by create image
        val arr = ByteArray(buffer.remaining())
        buffer.get(arr)

        val img = BitmapFactory.decodeByteArray(arr, 0, arr.size)
            ?: throw IOException("could not decode image of compressed size ${arr.size}")

        val conf = img.config
        var size = 4 * img.width * img.height
        if (conf == Bitmap.Config.RGB_565) {
            size = 2 * img.width * img.height
        }

        val byteBuf = ByteBuffer.allocate(size)
        img.copyPixelsFromBuffer(byteBuf)

        // val result = NioByteBuffer.fromNIO(byteBuf)
        val result = byteBuf
        result.rewind()
        return result
    }

}
