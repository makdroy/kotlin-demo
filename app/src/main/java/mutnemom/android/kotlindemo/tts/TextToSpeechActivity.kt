package mutnemom.android.kotlindemo.tts

import android.media.MediaPlayer
import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.net.toUri
import mutnemom.android.kotlindemo.databinding.ActivityTextToSpeechBinding
import java.io.File
import java.util.*

class TextToSpeechActivity : AppCompatActivity() {

    private lateinit var binding: ActivityTextToSpeechBinding

    private var tts: TextToSpeech? = null
    private val ttsListener = TextToSpeech.OnInitListener { status: Int ->
        when (status) {
            TextToSpeech.SUCCESS -> tts?.setLanguage(Locale("th", "TH"))
                ?.also { result ->
                    when (result) {
                        TextToSpeech.LANG_MISSING_DATA,
                        TextToSpeech.LANG_NOT_SUPPORTED -> {
                            binding.btnStart.isEnabled = false
                            Toast
                                .makeText(this, "Language is not supported", Toast.LENGTH_LONG)
                                .show()
                        }
                        else -> binding.btnStart.isEnabled = true
                    }
                }
            else -> Toast
                .makeText(this, "Initialization failed", Toast.LENGTH_LONG)
                .show()
        }
    }

    private val utteranceProgressListener = object : UtteranceProgressListener() {
        override fun onStart(utteranceId: String?) {
            Log.e("utterance", "-> onStart()")
        }

        override fun onDone(utteranceId: String?) {
            Log.e("utterance", "-> onDone()")

            if (utteranceId == synthesizeId) {
                Log.e("utterance", "-> play audio")
                playAudio()
            }
        }

        override fun onError(utteranceId: String?) {
            Log.e("utterance", "-> onError()")
        }
    }

    private val synthesizeId = UUID.randomUUID().toString()
    private lateinit var wavFile: File

    private var player: MediaPlayer? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityTextToSpeechBinding.inflate(layoutInflater)
        setContentView(binding.root)

        wavFile = File("${applicationContext.getExternalFilesDir(null)}/tts_backup.wav")

        tts = TextToSpeech(this, ttsListener)
        tts?.setOnUtteranceProgressListener(utteranceProgressListener)

        binding.btnStart.setOnClickListener { speakOut() }
        binding.btnStop.setOnClickListener { player?.pause() }
    }

    override fun onDestroy() {
        player?.apply {
            stop()
            reset()
        }

        tts?.apply {
            stop()
            shutdown()
        }

        super.onDestroy()
    }

    private fun speakOut() {
        when (player) {
            null -> binding.txtSample.text?.toString()?.let {
                tts?.synthesizeToFile(it, null, wavFile, synthesizeId)
            }
            else -> player!!.start()
        }
    }

    private fun playAudio() {
        player = MediaPlayer.create(this, wavFile.toUri())
        player?.start()
    }

}
