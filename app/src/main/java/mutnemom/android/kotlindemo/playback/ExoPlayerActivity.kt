package mutnemom.android.kotlindemo.playback

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import com.google.android.exoplayer2.*
import com.google.android.exoplayer2.source.MediaSource
import com.google.android.exoplayer2.source.TrackGroupArray
import com.google.android.exoplayer2.source.hls.HlsMediaSource
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector
import com.google.android.exoplayer2.upstream.DefaultHttpDataSource
import mutnemom.android.kotlindemo.databinding.ActivityExoPlayerBinding
import mutnemom.android.kotlindemo.extensions.toast

class ExoPlayerActivity : AppCompatActivity(), View.OnClickListener {

    private lateinit var binding: ActivityExoPlayerBinding

    private var mp4Url =
        "https://s3-ap-southeast-1.amazonaws.com/dev.elibrary-private-contents/encoded-media-upload/video/sample_video_1.mp4"

    private var mp3Url =
        "https://s3-ap-southeast-1.amazonaws.com/dev.elibrary-private-contents/encoded-media-upload/audio/sample_audio_1.mp3"

    private var mediaUrl =
        "https://s3-ap-southeast-1.amazonaws.com/dev.elibrary-private-contents/a32b5f03-2931-40a7-99a6-4a2e0af4a839/encrypted-contents/364e6720-7526-4417-96de-d66110204b22/b7b80d5b-6666-49ad-93c1-101186173841.m3u8"

    private var qualityUrl =
        "https://bitmovin-a.akamaihd.net/content/MI201109210084_1/m3u8s-fmp4/f08e80da-bf1d-4e3d-8899-f0f6155f6efa.m3u8"

//    private var mediaUrl =
//        "https://bitdash-a.akamaihd.net/content/MI201109210084_1/m3u8s/f08e80da-bf1d-4e3d-8899-f0f6155f6efa.m3u8"

//    private var mediaUrl = "http://demo.unified-streaming.com/video/tears-of-steel/tears-of-steel.ism/.m3u8"

    private var player: ExoPlayer? = null
    private var trackSelector: DefaultTrackSelector? = null

    private var playbackPosition: Long = 0L
    private var currentWindow: Int = 0
    private var playWhenReady = false

    private var isGenQuality = false
    private val qualityList = arrayListOf<String>()

