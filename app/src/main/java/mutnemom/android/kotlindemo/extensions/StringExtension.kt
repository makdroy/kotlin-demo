package mutnemom.android.kotlindemo.extensions

import java.security.MessageDigest

fun String.sha256(): ByteArray = hashString("SHA-256")
fun String.hashString(algorithm: String): ByteArray = MessageDigest
    .getInstance(algorithm)
    .digest(toByteArray())
