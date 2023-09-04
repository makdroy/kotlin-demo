package mutnemom.android.kotlindemo

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import mutnemom.android.kotlindemo.databinding.ActivityDebounceClickBinding
import mutnemom.android.kotlindemo.extensions.toast

class DebounceClickActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDebounceClickBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityDebounceClickBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setEvent()
    }

    private fun setEvent() {
        binding.apply {
            btnCustomListener.setOnClickListener(1000L) {
                toast("handle click event")
            }

            btnCoroutinesFlow.clicks()
                .throttleFirst(1200L)
                .onEach { toast("handle click with Flow") }
                .launchIn(lifecycleScope)
        }
    }

    private fun View.clicks(): Flow<Unit> = callbackFlow {
        setOnClickListener { trySend(Unit) }
        awaitClose { setOnClickListener(null) }
    }

    private fun <T> Flow<T>.throttleFirst(windowDuration: Long): Flow<T> = flow {
        var lastEmissionTime = 0L
        collect { upstream ->
            val currentTime = System.currentTimeMillis()
            val mayEmit = currentTime - lastEmissionTime > windowDuration
            if (mayEmit) {
                lastEmissionTime = currentTime
                emit(upstream)
            }
        }
    }

    private fun View.setOnClickListener(debounceInterval: Long, listenerBlock: (View) -> Unit) =
        setOnClickListener(DebounceOnClickListener(debounceInterval, listenerBlock))

    class DebounceOnClickListener(
        private val interval: Long,
        private val listenerBlock: (View) -> Unit
    ) : View.OnClickListener {

        private var lastClickTime = 0L

        override fun onClick(v: View) {
            val time = System.currentTimeMillis()
            if (time - lastClickTime >= interval) {
                lastClickTime = time
                listenerBlock(v)
            }
        }

    }

}
