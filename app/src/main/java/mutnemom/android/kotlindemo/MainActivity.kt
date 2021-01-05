package mutnemom.android.kotlindemo

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import mutnemom.android.kotlindemo.animations.text.ExpandableTextViewActivity
import mutnemom.android.kotlindemo.animations.transitions.TransitionsActivity
import mutnemom.android.kotlindemo.bottomnav.BottomNavActivity
import mutnemom.android.kotlindemo.bottomsheet.BottomSheetActivity
import mutnemom.android.kotlindemo.custom.CustomViewActivity
import mutnemom.android.kotlindemo.databinding.ActivityMainBinding
import mutnemom.android.kotlindemo.datetime.DateTimeActivity
import mutnemom.android.kotlindemo.dialog.DialogActivity
import mutnemom.android.kotlindemo.draggable.DragViewActivity
import mutnemom.android.kotlindemo.encrypt.AES256Activity
import mutnemom.android.kotlindemo.extensions.toast
import mutnemom.android.kotlindemo.fragments.AboutFragmentActivity
import mutnemom.android.kotlindemo.gesture.GestureActivity
import mutnemom.android.kotlindemo.image.CoilActivity
import mutnemom.android.kotlindemo.image.ShapeableImageViewActivity
import mutnemom.android.kotlindemo.location.LocationDemoActivity
import mutnemom.android.kotlindemo.model.DownloadModel
import mutnemom.android.kotlindemo.notification.NotificationActivity
import mutnemom.android.kotlindemo.playback.ExoPlayerActivity
import mutnemom.android.kotlindemo.playback.ExoPlayerCastActivity
import mutnemom.android.kotlindemo.playback.PlaybackNotificationActivity
import mutnemom.android.kotlindemo.recyclerview.ListAdapterDemoActivity
import mutnemom.android.kotlindemo.recyclerview.RecyclerViewActivity
import mutnemom.android.kotlindemo.room.RoomCoroutinesActivity
import mutnemom.android.kotlindemo.screenshot.ScreenshotActivity
import mutnemom.android.kotlindemo.search.SearchActivity
import mutnemom.android.kotlindemo.services.DownloadFileService
import mutnemom.android.kotlindemo.storage.DataAndFileDemoActivity
import mutnemom.android.kotlindemo.storage.MediaStoreActivity
import mutnemom.android.kotlindemo.toggle.SwitchActivity
import mutnemom.android.kotlindemo.tts.TextToSpeechActivity

