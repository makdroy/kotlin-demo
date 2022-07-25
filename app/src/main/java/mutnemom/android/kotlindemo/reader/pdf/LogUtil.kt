package mutnemom.android.kotlindemo.reader.pdf

import android.util.Log

object LogUtil {

    fun logInfo(obj: Any, message: String) {
        Log.i(obj::class.java.simpleName, message)
    }

    fun logWarning(obj: Any, message: String) {
        Log.w(obj::class.java.simpleName, message)
    }

    fun logError(obj: Any, message: String) {
        Log.e(obj::class.java.simpleName, message)
    }

}
