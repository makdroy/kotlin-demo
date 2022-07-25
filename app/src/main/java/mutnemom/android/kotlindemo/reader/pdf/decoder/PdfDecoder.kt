package mutnemom.android.kotlindemo.reader.pdf.decoder

import mutnemom.android.kotlindemo.reader.pdf.PdfObject
import mutnemom.android.kotlindemo.reader.pdf.decryptor.PdfDecryptorFactory
import okio.IOException
import java.nio.ByteBuffer

/* A PDF Decoder encapsulates all the methods of decoding a stream of bytes
 * based on all the various encoding methods.
 *
 * You should use the decodeStream() method of this object rather than using
 * any of the decoders directly.
 */
object PdfDecoder {

    /* decode a byte[] stream using the filters specified in the object's
     * dictionary (passed as argument 1).
     * @param dict the dictionary associated with the stream
     * @param streamBuf the data in the stream, as a byte buffer
     */
    fun decodeStream(dict: PdfObject, streamBuf: ByteBuffer): ByteBuffer? {
        var resultBuf: ByteBuffer? = streamBuf
        return dict.getDictRef("Filter")
            ?.let {
                // apply filters
                val arr: Array<PdfObject>
                val params: Array<PdfObject>

                if (it.type == PdfObject.NAME) {
                    arr = arrayOf(it)
                    params = dict.getDictRef("DecodeParms")
                        ?.let { param -> arrayOf(param) }
                        ?: arrayOf()

                } else {
                    arr = it.getArray() ?: arrayOf()
                    params = dict.getDictRef("DecodeParms")?.getArray() ?: arrayOf()
                }

                // determine whether default encryption applies or if there's a
                // specific Crypt filter; it must be the first filter according to
                // the errata for PDF1.7
                val specificCryptFilter =
                    arr.isNotEmpty() && arr[0].getStringValue().equals("Crypt")

                if (!specificCryptFilter) {
                    // no crypt filter, so should apply default decryption
                    // (if present!)
                    resultBuf = dict.decryptor.decryptBuffer(null, dict, streamBuf)
                }

                for (i in arr.indices) {
                    val stringValue = arr[i].getStringValue()
                    stringValue?.also { encrypted ->
                        resultBuf = when (encrypted) {
                            "FlateDecode", "Fl" ->
                                FlateDecoder.decode(streamBuf, params.getOrNull(i))

//                            "LZWDecode", "LZW" -> LZWDecoder.decode(streamBuf, params[i])
//                            "ASCII85Decode", "A85" -> ASCII85Decoder.decode(streamBuf)
//                            "ASCIIHexDecode", "AHx" -> ASCIIHexDecoder.decode(streamBuf)
//                            "RunLengthDecode", "RL" -> RunLengthDecoder.decode(streamBuf)
                            "DCTDecode", "DCT" -> DCTDecoder.decode(streamBuf)
//                            "CCITTFaxDecode", "CCF" -> CCITTFaxDecoder.decode(dict, streamBuf)
                            "Crypt" -> {
                                var cfName: String? = PdfDecryptorFactory.CF_IDENTITY
                                val nameObj = params[i].getDictRef("Name")
                                if (nameObj != null && nameObj.type == PdfObject.NAME) {
                                    cfName = nameObj.getStringValue()
                                }

                                dict.decryptor.decryptBuffer(cfName, null, streamBuf)
                            }
                            else -> throw IOException(
                                "Unknown coding method: ${arr[i].getStringValue()}"
                            )
                        }
                    }
                }

                resultBuf
            }
            ?: dict.decryptor.decryptBuffer(null, dict, streamBuf)
    }

}
