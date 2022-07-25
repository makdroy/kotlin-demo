package mutnemom.android.kotlindemo.reader.pdf.decryptor

import mutnemom.android.kotlindemo.reader.pdf.PdfObject
import okio.IOException
import java.nio.ByteBuffer

class IdentityDecryptor : PdfDecryptor {

    companion object {
        val INSTANCE = IdentityDecryptor()
    }

    override fun isOwnerAuthorised(): Boolean = false

    override fun decryptString(objNum: Int, objGen: Int, inputBasicString: String): String {
        return inputBasicString
    }

    override fun decryptBuffer(
        cryptFilterName: String?,
        streamObj: PdfObject?,
        streamBuf: ByteBuffer?
    ): ByteBuffer? {
        if (cryptFilterName != null) {
            throw IOException("This Encryption version does not support Crypt filters")
        }

        return streamBuf
    }

}
