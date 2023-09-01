package mutnemom.android.kotlindemo.utils

import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.util.Base64
import java.nio.charset.StandardCharsets
import java.security.MessageDigest
import java.security.NoSuchAlgorithmException

object SignatureUtils {

    @Suppress("DEPRECATION")
    fun getAppHash(context: Context): List<String> {
        val appCodes = arrayListOf<String>()
        try {
            val packageName = context.packageName
            val packageManager = context.packageManager

            val base64Hash: String? = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                val packageInfo = packageManager
                    .getPackageInfo(packageName, PackageManager.GET_SIGNING_CERTIFICATES)

                packageInfo?.signingInfo?.let {
                    if (it.hasMultipleSigners()) {
                        hash(packageName, it.apkContentsSigners.toString())
                    } else {
                        hash(packageName, it.signingCertificateHistory.toString())
                    }
                }

            } else {
                packageManager
                    .getPackageInfo(packageName, PackageManager.GET_SIGNATURES)
                    ?.signatures
                    ?.getOrNull(0)
                    ?.let { hash(packageName, it.toCharsString()) }
            }

            base64Hash
                ?.takeIf { it.isNotEmpty() }
                ?.also { appCodes.add(String.format("%s", it)) }

        } catch (e: PackageManager.NameNotFoundException) {
            e.printStackTrace()
        }

        return appCodes
    }

    private fun hash(packageName: String, signature: String): String? {
        val appInfo = "$packageName $signature"
        var hashString: String? = null
        try {
            val messageDigest = MessageDigest.getInstance("SHA-256")
            messageDigest.update(appInfo.toByteArray(StandardCharsets.UTF_8))
            var hashSignature = messageDigest.digest()
            hashSignature = hashSignature.copyOfRange(0, 9)
            val base64Hash =
                Base64.encodeToString(hashSignature, Base64.NO_PADDING or Base64.NO_WRAP)
            hashString = base64Hash.substring(0, 11)

        } catch (e: NoSuchAlgorithmException) {
            e.printStackTrace()
        }

        return hashString
    }

}
