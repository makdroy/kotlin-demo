package mutnemom.android.kotlindemo

import android.content.ContentValues
import android.content.Context
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import androidx.annotation.RequiresApi
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.BufferedInputStream
import java.net.URL

object FileUtils {

    @RequiresApi(Build.VERSION_CODES.Q)
     fun testCreateFile(context: Context) {
        CoroutineScope(Dispatchers.IO).launch {
//                val imageMimeType = "image/*"
//                val pdfMimeType = "application/pdf"
            val epubMimeType = "application/epub+zip"

            val url =
                "https://s3-ap-southeast-1.amazonaws.com/htiebook.content/contents/sample/epub/70c2fc3d-1abb-4be6-b915-0e173b65a71b.epub"
            val contentValues = ContentValues().apply {
                put(MediaStore.MediaColumns.DISPLAY_NAME, "loadingEpub.epub")
                put(MediaStore.MediaColumns.MIME_TYPE, epubMimeType)
                put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS)
            }

            val resolver = context.contentResolver
            val uri = resolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, contentValues)

            if (uri != null) {
                var count: Int
                val data = ByteArray(1024 * 2)
                BufferedInputStream(URL(url).openStream(), 1024 * 2).use { input ->
                    resolver.openOutputStream(uri)?.use { output ->
                        count = input.read(data)

                        while (count != -1) {
                            output.write(data, 0, count)
                            count = input.read(data)
                        }
                    }
                }
            }
        }
    }

}
