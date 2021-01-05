package mutnemom.android.kotlindemo.services

@Suppress("UNUSED")
object JNIProvider {

    init {
        System.loadLibrary("native-lib")
    }

    val baseUrl: String
        get() = getApiBinanceUrl()

    private external fun getApiBinanceUrl(): String

}
