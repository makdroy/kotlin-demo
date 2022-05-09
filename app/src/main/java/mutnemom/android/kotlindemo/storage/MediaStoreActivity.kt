package mutnemom.android.kotlindemo.storage

import android.content.ContentUris
import android.content.ContentValues
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import mutnemom.android.kotlindemo.databinding.ActivityMediaStoreBinding
import okio.IOException

class MediaStoreActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMediaStoreBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMediaStoreBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setEvent()
    }

    private fun setEvent() {
        binding.apply {
            btnCreate.setOnClickListener {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    createNewFileWithMediaStoreApi()
                }
            }

            btnRead.setOnClickListener {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    readFileWithMediaStoreApi()
                }
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    private fun createNewFileWithMediaStoreApi() {
        try {
            val values = ContentValues()
            values.put(MediaStore.MediaColumns.DISPLAY_NAME, "MediaStoreApi.pdf")
            values.put(MediaStore.MediaColumns.MIME_TYPE, "application/pdf")
            values.put(
                MediaStore.MediaColumns.RELATIVE_PATH,
                "${Environment.DIRECTORY_DOCUMENTS}/test-media-store/"
            )

            val uri = contentResolver.insert(MediaStore.Files.getContentUri("external"), values)
            with(uri ?: return) {
                contentResolver.openOutputStream(this)
                    ?.use { it.write("My name is Hytexts.".toByteArray()) }

                Log.e("tt", "-> created")
            }

            Log.e("tt", "-> uri: $uri")

        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    private fun readFileWithMediaStoreApi() {
        try {
            val contentUri = MediaStore.Files.getContentUri("external")
            val selection = MediaStore.MediaColumns.RELATIVE_PATH + "=?"
            val selectionArgs = arrayOf("${Environment.DIRECTORY_DOCUMENTS}/test-media-store/")
            contentResolver
                .query(contentUri, null, selection, selectionArgs, null)
                ?.use {
                    if (it.count == 0) {
                        Log.e("tt", "-> is empty directory")
                    } else {
                        while (it.moveToNext()) {
                            var columnIndex =
                                it.getColumnIndex(MediaStore.MediaColumns.DISPLAY_NAME)
                            if (columnIndex >= 0) {
                                val fileName = it.getString(columnIndex)
                                if (fileName.equals("MediaStoreApi.pdf")) {
                                    columnIndex = it.getColumnIndex(MediaStore.MediaColumns._ID)
                                    val id = it.getLong(columnIndex)
                                    ContentUris.withAppendedId(contentUri, id)
                                        .also { uri -> Log.e("tt", "-> uri: $uri") }
                                }
                            }
                        }
                    }
                }

        } catch (e: Throwable) {
            e.printStackTrace()
        }
    }

}
