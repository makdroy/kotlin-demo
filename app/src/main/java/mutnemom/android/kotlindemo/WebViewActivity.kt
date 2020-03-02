package mutnemom.android.kotlindemo

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_web_view.*

class WebViewActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_web_view)

        val sampleUrl = "https://stackoverflow.com/"
        val googleDocPrefix = "https://docs.google.com/gview?embedded=true&url="
        val sampleDocx = "https://file-examples.com/wp-content/uploads/2017/02/file-sample_100kB.docx"

        webContainer?.loadUrl(sampleUrl)
        btnLoadPdf?.setOnClickListener {
//            webContainer?.loadUrl(sampleImage)
            webContainer?.loadUrl("$googleDocPrefix$sampleDocx")
        }
    }
}