    private var videoRendererIndex = 0
    private var videoTrackGroups: TrackGroupArray? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityExoPlayerBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setEvent()
    }

    override fun onResume() {
        super.onResume()
        initPlayback()
    }

    override fun onPause() {
        super.onPause()
        releasePlayback()
    }

    override fun onClick(v: View?) {
        (v as? Button)
            ?.text
            ?.also { changeVideoTrack(it.toString()) }
    }

    private fun setEvent() {
        binding.apply {
            btnSpeed.setOnClickListener { doubleSpeed() }

            btnPause.setOnClickListener { player?.playWhenReady = false }
            btnPlay.setOnClickListener { player?.playWhenReady = true }
        }
    }

    private fun changeVideoTrack(heightPixel: String) {
        toast("play track -> $heightPixel")

        val trackOverride = if (qualityList.contains(heightPixel)) {
            val index = qualityList.indexOf(heightPixel)
            Log.e("tt", "-> found quality at index: $index")
            index
        } else {
            Log.e("tt", "-> not found quality")
            0
        }

        trackSelector?.apply {
            val builder = parameters.buildUpon()
            builder.setSelectionOverride(
                videoRendererIndex,
                videoTrackGroups!!,
                DefaultTrackSelector.SelectionOverride(0, trackOverride)
            )

            setParameters(builder)
        }
    }

    private fun getVideoQualities() {
        if (isGenQuality) return

        trackSelector?.currentMappedTrackInfo?.also {
            for (i in 0 until it.rendererCount) {
                if (it.getRendererType(i) == C.TRACK_TYPE_VIDEO) {
                    videoRendererIndex = i

                    it.getTrackGroups(i).apply {
                        videoTrackGroups = this

                        if (length > 0) {
                            val trackGroupIndex = 0
                            get(trackGroupIndex).also { trackGroup ->

                                for (trackIndex in 0 until trackGroup.length) {
                                    trackGroup.getFormat(trackIndex)
                                        .apply { qualityList.add("${height}p") }
                                }
                            }
                        }
                    }
                }
            }
        }

        qualityList.forEach {
            Button(this@ExoPlayerActivity)
                .apply { text = it }
                .also { it.setOnClickListener(this) }
                .also { binding.flowButton.addView(it) }
        }

        isGenQuality = true
    }

    private fun initPlayback() {
        if (player == null) {
            val trackSelectorParameters = DefaultTrackSelector.ParametersBuilder(this).build()
            trackSelector = DefaultTrackSelector(this)
            trackSelector?.parameters = trackSelectorParameters

            player = SimpleExoPlayer
                .Builder(this, DefaultRenderersFactory(this))
                .setTrackSelector(trackSelector!!)
                .build()

            binding.exoPlayback.player = this.player
            player?.playWhenReady = playWhenReady
            player?.seekTo(currentWindow, playbackPosition)
        }

        val mediaSource = buildMediaSource(Uri.parse(qualityUrl))

//        player?.prepare(mediaSource, true, false)
        player?.setMediaSource(mediaSource)
        player?.prepare()
//        player?.addListener(object : Player.EventListener {
//
//            override fun onPlaybackStateChanged(state: Int) {
//                super.onPlaybackStateChanged(state)
//                if (state == Player.STATE_READY) getVideoQualities()
//            }
//
//            override fun onTracksChanged(
//                trackGroups: TrackGroupArray,
//                trackSelections: TrackSelectionArray
//            ) {
//                super.onTracksChanged(trackGroups, trackSelections)
//                toast("track changed")
//            }
//
//            override fun onPlaybackParametersChanged(playbackParameters: PlaybackParameters) {
//                super.onPlaybackParametersChanged(playbackParameters)
//                toast("speed: ${playbackParameters.speed}")
//            }
//
//        })
    }

    private fun releasePlayback() {
        player?.also {
            playbackPosition = it.currentPosition
            currentWindow = it.currentWindowIndex
            playWhenReady = it.playWhenReady

            it.release()
        }

        player = null
    }

    private fun buildMediaSource(uri: Uri): MediaSource {
        val userAgent = "momentum"

        // for mp3, mp4
//        return ExtractorMediaSource.Factory(
//            DefaultHttpDataSourceFactory(userAgent)
//        ).createMediaSource(uri)

        // for m3u8
        return HlsMediaSource
            .Factory(
                DefaultHttpDataSource.Factory().apply {
                    setUserAgent(userAgent)
//                    setTransferListener(null)
//                    setConnectTimeoutMs(DefaultHttpDataSource.DEFAULT_CONNECT_TIMEOUT_MILLIS)
//                    setReadTimeoutMs(DefaultHttpDataSource.DEFAULT_READ_TIMEOUT_MILLIS)
//                    setAllowCrossProtocolRedirects(false)
                }
            )
            .createMediaSource(MediaItem.fromUri(uri))
    }

    private fun doubleSpeed() {
        player?.apply {
            val currentSpeed = playbackParameters.speed
            val expected = currentSpeed * 2

            setPlaybackParameters(PlaybackParameters(expected, 1f))
        }
    }

    private fun openLineAt() {
        when (isAppInstalled(this, linePackageName)) {
            true -> {
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(lineAtHytexts))
                startActivity(intent)
            }

            else -> popupToLoadLineApps()
        }
    }

    private fun popupToLoadLineApps() {
//        CustomAlertDialog().makeSimpleDialog(
//            this,
//            getString(R.string.text_warning_title),
//            getString(R.string.text_install_line_suggestion),
//            true
//        ) {
//            it?.dismiss()
//        }
    }

}

const val lineAtHytexts = "line://ti/p/@hytexts"
const val linePackageName = "jp.naver.line.android"
fun isAppInstalled(context: Context, packageName: String): Boolean {
    return context.packageManager.getLaunchIntentForPackage(packageName) != null
}
