package mutnemom.android.kotlindemo.notification

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Intent
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.NotificationCompat
import mutnemom.android.kotlindemo.MainActivity
import mutnemom.android.kotlindemo.R
import mutnemom.android.kotlindemo.databinding.ActivityNotificationBinding

class NotificationActivity : AppCompatActivity() {

    private lateinit var binding: ActivityNotificationBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityNotificationBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setNotificationChannel()
        setEvent()
    }

    private fun setEvent() {
        binding.btnSend.setOnClickListener { sendNotification() }
    }

    private fun setNotificationChannel() {
        /* in production version, should be execute once at startup */

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel(
                "local-noti-demo",
                "Demo Local Notification",
                NotificationManager.IMPORTANCE_HIGH
            ).also {
                (getSystemService(NOTIFICATION_SERVICE) as? NotificationManager)
                    ?.createNotificationChannel(it)
            }
        }
    }

    private fun sendNotification() {
        val manager = getSystemService(NOTIFICATION_SERVICE) as? NotificationManager
        val intent = Intent(this, MainActivity::class.java)
        val contentIntent = PendingIntent
            .getActivity(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT)

        NotificationCompat
            .Builder(this, "local-noti-demo")
            .setAutoCancel(true)
            .setDefaults(Notification.DEFAULT_ALL)
            .setWhen(System.currentTimeMillis())
            .setSmallIcon(R.drawable.ic_launcher_v24)
            .setColor(Color.parseColor("#0066FF"))
            .setTicker("Hearty365")
            .setContentTitle("Default notification")
            .setContentText("Lorem ipsum dolor sit amet, consectetur adipiscing elit.")
            .setDefaults(Notification.DEFAULT_LIGHTS or Notification.DEFAULT_SOUND)
            .setContentIntent(contentIntent)
            .setContentInfo("Info")
            .also { manager?.notify(1, it.build()) }

        /* akexorcist */
//        val notification = NotificationCompat.Builder(this, "local-noti-demo")
//            .apply {
//                setSmallIcon(R.drawable.ic_launcher_v24)
//                setContentTitle("noti-, title")
//                setContentText("noti-, text")
//            }.build()
//
//        (getSystemService(NOTIFICATION_SERVICE) as? NotificationManager)
//            ?.notify(0, notification)

    }

}
