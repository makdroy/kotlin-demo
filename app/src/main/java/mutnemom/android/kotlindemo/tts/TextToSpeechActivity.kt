package mutnemom.android.kotlindemo.tts

import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_text_to_speech.*
import mutnemom.android.kotlindemo.R
import java.util.*

class TextToSpeechActivity : AppCompatActivity() {

    private var tts: TextToSpeech? = null
    private val ttsListener = TextToSpeech.OnInitListener { status: Int ->
        when (status) {
            TextToSpeech.SUCCESS -> tts?.setLanguage(Locale("th", "TH"))
                ?.also { result ->
                    when (result) {
                        TextToSpeech.LANG_MISSING_DATA,
                        TextToSpeech.LANG_NOT_SUPPORTED -> {
                            btnStart?.isEnabled = false
                            Toast
                                .makeText(this, "Language is not supported", Toast.LENGTH_LONG)
                                .show()
                        }
                        else -> btnStart?.isEnabled = true
                    }
                }
            else -> Toast
                .makeText(this, "Initialization failed", Toast.LENGTH_LONG)
                .show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_text_to_speech)

        tts = TextToSpeech(this, ttsListener)

        btnStart?.setOnClickListener { speakOut() }
        btnStop?.setOnClickListener { tts?.stop() }
    }

    override fun onDestroy() {
        tts?.apply {
            stop()
            shutdown()
        }
        super.onDestroy()
    }

    private fun speakOut() {
        txtSample?.text?.toString()?.let {
            tts?.speak(it, TextToSpeech.QUEUE_FLUSH, null, UUID.randomUUID().toString())
        }

    }

}
