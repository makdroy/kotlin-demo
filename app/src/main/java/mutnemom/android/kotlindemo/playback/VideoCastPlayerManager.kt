package mutnemom.android.kotlindemo.playback

import android.content.Context
import android.widget.Toast
import com.google.android.exoplayer2.C
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.ext.cast.CastPlayer
import com.google.android.exoplayer2.ext.cast.SessionAvailabilityListener
import com.google.android.exoplayer2.ui.PlayerView
import com.google.android.exoplayer2.ui.StyledPlayerControlView
import com.google.android.gms.cast.framework.CastContext

class VideoCastPlayerManager(
    context: Context,
    castContext: CastContext,
    private val playerView: PlayerView
) : Player.Listener, SessionAvailabilityListener {

    private var currentPlayer: Player? = null
    private val mediaQueue: ArrayList<MediaItem> = arrayListOf()

    private val localPlayer = ExoPlayer.Builder(context).build()
    private val castPlayer = CastPlayer(castContext)

    init {
        localPlayer.addListener(this)
        castPlayer.addListener(this)
        castPlayer.setSessionAvailabilityListener(this)

        if (castPlayer.isCastSessionAvailable) {
            setCurrentPlayer(castPlayer)
        } else {
            setCurrentPlayer(localPlayer)
        }
    }

    override fun onCastSessionAvailable() {
        setCurrentPlayer(castPlayer)
    }

    override fun onCastSessionUnavailable() {
        setCurrentPlayer(localPlayer)
    }

    fun addItem(item: MediaItem) {
        mediaQueue.add(item)
//        currentPlayer?.setMediaItems(mediaQueue, 0, 0)

        currentPlayer?.addMediaItem(item)
    }

    fun release() {
        mediaQueue.clear()
        castPlayer.setSessionAvailabilityListener(null)
        castPlayer.release()
        localPlayer.release()
        playerView.player = null
    }

    private fun setCurrentPlayer(player: Player) {
        if (player == currentPlayer) return

        playerView.controllerHideOnTouch = (player == localPlayer)

        if (player == castPlayer) {
//            playerView.controllerShowTimeoutMs = 0
//            playerView.showController()
//            playerView.defaultArtwork = ResourcesCompat.getDrawable(
//                playerView.resources,
//                R.drawable.ic_baseline_cast_connected_400,
//                null
//            )

            Toast.makeText(playerView.context, "-> casting", Toast.LENGTH_SHORT).show()

        } else {
            playerView.controllerShowTimeoutMs = StyledPlayerControlView.DEFAULT_SHOW_TIMEOUT_MS
            playerView.defaultArtwork = null
        }

        var playbackPositionMs = C.TIME_UNSET
        var currentItemIndex = C.INDEX_UNSET
        if (currentPlayer != null) {
            // clear previous player
            val playbackState = currentPlayer!!.playbackState
            if (playbackState != Player.STATE_ENDED) {

                playbackPositionMs = currentPlayer!!.contentPosition
                currentItemIndex = currentPlayer!!.currentMediaItemIndex
            }

//            currentPlayer?.stop()
//            currentPlayer?.clearMediaItems()
        }

        currentPlayer = player

        currentPlayer?.playWhenReady = true
        // currentPlayer?.setMediaItems(mediaQueue, 0, 0)
        currentPlayer?.prepare()

        playerView.player = currentPlayer
    }

}
