package mutnemom.android.kotlindemo

import android.Manifest
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.graphics.SurfaceTexture
import android.hardware.camera2.*
import android.os.Bundle
import android.util.Log
import android.util.Size
import android.view.Surface
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import kotlinx.android.synthetic.main.activity_main.*
import mutnemom.android.kotlindemo.draggable.DragViewActivity
import mutnemom.android.kotlindemo.fragments.AboutFragmentActivity
import mutnemom.android.kotlindemo.model.DownloadModel
import mutnemom.android.kotlindemo.services.DownloadFileService
import mutnemom.android.kotlindemo.tts.TextToSpeechActivity

class MainActivity :
    AppCompatActivity(),
    View.OnClickListener {

    companion object {
        private val LOG_TAG = MainActivity::class.java.simpleName
    }

    private lateinit var cameraDevice: CameraDevice
    private lateinit var captureRequestBuilder: CaptureRequest.Builder
    private lateinit var cameraCaptureSessions: CameraCaptureSession
    private lateinit var imageDimension: Size


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

        btnWebView.setOnClickListener(this)
        btnRecyclerView.setOnClickListener(this)
        btnButton.setOnClickListener(this)
        btnWebSocket?.setOnClickListener(this)
        btnProgressBar?.setOnClickListener(this)
        btnCamera?.setOnClickListener(this)

        registerReceiver()
        btnDownload?.setOnClickListener { startDownload() }
        txtFragmentChapter?.setOnClickListener { openFragmentChapterPage() }
        txtDragView?.setOnClickListener { openDragViewPage() }
        txtTts?.setOnClickListener { openTtsPage() }
    }

    override fun onClick(v: View) {
        when (v.id) {
            R.id.btnWebView -> startActivity(Intent(this, WebViewActivity::class.java))
            R.id.btnRecyclerView -> startActivity(Intent(this, RecyclerViewActivity::class.java))
            R.id.btnButton -> startActivity(Intent(this, ButtonActivity::class.java))
            R.id.btnWebSocket -> startActivity(Intent(this, WebSocketActivity::class.java))
            R.id.btnProgressBar -> startActivity(Intent(this, ProgressBarActivity::class.java))

            R.id.btnCamera -> {
                val cameraManager = getSystemService(Context.CAMERA_SERVICE) as? CameraManager
                cameraManager?.apply {
                    Log.e(LOG_TAG, "-> available camera: ${cameraIdList.size}.")

                    when {
                        cameraIdList.isNotEmpty() -> {
                            val callback = object : CameraDevice.StateCallback() {
                                override fun onDisconnected(camera: CameraDevice) {
                                    Log.e(LOG_TAG, "-> onDisconnected cameraId: ${camera.id}")
                                }

                                override fun onError(camera: CameraDevice, error: Int) {
                                    Log.e(LOG_TAG, "-> onError cameraId: ${camera.id}")
                                }

                                override fun onOpened(camera: CameraDevice) {
                                    Log.e(LOG_TAG, "-> onOpened cameraId: ${camera.id}")
                                    cameraDevice = camera
                                    createCameraPreview()
                                }

                                override fun onClosed(camera: CameraDevice) {
                                    Log.e(LOG_TAG, "-> onClosed cameraId: ${camera.id}")
                                    super.onClosed(camera)
                                }
                            }

                            if (ActivityCompat.checkSelfPermission(
                                    this@MainActivity,
                                    Manifest.permission.CAMERA
                                )
                                == PackageManager.PERMISSION_GRANTED
                            ) {

                                val cameraId = cameraIdList[0]
                                val characteristics = getCameraCharacteristics(cameraId)
                                characteristics
                                    .get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP)
                                    ?.also {
                                        imageDimension =
                                            it.getOutputSizes(SurfaceTexture::class.java)[0]
                                        openCamera(cameraId, callback, null)
                                    }
                            }
                        }
                    }
                }
            }
        }
    }

    private fun createCameraPreview() {
        try {
            textureView?.surfaceTexture?.apply {
                setDefaultBufferSize(imageDimension.width, imageDimension.height)
                val surface = Surface(this)
                captureRequestBuilder =
                    cameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW)
                captureRequestBuilder.addTarget(surface)
                cameraDevice.createCaptureSession(
                    listOf(surface),
                    object : CameraCaptureSession.StateCallback() {
                        override fun onConfigured(cameraCaptureSession: CameraCaptureSession) {
                            //The camera is already closed
                            if (this@MainActivity::cameraDevice.isInitialized.not()) {
                                return
                            }
                            // When the session is ready, we start displaying the preview.
                            cameraCaptureSessions = cameraCaptureSession
                            updatePreview()
                        }

                        override fun onConfigureFailed(cameraCaptureSession: CameraCaptureSession) {
                            Toast.makeText(
                                this@MainActivity,
                                "Configuration change",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    },
                    null
                )
            }
        } catch (e: CameraAccessException) {
            e.printStackTrace()
        }

    }

    private fun updatePreview() {
        if (this::cameraDevice.isInitialized.not()) {
            Log.e(LOG_TAG, "updatePreview error, return")
        }

        captureRequestBuilder.set(CaptureRequest.CONTROL_MODE, CameraMetadata.CONTROL_MODE_AUTO)

        try {
            cameraCaptureSessions.setRepeatingRequest(
                captureRequestBuilder.build(),
                null,
                null /* mBackgroundHandler */
            )
        } catch (e: CameraAccessException) {
            e.printStackTrace()
        }

    }

    private fun registerReceiver() {
        LocalBroadcastManager.getInstance(this).apply {
            val intentFilter = IntentFilter()
            intentFilter.addAction("message_progress")

            registerReceiver(broadcastReceiver, intentFilter)
        }
    }

    private fun startDownload() {
        Intent(this, DownloadFileService::class.java).apply {
            startService(this)
        }
    }

    private fun openFragmentChapterPage() {
        Intent(this, AboutFragmentActivity::class.java).apply {
            startActivity(this)
        }
    }

    private fun openDragViewPage() {
        Intent(this, DragViewActivity::class.java)
            .apply { startActivity(this) }
    }

    private fun openTtsPage() {
        Intent(this, TextToSpeechActivity::class.java)
            .apply { startActivity(this) }
    }

}
