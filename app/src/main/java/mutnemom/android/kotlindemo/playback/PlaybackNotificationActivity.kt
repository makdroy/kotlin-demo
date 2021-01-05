package mutnemom.android.kotlindemo.playback

import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import mutnemom.android.kotlindemo.R
import mutnemom.android.kotlindemo.databinding.ActivityPlaybackNotificationBinding
import mutnemom.android.kotlindemo.extensions.toast

class PlaybackNotificationActivity : AppCompatActivity() {

    private lateinit var binding: ActivityPlaybackNotificationBinding

    private var notificationManager: NotificationManager? = null
    private val tracks by lazy {
        listOf(
            TrackModel(R.drawable.track1, "Track 1", "Artist 1"),
            TrackModel(R.drawable.track2, "Track 2", "Artist 2"),
            TrackModel(R.drawable.track3, "Track 3", "Artist 3"),
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityPlaybackNotificationBinding.inflate(layoutInflater)
        setContentView(binding.root)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            createChannel()
        }

        binding.btnPlay.setOnClickListener {
            toast("-> on click")
            CustomPlaybackNotification.create(this, tracks[1], R.drawable.ic_playback_pause, 1, tracks.size - 1)
        }
    }

    private fun createChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CustomPlaybackNotification.CHANNEL_ID,
                "Kotlin Demo",
                NotificationManager.IMPORTANCE_LOW
            )

            notificationManager = getSystemService(NotificationManager::class.java)
            notificationManager?.apply { createNotificationChannel(channel) }
        }
    }

}
