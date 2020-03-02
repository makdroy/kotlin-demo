package mutnemom.android.kotlindemo.readium

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import mutnemom.android.kotlindemo.R

class EpubReaderActivity : AppCompatActivity() {

    private lateinit var screenReader: R2ScreenReader

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_epub_reader)
    }

}
