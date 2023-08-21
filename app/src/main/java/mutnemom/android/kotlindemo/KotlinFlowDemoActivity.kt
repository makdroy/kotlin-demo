package mutnemom.android.kotlindemo

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collectIndexed
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.launch
import mutnemom.android.kotlindemo.databinding.ActivityKotlinFlowDemoBinding

class KotlinFlowDemoActivity : AppCompatActivity() {

    private lateinit var binding: ActivityKotlinFlowDemoBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityKotlinFlowDemoBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setEvent()
    }

    private fun setEvent() {
        binding.apply {
            btnAsFlow.setOnClickListener { testKotlinFlowAsFlow() }
            btnEmit.setOnClickListener { testKotlinFlowEmit() }
        }
    }

    private fun printMessage(msg: String) {
        val newText = StringBuilder(binding.txtFlowEvent.text)
            .append("\n")
            .append(msg)
            .toString()

        binding.txtFlowEvent.text = newText
    }

    private fun testKotlinFlowEmit() {
        binding.txtFlowEvent.text = StringBuilder().toString()
        lifecycleScope.launch {
            createFlowEmit()
                .onStart { printMessage("start flow with emit()") }
                .catch { error -> printMessage("error: $error") }
                .onEach { printMessage("onEach(): $it") }
                .onCompletion { error -> printMessage("onCompletion(): ${error == null}") }
                .first().also { printMessage("first(): $it") }
        }
    }

    private fun testKotlinFlowAsFlow() {
        binding.txtFlowEvent.text = StringBuilder().toString()
        lifecycleScope.launch {
            createFlowWithAsFlow()
                .onStart { printMessage("start flow with asFlow()") }
                .catch { error -> printMessage("error: $error") }
                .onEach { printMessage("onEach(): $it") }
                .onCompletion { error -> printMessage("onCompletion(): ${error == null}") }
                .collectIndexed { index, value -> printMessage("collect(): $index, $value") }
        }
    }

    private fun createFlowWithAsFlow(): Flow<Int> =
        listOf(6, 7, 8)
            .asFlow()
            .onEach { delay(2999L) }

    private fun createFlowEmit() = flow {
        for (i in 1..3) {
            delay(300L)
            emit(i)
        }
    }
}
