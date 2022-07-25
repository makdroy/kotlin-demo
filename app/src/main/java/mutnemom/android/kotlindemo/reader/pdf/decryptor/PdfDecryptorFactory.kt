package mutnemom.android.kotlindemo.reader.pdf.decryptor

object PdfDecryptorFactory {

    /* Default key length for versions where key length is optional */
    private const val DEFAULT_KEY_LENGTH = 40

    /* The name of the standard Identity CryptFilter */
    const val CF_IDENTITY = "Identity"

//    fun createDecryptor(
//        encryptDict: PdfObject?,
//        documentId: PdfObject?,
//        password: PdfPassword?
//    ): PdfDecryptor {
//
//        val notNullPwd = password ?: PdfPassword("")
//        if (encryptDict == null) {
//            return IdentityDecryptor.INSTANCE
//
//        } else {
//            // this means that we'll fail if,
//            // for example, public key encryption is employed
//
//            return encryptDict.getDictRef("Filter")
//                ?.let { filter ->
//                    when (filter.getStringValue()) {
//                        "Standard" -> prepareStandardDecryptor(encryptDict, documentId, notNullPwd)
//                        else -> throw IOException(
//                            "Unsupported encryption Filter: $filter; only Standard is supported."
//                        )
//                    }
//                }
//                ?: throw IOException("No Filter specified in Encrypt dictionary")
//        }
//    }

//    private fun prepareStandardDecryptor(
//        encryptDict: PdfObject,
//        documentId: PdfObject?,
//        password: PdfPassword
//    ): PdfDecryptor =
//        encryptDict.getDictRef("V")
//            ?.let { obj ->
//                when (val valueInt = obj.getIntValue()) {
//                    1, 2 -> {
//                        val lengthObj = encryptDict.getDictRef("Length")
//                        val length = lengthObj?.getIntValue() ?: DEFAULT_KEY_LENGTH
//
//                        createStandardDecryptor(
//                            encryptDict,
//                            documentId,
//                            password,
//                            length,
//                            false,
//                            StandardDecryptor.EncryptionAlgorithm.RC4
//                        )
//                    }
//                    4 -> createCryptFilterDecryptor(encryptDict, documentId, password, valueInt)
//                    else -> throw IOException("Unsupported encryption version: $valueInt")
//                }
//            }
//            ?: throw IOException("Cannot extract encryption version")

//    private fun createStandardDecryptor(
//        encryptDict: PdfObject,
//        documentId: PdfObject?,
//        password: PdfPassword,
//        keyLength: Int,
//        encryptMetadata: Boolean,
//        encryptionAlgorithm: StandardDecryptor.EncryptionAlgorithm
//    ): PdfDecryptor {
//
//    }
//
//    private fun createCryptFilterDecryptor(
//        encryptDict: PdfObject,
//        documentId: PdfObject?,
//        password: PdfPassword,
//        cryptFilterValue: Int
//    ): PdfDecryptor {
//
//    }

}
