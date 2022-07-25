package mutnemom.android.kotlindemo.reader.pdf.decryptor

class StandardDecryptor {

    /* Describes an encryption algorithm to be used,
     * declaring not only the cipher type,
     * but also key generation techniques
     */
    enum class EncryptionAlgorithm {
        RC4, AESV2;

        val isRC4: Boolean
            get() = this == RC4

        val isAES: Boolean
            get() = this == AESV2
    }

}
