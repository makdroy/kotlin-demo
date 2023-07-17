package mutnemom.android.kotlindemo

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.datasource.DefaultDataSource
import androidx.media3.datasource.DefaultHttpDataSource
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.source.DefaultMediaSourceFactory
import androidx.media3.ui.PlayerView
import mutnemom.android.kotlindemo.databinding.ActivityMedia3ExoplayerBinding

class Media3ExoplayerActivity : AppCompatActivity(), PlayerView.ControllerVisibilityListener {

    private lateinit var binding: ActivityMedia3ExoplayerBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMedia3ExoplayerBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupPlayerView()
        savedInstanceState
            ?.also { restorePlayerState(it) }
            ?: initPlayerState()
    }

    override fun onStart() {
        super.onStart()
        initializePlayer()
    }

    override fun onVisibilityChanged(visibility: Int) {
        binding.debugRootView.visibility = visibility
    }

    private fun setupPlayerView() {
        binding.playerView.apply {
            setControllerVisibilityListener(this@Media3ExoplayerActivity)
//            setErrorMessageProvider(PlayerErrorMessageProvider())
            requestFocus()
        }
    }

    private fun initPlayerState() {
        clearStartPosition()
    }

    private fun restorePlayerState(savedState: Bundle) {

    }

    private var startAutoPlay: Boolean = false
    private var startItemIndex: Int = -1
    private var startPosition: Long = -1L

    private fun clearStartPosition() {
        startItemIndex = C.INDEX_UNSET
        startPosition = C.TIME_UNSET
        startAutoPlay = true
    }

    private var exoPlayer: ExoPlayer? = null
    private fun initializePlayer(): Boolean {
        if (exoPlayer == null) {
//            val mediaItems = createMediaItems(intent)
//            if (mediaItems.isEmpty()) {
//                return false
//            }
//
//            val lastSeenTracks = Tracks.EMPTY
//            val playerBuilder = ExoPlayer.Builder(this)
//                .setMediaSourceFactory(createMediaSourceFactory())
//
//            setRenderersFactory(
//                playerBuilder,
//                intent.getBooleanExtra()
//            )


            // "mp4" to "https://s3-ap-southeast-1.amazonaws.com/dev.elibrary-private-contents/encoded-media-upload/video/sample_video_1.mp4",
            // "m3u8" to "https://bitmovin-a.akamaihd.net/content/MI201109210084_1/m3u8s-fmp4/f08e80da-bf1d-4e3d-8899-f0f6155f6efa.m3u8",

            val mediaItems = MediaItem.Builder()
                .setUri(
                    "https://s3-ap-southeast-1.amazonaws.com/dev.elibrary-private-contents/encoded-media-upload/video/sample_video_1.mp4"
                    // "https://bitmovin-a.akamaihd.net/content/MI201109210084_1/m3u8s-fmp4/f08e80da-bf1d-4e3d-8899-f0f6155f6efa.m3u8"
                )
                .build()

            val httpDataSourceFactory = DefaultHttpDataSource.Factory()

            val dataSourceFactory = DefaultDataSource.Factory(this, httpDataSourceFactory)

            val mediaSourceFactory = DefaultMediaSourceFactory(this)
                .setDataSourceFactory(dataSourceFactory)

            val playerBuilder = ExoPlayer.Builder(this)
                .setMediaSourceFactory(mediaSourceFactory)

            exoPlayer = playerBuilder.build()
            exoPlayer?.apply {
//                setTrackSelectionParameters(trackSelectionParameters);
//                addListener(new PlayerEventListener ());
//                addAnalyticsListener(new EventLogger ());
//                setAudioAttributes(AudioAttributes.DEFAULT, /* handleAudioFocus= */ true);
                playWhenReady = startAutoPlay
            }

            binding.playerView.player = exoPlayer

            exoPlayer?.apply {
                setMediaItems(listOf(mediaItems), true)
                prepare()
            }
        }

        return true
    }

}
