package mutnemom.android.kotlindemo.playback

import android.os.Bundle
import android.view.Menu
import androidx.appcompat.app.AppCompatActivity
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.MediaMetadata
import com.google.android.exoplayer2.util.MimeTypes
import com.google.android.gms.cast.framework.CastButtonFactory
import com.google.android.gms.cast.framework.CastContext
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailability
import mutnemom.android.kotlindemo.R
import mutnemom.android.kotlindemo.databinding.ActivityExoPlayerCastBinding
import mutnemom.android.kotlindemo.extensions.toast

class ExoPlayerCastActivity : AppCompatActivity() {

    private lateinit var binding: ActivityExoPlayerCastBinding

    private var videoCastPlayerManager: VideoCastPlayerManager? = null
    private var videoCastContext: CastContext? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        GoogleApiAvailability.getInstance()
            .isGooglePlayServicesAvailable(this)
            .also { playServicesState ->
                if (playServicesState == ConnectionResult.SUCCESS) {
                    videoCastContext = CastContext.getSharedInstance(this)
                }
            }

        binding = ActivityExoPlayerCastBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setEvent()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        return if (menu == null) {
            super.onCreateOptionsMenu(menu)
        } else {
            super.onCreateOptionsMenu(menu)
            menuInflater.inflate(R.menu.exoplayer_cast_menu, menu)
            CastButtonFactory.setUpMediaRouteButton(this, menu, R.id.media_route_menu_item)
            true
        }
    }

    override fun onResume() {
        super.onResume()

        if (videoCastContext == null) {
            toast("cast context is null, do nothing")
        } else {
            setupPlayerManager()
        }
    }

    override fun onPause() {
        super.onPause()
        if (videoCastContext != null) {
            videoCastPlayerManager?.release()
            videoCastPlayerManager = null
        }
    }

    private fun setEvent() {
        binding.btnPlay.setOnClickListener {
            videoCastPlayerManager?.addItem(createMediaItem())
        }
    }

    private fun setupPlayerManager() {
        videoCastPlayerManager = VideoCastPlayerManager(
            this,
            videoCastContext!!,
            binding.exoPlayback
        )
    }

    private fun createMediaItem(): MediaItem = MediaItem.Builder()
        .setUri("https://storage.googleapis.com/shaka-demo-assets/angel-one-hls/hls.m3u8")
        .setMediaMetadata(MediaMetadata.Builder().setTitle("Test Casting Video").build())
        .setMimeType(MimeTypes.APPLICATION_M3U8)
        .build()

}
