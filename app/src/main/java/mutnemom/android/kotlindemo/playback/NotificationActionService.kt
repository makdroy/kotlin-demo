package mutnemom.android.kotlindemo.playback

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class NotificationActionService : BroadcastReceiver() {

    override fun onReceive(context: Context?, intent: Intent?) {
        intent
            ?.let { Intent("TRACKS TRACKS").putExtra("actionName", it.action) }
            ?.also { context?.sendBroadcast(it) }
    }

}
