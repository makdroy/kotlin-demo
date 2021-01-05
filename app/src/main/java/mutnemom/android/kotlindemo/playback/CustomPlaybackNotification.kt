package mutnemom.android.kotlindemo.playback

import android.app.Notification
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.os.Build
import android.support.v4.media.session.MediaSessionCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import mutnemom.android.kotlindemo.R

object CustomPlaybackNotification {

    const val CHANNEL_ID = "channel001"

    private const val ACTION_NEXT = "action-next"
    private const val ACTION_PLAY = "action-play"
    private const val ACTION_PREVIOUS = "action-previous"

    var notification: Notification? = null

    fun create(context: Context, track: TrackModel, playButton: Int, pos: Int, size: Int) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val manager = NotificationManagerCompat.from(context)
            val session = MediaSessionCompat(context, "tag")
            val icon = BitmapFactory.decodeResource(context.resources, track.image)

            val code = 0
            val flag = PendingIntent.FLAG_UPDATE_CURRENT

            var pendingIntentPrevious: PendingIntent? = null
            var drw_previous = 0
            if (pos > 0) {
                drw_previous = R.drawable.exo_ic_skip_previous
                pendingIntentPrevious = Intent(context, NotificationActionService::class.java)
                    .apply { action = ACTION_PREVIOUS }
                    .let { PendingIntent.getBroadcast(context, code, it, flag) }
            }

            val pendingIntentPlay = Intent(context, NotificationActionService::class.java)
                .apply { action = ACTION_PLAY }
                .let { PendingIntent.getBroadcast(context, code, it, flag) }

            var pendingIntentNext: PendingIntent? = null
            var drw_next = 0
            if (pos == size) {
                drw_next = R.drawable.exo_ic_skip_next
                pendingIntentNext = Intent(context, NotificationActionService::class.java)
                    .apply { action = ACTION_NEXT }
                    .let { PendingIntent.getBroadcast(context, code, it, flag) }
            }

            notification = NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_music_note)
                .setContentTitle(track.title)
                .setContentText(track.artist)
                .setLargeIcon(icon)
                .setOnlyAlertOnce(true)
                .setShowWhen(false)
                .addAction(drw_previous, "Previous", pendingIntentPrevious)
                .addAction(playButton, "Play", pendingIntentPlay)
                .addAction(drw_next, "Next", pendingIntentNext)
                .setStyle(androidx.media.app.NotificationCompat.MediaStyle())
                .setPriority(NotificationCompat.PRIORITY_LOW)
                .build()

            notification?.also { manager.notify(1, it) }
        }
    }

}
