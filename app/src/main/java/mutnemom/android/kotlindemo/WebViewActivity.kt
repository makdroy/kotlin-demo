package mutnemom.android.kotlindemo

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import mutnemom.android.kotlindemo.databinding.ActivityWebViewBinding

class WebViewActivity : AppCompatActivity() {

    private lateinit var binding: ActivityWebViewBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityWebViewBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val sampleUrl = "https://stackoverflow.com/"
        val googleDocPrefix = "https://docs.google.com/gview?embedded=true&url="
        val sampleDocx =
            "https://file-examples.com/wp-content/uploads/2017/02/file-sample_100kB.docx"

        binding.webContainer.loadUrl(sampleUrl)
        binding.btnLoadPdf.setOnClickListener {
//            webContainer?.loadUrl(sampleImage)
            binding.webContainer.loadUrl("$googleDocPrefix$sampleDocx")
        }
    }
}