class MainActivity :
    AppCompatActivity(),
    View.OnClickListener {

    private lateinit var binding: ActivityMainBinding

    private val broadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            intent?.also {
                if (it.action == "message_progress") {
                    val downloadModel =
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                            intent.getParcelableExtra("download", DownloadModel::class.java)
                        } else {
                            @Suppress("DEPRECATION")
                            intent.getParcelableExtra("download")
                        }

                    downloadModel ?: return@also

                    binding.progressDownload.progress = downloadModel.progress
                    if (downloadModel.progress == 100) {
                        binding.txtProgressDownload.text =
                            getString(R.string.txt_download_file_complete)
                    } else {
                        binding.txtProgressDownload.text = String.format(
                            "Download (%d/%d) MB",
                            downloadModel.currentFileSize,
                            downloadModel.totalFileSize
                        )
                    }
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnExoPlayerCast.setOnClickListener(this)
        binding.btnNotification.setOnClickListener(this)
        binding.btnRecyclerView.setOnClickListener(this)
        binding.btnBottomSheet.setOnClickListener(this)
        binding.btnProgressBar.setOnClickListener(this)
        binding.btnAnimations.setOnClickListener(this)
        binding.btnScreenshot.setOnClickListener(this)
        binding.btnCustomView.setOnClickListener(this)
        binding.btnBottomNav.setOnClickListener(this)
        binding.btnExoPlayer.setOnClickListener(this)
        binding.btnWebSocket.setOnClickListener(this)
        binding.btnDateTime.setOnClickListener(this)
        binding.btnDownload.setOnClickListener(this)
        binding.btnGesture.setOnClickListener(this)
        binding.btnWebView.setOnClickListener(this)
        binding.btnButton.setOnClickListener(this)
        binding.btnAes256.setOnClickListener(this)
        binding.btnSwitch.setOnClickListener(this)
        binding.btnSearch.setOnClickListener(this)
        binding.btnDialog.setOnClickListener(this)
        binding.btnCoil.setOnClickListener(this)

        registerReceiver()

        binding.btnFragmentChapter.setOnClickListener { openFragmentChapterPage() }
        binding.btnRoomCoroutines.setOnClickListener { openRoomCoroutinesPage() }
        binding.btnMediaStore.setOnClickListener { openMediaStorePage() }
        binding.btnDragView.setOnClickListener { openDragViewPage() }
        binding.btnTts.setOnClickListener { openTtsPage() }

        with(binding) {
            btnPlaybackNotification.setOnClickListener { openPlaybackNotificationPage() }
            btnShapeableImageView.setOnClickListener { openShapeableImageViewPage() }
            btnExpandableTextView.setOnClickListener { openExpandableTextViewPage() }
            btnMonitorNetwork.setOnClickListener { openMonitorNetworkStatePage() }
            btnListAdapter.setOnClickListener { openListAdapterDemoPage() }
            btnCounterFab.setOnClickListener { increaseCounterFabNumber() }
            btnTabLayout.setOnClickListener { openTabLayoutDemoPage() }
            btnDataFile.setOnClickListener { openDataAndFileDemoPage() }
            btnLocalize.setOnClickListener { openPerAppLanguagePage() }
            btnDebounce.setOnClickListener { openDebouncePage() }
            btnLocation.setOnClickListener { openLocationDemoPage() }
        }
    }

    override fun onClick(v: View) {
        when (v.id) {
            R.id.btnExoPlayerCast -> startActivity(Intent(this, ExoPlayerCastActivity::class.java))
            R.id.btnNotification -> startActivity(Intent(this, NotificationActivity::class.java))
            R.id.btnRecyclerView -> startActivity(Intent(this, RecyclerViewActivity::class.java))
            R.id.btnBottomSheet -> startActivity(Intent(this, BottomSheetActivity::class.java))
            R.id.btnProgressBar -> startActivity(Intent(this, ProgressBarActivity::class.java))
            R.id.btnAnimations -> startActivity(Intent(this, TransitionsActivity::class.java))
            R.id.btnCustomView -> startActivity(Intent(this, CustomViewActivity::class.java))
            R.id.btnScreenshot -> startActivity(Intent(this, ScreenshotActivity::class.java))
            R.id.btnBottomNav -> startActivity(Intent(this, BottomNavActivity::class.java))
            R.id.btnExoPlayer -> startActivity(Intent(this, ExoPlayerActivity::class.java))
            R.id.btnWebSocket -> startActivity(Intent(this, WebSocketActivity::class.java))
            R.id.btnDownload -> startService(Intent(this, DownloadFileService::class.java))
            R.id.btnDateTime -> startActivity(Intent(this, DateTimeActivity::class.java))
            R.id.btnGesture -> startActivity(Intent(this, GestureActivity::class.java))
            R.id.btnWebView -> startActivity(Intent(this, WebViewActivity::class.java))
            R.id.btnButton -> startActivity(Intent(this, ButtonActivity::class.java))
            R.id.btnAes256 -> startActivity(Intent(this, AES256Activity::class.java))
            R.id.btnSwitch -> startActivity(Intent(this, SwitchActivity::class.java))
            R.id.btnSearch -> startActivity(Intent(this, SearchActivity::class.java))
            R.id.btnDialog -> startActivity(Intent(this, DialogActivity::class.java))
            R.id.btnCoil -> startActivity(Intent(this, CoilActivity::class.java))
        }
    }

    private fun registerReceiver() {
        LocalBroadcastManager.getInstance(this).apply {
            val intentFilter = IntentFilter()
            intentFilter.addAction("message_progress")
            registerReceiver(broadcastReceiver, intentFilter)
        }
    }

    private fun openExpandableTextViewPage() {
        Intent(this, ExpandableTextViewActivity::class.java)
            .apply { startActivity(this) }
    }

    private fun openFragmentChapterPage() {
        Intent(this, AboutFragmentActivity::class.java)
            .apply { startActivity(this) }
    }

    private fun openDragViewPage() {
        Intent(this, DragViewActivity::class.java)
            .apply { startActivity(this) }
    }

    private fun openTtsPage() {
        Intent(this, TextToSpeechActivity::class.java)
            .apply { startActivity(this) }
    }

    private fun openRoomCoroutinesPage() {
        Intent(this, RoomCoroutinesActivity::class.java)
            .apply { startActivity(this) }
    }

    private fun openPlaybackNotificationPage() {
        Intent(this, PlaybackNotificationActivity::class.java)
            .apply { startActivity(this) }
    }

    private fun openMediaStorePage() {
        Intent(this, MediaStoreActivity::class.java)
            .apply { startActivity(this) }
    }

    private fun openMonitorNetworkStatePage() {
        Intent(this, NetworkStateActivity::class.java)
            .apply { startActivity(this) }
    }

    private fun openShapeableImageViewPage() {
        Intent(this, ShapeableImageViewActivity::class.java)
            .apply { startActivity(this) }
    }

    private fun openListAdapterDemoPage() {
        Intent(this, ListAdapterDemoActivity::class.java)
            .apply { startActivity(this) }
    }

    private fun openDataAndFileDemoPage() {
        Intent(this, DataAndFileDemoActivity::class.java)
            .apply { startActivity(this) }
    }

    private fun openPerAppLanguagePage() {
        Intent(this, PerAppLanguageActivity::class.java)
            .apply { startActivity(this) }
    }

    private fun openDebouncePage() {
        Intent(this, DebounceClickActivity::class.java)
            .apply { startActivity(this) }
    }

    private fun openLocationDemoPage() {
        Intent(this, LocationDemoActivity::class.java)
            .apply { startActivity(this) }
    }

    private fun openTabLayoutDemoPage() {
        Intent(this, TabLayoutDemoActivity::class.java)
            .apply { startActivity(this) }
    }

    private fun increaseCounterFabNumber() {
        val isIncreasingMode = true
        if (isIncreasingMode) {
            binding.btnCounterFab.increase()
        } else {
            binding.btnCounterFab.decrease()
        }

        toast("count: ${binding.btnCounterFab.count}")
    }

}
