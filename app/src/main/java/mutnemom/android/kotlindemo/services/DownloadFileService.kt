package mutnemom.android.kotlindemo.services

import android.app.IntentService
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import mutnemom.android.kotlindemo.R
import mutnemom.android.kotlindemo.model.DownloadModel
import okhttp3.ResponseBody
import retrofit2.Retrofit
import java.io.BufferedInputStream
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import kotlin.math.pow
import kotlin.math.roundToInt

class DownloadFileService : IntentService("Download Service") {

    private lateinit var notificationManager: NotificationManager
    private lateinit var notificationBuilder: NotificationCompat.Builder

    private var totalFileSize = 0

    override fun onHandleIntent(intent: Intent?) {
        notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationBuilder = NotificationCompat.Builder(this, "Download")
            .setSmallIcon(R.mipmap.ic_launcher_round)
            .setContentTitle("Download")
            .setContentText("Downloading File")
            .setAutoCancel(true)

        notificationManager.notify(0, notificationBuilder.build())

        initDownload()
    }

    override fun onTaskRemoved(rootIntent: Intent?) {
        notificationManager.cancel(0)
    }

    private fun initDownload() {
        val retrofit = Retrofit.Builder()
            .baseUrl("https://download.learn2crack.com/")
            .build()

        retrofit.create(RetrofitInterface::class.java).apply {
            try {
                downloadFile().execute().body()?.apply {
                    downloadFile(this)
                }

            } catch (e: Throwable) {
                e.printStackTrace()
            }
        }

    }

    @Throws(IOException::class)
    private fun downloadFile(body: ResponseBody) {
        var count: Int
        val data = ByteArray(1024 * 4)
        val fileSize = body.contentLength()

        BufferedInputStream(body.byteStream(), 1024 * 8).use { bis ->
            val storageDir = getExternalFilesDir(null)
            val outputFile = File(storageDir, "file.zip")
            Log.e("tt", "-> path: ${outputFile.absolutePath}")

            FileOutputStream(outputFile).use { fos ->
                var total = 0
                val startTime = System.currentTimeMillis()
                var timeCount = 1

                count = bis.read(data)
                while (count != -1) {
                    total += count
                    totalFileSize = (fileSize / (1024.0.pow(2.0))).toInt()
                    val current = (total / 1024.0.pow(2.0)).roundToInt()

                    val process = ((total * 100) / fileSize).toInt()
                    val currentTime = System.currentTimeMillis() - startTime
                    val download = DownloadModel(totalFileSize = totalFileSize)

                    if (currentTime > 1_000 * timeCount) {
                        download.currentFileSize = current
                        download.progress = process
                        sendNotification(download)
                        timeCount++
                    }

                    fos.write(data, 0, count)
                    count = bis.read(data)
                }

                onDownloadComplete()
            }
        }
    }

    private fun sendNotification(downloadModel: DownloadModel) {
        sendIntent(downloadModel)
        notificationBuilder.setProgress(100, downloadModel.progress, false)
        notificationBuilder.setContentText("Downloading file ${downloadModel.currentFileSize}/$totalFileSize MB")
        notificationManager.notify(0, notificationBuilder.build())
    }

    private fun sendIntent(downloadModel: DownloadModel) {
        Intent("message_progress").apply {
            putExtra("download", downloadModel)
            LocalBroadcastManager
                .getInstance(this@DownloadFileService)
                .sendBroadcast(this)
        }
    }

    private fun onDownloadComplete() {
        DownloadModel(progress = 100).also {
            sendIntent(it)
        }

        notificationManager.cancel(0)
        notificationBuilder.setProgress(0, 0, false)
        notificationBuilder.setContentText("File Downloaded")
        notificationManager.notify(0, notificationBuilder.build())
    }
}