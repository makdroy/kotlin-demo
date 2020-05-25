package mutnemom.android.kotlindemo

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import kotlinx.android.synthetic.main.activity_main.*
import mutnemom.android.kotlindemo.animations.transitions.TransitionsActivity
import mutnemom.android.kotlindemo.bottomnav.BottomNavActivity
import mutnemom.android.kotlindemo.custom.CustomViewActivity
import mutnemom.android.kotlindemo.datetime.DateTimeActivity
import mutnemom.android.kotlindemo.draggable.DragViewActivity
import mutnemom.android.kotlindemo.encrypt.AES256Activity
import mutnemom.android.kotlindemo.fragments.AboutFragmentActivity
import mutnemom.android.kotlindemo.gesture.GestureActivity
import mutnemom.android.kotlindemo.model.DownloadModel
import mutnemom.android.kotlindemo.room.RoomCoroutinesActivity
import mutnemom.android.kotlindemo.screenshot.ScreenshotActivity
import mutnemom.android.kotlindemo.search.SearchActivity
import mutnemom.android.kotlindemo.services.DownloadFileService
import mutnemom.android.kotlindemo.toggle.SwitchActivity
import mutnemom.android.kotlindemo.tts.TextToSpeechActivity

class MainActivity :
    AppCompatActivity(),
    View.OnClickListener {

    private val broadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            intent?.also {
                if (it.action == "message_progress") {
                    val downloadModel =
                        intent.getParcelableExtra<DownloadModel>("download") ?: return

                    progressDownload?.progress = downloadModel.progress
                    if (downloadModel.progress == 100) {
                        txtProgressDownload?.text = getString(R.string.txt_download_file_complete)
                    } else {
                        txtProgressDownload?.text = String.format(
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
        setContentView(R.layout.activity_main)

        btnRecyclerView?.setOnClickListener(this)
        btnProgressBar?.setOnClickListener(this)
        btnAnimations?.setOnClickListener(this)
        btnScreenshot?.setOnClickListener(this)
        btnCustomView?.setOnClickListener(this)
        btnBottomNav?.setOnClickListener(this)
        btnWebSocket?.setOnClickListener(this)
        btnDateTime?.setOnClickListener(this)
        btnDownload?.setOnClickListener(this)
        btnGesture?.setOnClickListener(this)
        btnWebView?.setOnClickListener(this)
        btnButton?.setOnClickListener(this)
        btnAes256?.setOnClickListener(this)
        btnSwitch?.setOnClickListener(this)
        btnSearch?.setOnClickListener(this)

        registerReceiver()

        btnFragmentChapter?.setOnClickListener { openFragmentChapterPage() }
        btnRoomCoroutines?.setOnClickListener { openRoomCoroutinesPage() }
        btnDragView?.setOnClickListener { openDragViewPage() }
        btnTts?.setOnClickListener { openTtsPage() }
    }

    override fun onClick(v: View) {
        when (v.id) {
            R.id.btnRecyclerView -> startActivity(Intent(this, RecyclerViewActivity::class.java))
            R.id.btnProgressBar -> startActivity(Intent(this, ProgressBarActivity::class.java))
            R.id.btnAnimations -> startActivity(Intent(this, TransitionsActivity::class.java))
            R.id.btnCustomView -> startActivity(Intent(this, CustomViewActivity::class.java))
            R.id.btnScreenshot -> startActivity(Intent(this, ScreenshotActivity::class.java))
            R.id.btnBottomNav -> startActivity(Intent(this, BottomNavActivity::class.java))
            R.id.btnWebSocket -> startActivity(Intent(this, WebSocketActivity::class.java))
            R.id.btnDownload -> startService(Intent(this, DownloadFileService::class.java))
            R.id.btnDateTime -> startActivity(Intent(this, DateTimeActivity::class.java))
            R.id.btnGesture -> startActivity(Intent(this, GestureActivity::class.java))
            R.id.btnWebView -> startActivity(Intent(this, WebViewActivity::class.java))
            R.id.btnButton -> startActivity(Intent(this, ButtonActivity::class.java))
            R.id.btnAes256 -> startActivity(Intent(this, AES256Activity::class.java))
            R.id.btnSwitch -> startActivity(Intent(this, SwitchActivity::class.java))
            R.id.btnSearch -> startActivity(Intent(this, SearchActivity::class.java))
        }
    }

    private fun registerReceiver() {
        LocalBroadcastManager.getInstance(this).apply {
            val intentFilter = IntentFilter()
            intentFilter.addAction("message_progress")
            registerReceiver(broadcastReceiver, intentFilter)
        }
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

}
