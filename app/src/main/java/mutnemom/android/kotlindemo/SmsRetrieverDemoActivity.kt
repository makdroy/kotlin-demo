package mutnemom.android.kotlindemo

import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.auth.api.phone.SmsRetriever
import com.google.android.gms.auth.api.phone.SmsRetrieverClient
import com.google.android.gms.tasks.Task
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import mutnemom.android.kotlindemo.databinding.ActivitySmsRetrieverDemoBinding
import mutnemom.android.kotlindemo.extensions.toast
import mutnemom.android.kotlindemo.utils.SignatureUtils

class SmsRetrieverDemoActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySmsRetrieverDemoBinding

    private var smsRetrieverClient: SmsRetrieverClient? = null
    private var task: Task<Void>? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivitySmsRetrieverDemoBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setEvent()
    }

    private fun setEvent() {
        binding.apply {
            btnStart.setOnClickListener { setupSmsRetriever() }
            btnHash.setOnClickListener { showAppHash() }
        }
    }

    private fun requestSmsOtp() {
        showLoading()

        CoroutineScope(Dispatchers.IO).launch {
            delay(2_000L)

            withContext(Dispatchers.Main) {
                toast("-> handle OTP")
                hideLoading()
            }
        }
    }

    private fun setupSmsRetriever() {
        smsRetrieverClient = SmsRetriever.getClient(this)
        task = smsRetrieverClient?.startSmsRetriever()

        task?.addOnSuccessListener {
            toast("-> Successfully started retriever")
            requestSmsOtp()

            // expect broadcast intent
        }

        task?.addOnFailureListener {
            toast("-> Failed to start retriever")
            it.printStackTrace()
        }
    }

    private fun hideLoading() {
        binding.progressLoading.visibility = View.GONE
    }

    private fun showLoading() {
        binding.progressLoading.visibility = View.VISIBLE
    }

    private fun showAppHash() {
        SignatureUtils
            .getAppHash(this@SmsRetrieverDemoActivity)
            .forEach { Log.e("tt", "-> app hash: $it") }
    }

}
