package mutnemom.android.kotlindemo.services

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.google.android.gms.auth.api.phone.SmsRetriever
import com.google.android.gms.common.api.CommonStatusCodes
import com.google.android.gms.common.api.Status

class SmsReceiver : BroadcastReceiver() {

    @Suppress("DEPRECATION")
    override fun onReceive(p0: Context?, p1: Intent?) {
        p1 ?: return
        if (SmsRetriever.SMS_RETRIEVED_ACTION == p1.action) {
            val extras = p1.extras
            val status = extras?.get(SmsRetriever.EXTRA_STATUS) as Status

            when (status.statusCode) {
                CommonStatusCodes.SUCCESS -> {
                    val msg = extras.get(SmsRetriever.EXTRA_SMS_MESSAGE)
                    Log.e("tt", "-> sms: $msg")
                }
                CommonStatusCodes.TIMEOUT -> {}
            }
        }
    }

}
