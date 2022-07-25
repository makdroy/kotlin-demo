package mutnemom.android.kotlindemo.reader.pdf

import android.graphics.Bitmap
import android.graphics.pdf.PdfRenderer
import android.os.Bundle
import android.os.ParcelFileDescriptor
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import kotlinx.coroutines.*
import mutnemom.android.kotlindemo.databinding.ActivityPdfReaderBinding
import java.io.File
import java.io.InputStream
import java.io.RandomAccessFile
import java.nio.channels.FileChannel

class PdfReaderActivity : AppCompatActivity() {

    private lateinit var binding: ActivityPdfReaderBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityPdfReaderBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setEvent()
    }

    private fun setEvent() {
        binding.apply {
            btnClose.setOnClickListener {  }
            btnOpen.setOnClickListener {
//                openPdf()
//                decryptAES("sample3-encrypted.pdf")

                val ips = assets.open("sample1.pdf")
                val pdfFile = File(cacheDir, "sample.pdf")

                // convert input stream to File
                ips.toFile(pdfFile.absolutePath)

                openPdfFile(pdfFile)
            }
        }
    }

    private fun InputStream.toFile(path: String) {
        use { input ->
            File(path).outputStream().use { input.copyTo(it) }
        }
    }

    private fun openPdf() {
        // val testPdfKey = "64cffc9c9cf9442aefe1dbb424d06355"
        try {
            val ips = assets.open("sample2.pdf")
            // val ips = assets.open("sample3-encrypted.pdf")
            val pdfFile = File(cacheDir, "sample.pdf")

            // convert input stream to File
            ips.toFile(pdfFile.absolutePath)

            // create renderer
            val fileDescriptor =
                ParcelFileDescriptor.open(pdfFile, ParcelFileDescriptor.MODE_READ_ONLY)
            val pdfRenderer = PdfRenderer(fileDescriptor)

            // checking for valid pdf page count
            val pageCount = pdfRenderer.pageCount
            Log.e("tt", "-> pdf page count: $pageCount")

            // open page
            val pageIndex = 4
            var page = pdfRenderer.openPage(pageIndex)

            // calculate height
            // val height = page.height
            val height = resources.displayMetrics.densityDpi * page.height / 72

            // calculate width
            // val width = page.width
            val width = resources.displayMetrics.densityDpi * page.width / 72

            // render the page to bitmap
            var bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
            page.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)
            page.close()

            // use the rendered bitmap
            binding.previewContainer.setImageBitmap(bitmap)

            // for page 2
            page = pdfRenderer.openPage(pageIndex.inc())
            bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
            page.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)
            page.close()
            binding.previewContainer2.setImageBitmap(bitmap)


            CoroutineScope(Dispatchers.IO).launch {
                delay(2000L)
                withContext(Dispatchers.Main) {
                    // close the renderer
                    pdfRenderer.close()
                }
            }
        } catch (e: Throwable) {
            e.printStackTrace()
        }
    }

    /* Open a specific PDF file.
     */
    private fun openPdfFile(file: File, password: String? = null) {
        val raf = RandomAccessFile(file, "r")

        // extract a file channel
        val channel = raf.channel

        // memory-map a byte-buffer
        val mappedByteBuffer = channel.map(FileChannel.MapMode.READ_ONLY, 0, channel.size())

        // create a PDFFile from the data
        val pdfFile = password
            ?.let { PdfFile(mappedByteBuffer, PdfPassword(it)) }
            ?: PdfFile(mappedByteBuffer)

        Log.e("total page count", "-> ${pdfFile.pages}")

        val pageNumber = 0
        pdfFile.getPage(pageNumber)?.also {
//            val bitmap = it.getImage(1f, null, true, true)
//
//            Log.e("tt", "-> render page: $pageNumber")
//            binding.previewContainer.setImageBitmap(bitmap)
        }
    }

}
