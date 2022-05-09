package mutnemom.android.kotlindemo.storage

import android.content.Intent
import android.database.Cursor
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.DocumentsContract
import android.provider.OpenableColumns
import android.util.Log
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.net.toUri
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import mutnemom.android.kotlindemo.databinding.ActivityDataAndFileDemoBinding
import okio.IOException
import java.io.BufferedReader
import java.io.InputStreamReader

class DataAndFileDemoActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDataAndFileDemoBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityDataAndFileDemoBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setEvent()
    }

    private fun setEvent() {
        with(binding) {
            btnCreateFileSAF.setOnClickListener { createSamplePdfFileWithSAF() }
            btnOpenFileSAF.setOnClickListener { openSamplePdfFileWithSAF() }
        }
    }

    private val createFileLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            if (it.resultCode == RESULT_OK) {
                it.data?.data?.also { uri ->
                    Log.e("tt", "-> create sample pdf: $uri")
                }
            }
        }

    private fun createSamplePdfFileWithSAF() {
        val intent = Intent(Intent.ACTION_CREATE_DOCUMENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            type = "application/pdf"
            putExtra(Intent.EXTRA_TITLE, "sample-created.pdf")

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {

                // Optionally, specify a URI for the directory that should be opened in
                // the system file picker before your app creates the document.

                "content://com.android.externalstorage.documents/document/primary:DCIM/"
                    .toUri()
                    .also { putExtra(DocumentsContract.EXTRA_INITIAL_URI, it) }
            }
        }

        createFileLauncher.launch(intent)
    }

    private val openFileLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            if (it.resultCode == RESULT_OK) {
                it.data?.data?.also { uri ->
                    Log.e("tt", "-> open file: ${uri.lastPathSegment}")

                    dumpMetaData(uri)

                    CoroutineScope(Dispatchers.IO).launch {
                        val text = readTextFromUri(uri)
                        Log.e("tt", "-> data: $text")
                    }
                }
            }
        }

    private fun openSamplePdfFileWithSAF() {
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            type = "application/pdf"

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {

                // Optionally, specify a URI for the file that should appear in the
                // system file picker when it loads.

                "content://com.android.externalstorage.documents/document/primary:DCIM/"
                    .toUri()
                    .also { putExtra(DocumentsContract.EXTRA_INITIAL_URI, it) }
            }
        }

        openFileLauncher.launch(intent)
    }

    private fun dumpMetaData(uri: Uri) {
        // The query, because it only applies to a single document, returns only
        // one row. There's no need to filter, sort, or select fields,
        // because we want all fields for one document.
        val cursor: Cursor? = contentResolver.query(
            uri, null, null, null, null, null
        )

        cursor?.use {
            // moveToFirst() returns false if the cursor has 0 rows
            // Very handy for "if there's anything to look at, look at it" conditions.
            if (it.moveToFirst()) {

                // Note it's called "Display Name". This is provider-specific,
                // and might not necessarily be the file name.
                var columnIndex = it.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                if (columnIndex > 0) {
                    val displayName = it.getString(columnIndex)
                    Log.e("tt", "-> display name: $displayName")
                }

                columnIndex = it.getColumnIndex(OpenableColumns.SIZE)
                // If the size is unknown, the value stored is null.
                // But because an int can't be null,
                // the behavior is implementation-specific, and unpredictable.
                // So as a rule, check if it's null before assigning to an int.
                // This will happen often: The storage API allows for remote files, whose
                // size might not be locally known.

                val size = if (!it.isNull(columnIndex)) it.getString(columnIndex) else "Unknown"
                // Technically the column stores an int, but cursor.getString()
                // will do the conversion automatically.

                Log.e("tt", "-> file size: $size")
            }
        }
    }

    @Suppress("UNUSED")
    @Throws(IOException::class)
    private fun getBitmapFromUri(uri: Uri): Bitmap {
        val parcelFileDescriptor = contentResolver.openFileDescriptor(uri, "r")
        val fileDescriptor = parcelFileDescriptor?.fileDescriptor
        val image = BitmapFactory.decodeFileDescriptor(fileDescriptor)
        parcelFileDescriptor?.close()

        // After you open the bitmap, you can display it in an ImageView.
        return image
    }

    @Throws(IOException::class)
    private fun readTextFromUri(uri: Uri): String {
        val stringBuilder = StringBuilder()
        contentResolver.openInputStream(uri)?.use { ips ->
            BufferedReader(InputStreamReader(ips)).use { reader ->
                var line: String? = reader.readLine()
                while (line != null) {
                    stringBuilder.append(line)
                    line = reader.readLine()
                }
            }
        }

        return stringBuilder.toString()
    }

}
