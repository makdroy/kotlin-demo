package mutnemom.android.kotlindemo.reader.pdf.decryptor

import mutnemom.android.kotlindemo.reader.pdf.PdfObject
import okio.IOException
import java.nio.ByteBuffer

interface PdfDecryptor {

    //    /**
//     * Decrypt a buffer of data
//     * @param cryptFilterName the name of the crypt filter, if V4
//     * encryption is being used, where individual crypt filters may
//     * be specified for individual streams. If encryption is not using
//     * V4 encryption (indicated by V=4 in the Encrypt dictionary) then
//     * this must be null. Null may also be specified with V4 encryption
//     * to indicate that the default filter should be used.
//     * @param streamObj the object whose stream is being decrypted. The
//     * containing object's number and generation contribute to the key used for
//     * stream encrypted with the document's default encryption, so this is
//     * typically required. Should be null only if a cryptFilterName is
//     * specified, as objects with specific stream filters use the general
//     * document key, rather than a stream-specific key.
//     * @param streamBuf the buffer to decrypt
//     * @return a buffer containing the decrypted stream, positioned at its
//     * beginning; will only be the same buffer as streamBuf if the identity
//     * decrypter is being used
//     * @throws PDFParseException if the named crypt filter does not exist, or
//     * if a crypt filter is named when named crypt filters are not supported.
//     * Problems due to incorrect passwords are revealed prior to this point.
//     */
//    @Throws(PDFParseException::class)
    @Throws(IOException::class)
    fun decryptBuffer(
        cryptFilterName: String?,
        streamObj: PdfObject?,
        streamBuf: ByteBuffer?
    ): ByteBuffer?

//    /**
//     * Decrypt a [basic string][PDFStringUtil].
//     * @param objNum the object number of the containing object
//     * @param objGen the generation number of the containing object
//     * @param inputBasicString the string to be decrypted
//     * @return the decrypted string
//     * @throws PDFParseException if the named crypt filter does not exist, or
//     * if a crypt filter is named when named crypt filters are not supported.
//     * Problems due to incorrect passwords are revealed prior to this point.
//     */
//    @Throws(PDFParseException::class)
    @Throws(IOException::class)
    fun decryptString(objNum: Int, objGen: Int, inputBasicString: String): String

//    /**
//     * Determine whether the password known by the decrypter indicates that
//     * the user is the owner of the document. Can be used, in conjunction
//     * with [.isEncryptionPresent] to determine whether any
//     * permissions apply.
//     * @return whether owner authentication is being used to decrypt the
//     * document
//     */
    fun isOwnerAuthorised(): Boolean


}
