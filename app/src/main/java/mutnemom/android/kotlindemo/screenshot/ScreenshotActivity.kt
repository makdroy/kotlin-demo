package mutnemom.android.kotlindemo.screenshot

import android.Manifest
import android.content.ContentValues
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.media.MediaScannerConnection
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.os.Handler
import android.provider.MediaStore
import android.text.format.DateFormat
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import mutnemom.android.kotlindemo.BuildConfig
import mutnemom.android.kotlindemo.R
import mutnemom.android.kotlindemo.extensions.requestPermission
import mutnemom.android.kotlindemo.extensions.toast
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStream
import java.util.*

class ScreenshotActivity : AppCompatActivity() {

    private val storagePermCode by lazy { 221 }
    private val storagePerm by lazy { Manifest.permission.WRITE_EXTERNAL_STORAGE }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_screenshot)
    }

    override fun onResume() {
        super.onResume()

        Handler().postDelayed({
            when (ContextCompat.checkSelfPermission(this, storagePerm)) {
                PackageManager.PERMISSION_GRANTED -> takeScreenshot()
                else -> requestPermission(storagePerm, storagePermCode)
            }
        }, 1000)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        when (requestCode) {
            storagePermCode -> handleStoragePermResponse(grantResults)
            else -> super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        }
    }

    private fun handleStoragePermResponse(grantResults: IntArray) {
        if (hasPermission(grantResults)) {
            takeScreenshot()
        }
    }

    private fun hasPermission(grantResults: IntArray): Boolean {
        return grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED
    }

    @Suppress("deprecation")
    private fun takeScreenshot() = CoroutineScope(Dispatchers.IO).launch {
        val appName = resources.getString(R.string.app_name)
        val view = window.decorView.rootView
        val now = Date()
        val name = DateFormat.format("yyyy-MM-dd_hh:mm:ss", now)

        val bitmap = Bitmap.createBitmap(view.width, view.height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        view.draw(canvas)

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                val contentValues = ContentValues()
                contentValues.put(MediaStore.MediaColumns.DISPLAY_NAME, name.toString())
                contentValues.put(MediaStore.MediaColumns.MIME_TYPE, "image/png")
                contentValues.put(
                    MediaStore.MediaColumns.RELATIVE_PATH,
                    "${Environment.DIRECTORY_PICTURES}/$appName"
                )

                contentResolver.apply {
                    insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)
                        ?.let { uri ->
                            openOutputStream(uri).use { saveScreenshot(bitmap, it, uri.path) }
                        }
                }
            } else {
                val dirPath = "${Environment.getExternalStorageDirectory()}/Pictures/$appName"
                val dir = File(dirPath)
                if (!dir.exists()) dir.mkdirs()
                val file = File("${dir.absolutePath}/$name.png")

                FileOutputStream(file).use { saveScreenshot(bitmap, it, file.absolutePath) }
            }

        } catch (e: Throwable) {
            if (BuildConfig.DEBUG) e.printStackTrace()
        }
    }

    private suspend fun saveScreenshot(bitmap: Bitmap, ops: OutputStream?, filePath: String?) {
        filePath?.let {
            try {
                val mime = arrayOf("image/png")
                bitmap.compress(Bitmap.CompressFormat.PNG, 70, ops)
                withContext(Dispatchers.Main) { toast("บันทึกหน้าจอแล้ว") }
                MediaScannerConnection.scanFile(this, arrayOf(filePath), mime, null)
            } catch (e: Throwable) {
                if (BuildConfig.DEBUG) e.printStackTrace()
            }
        } ?: run { Log.e(ScreenshotActivity::class.java.simpleName, "-> file path not found") }
    }

}
