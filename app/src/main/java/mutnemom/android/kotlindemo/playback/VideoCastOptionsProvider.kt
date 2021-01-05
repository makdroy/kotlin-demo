package mutnemom.android.kotlindemo.playback

import android.content.Context
import android.content.pm.PackageManager
import android.util.Log
import com.google.android.gms.cast.framework.CastOptions
import com.google.android.gms.cast.framework.OptionsProvider
import com.google.android.gms.cast.framework.SessionProvider
import java.util.*

@Suppress("UNUSED")
class VideoCastOptionsProvider : OptionsProvider {

    companion object {
        const val CUSTOM_NAMESPACE = "urn:x-cast:custom_namespace"
    }

    override fun getCastOptions(context: Context): CastOptions {
        Log.e("tt", "-> getCastOptions()")

        val videoCastProviderId = context.packageManager
            .getApplicationInfo(context.packageName, PackageManager.GET_META_DATA)
            .metaData.getString("video-cast-provider-id")

//        //val mediaOptions = CastMediaOptions.Builder()
//
//        val launchOptions = LaunchOptions.Builder()
//            .setAndroidReceiverCompatible(true)
//            .build()
//
//        val supportedNamespaces: MutableList<String> = arrayListOf()
//        supportedNamespaces.add(CUSTOM_NAMESPACE)
//
//        return CastOptions.Builder()
////            .setLaunchOptions(launchOptions)
//            .setReceiverApplicationId(videoCastProviderId!!)
////            .setSupportedNamespaces(supportedNamespaces)
//            //.setCastMediaOptions(mediaOptions)
//            .build()

        return CastOptions.Builder()
            .setResumeSavedSession(false)
            .setEnableReconnectionService(false)
            .setReceiverApplicationId("C0868879")
            .setStopReceiverApplicationWhenEndingSession(true)
            .build()
    }

    override fun getAdditionalSessionProviders(p0: Context): MutableList<SessionProvider>? {
        return Collections.emptyList()
    }

}
