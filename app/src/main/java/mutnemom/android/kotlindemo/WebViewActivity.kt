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
        val samplePdf = "https://www.w3.org/WAI/ER/tests/xhtml/testfiles/resources/pdf/dummy.pdf"
        val sampleWord = "https://file-examples.com/wp-content/uploads/2017/02/file-sample_100kB.doc"
        val sampleDocx = "https://file-examples.com/wp-content/uploads/2017/02/file-sample_100kB.docx"
        val sampleImage = "https://s3-ap-southeast-1.amazonaws.com/htimember/images/profileImage/small/cf1062a5-6fab-4680-9fef-592132399988.jpg"

        webContainer?.loadUrl(sampleUrl)
        btnLoadPdf?.setOnClickListener {
//            webContainer?.loadUrl(sampleImage)
            webContainer?.loadUrl("$googleDocPrefix$sampleDocx")
        }
    }
}
