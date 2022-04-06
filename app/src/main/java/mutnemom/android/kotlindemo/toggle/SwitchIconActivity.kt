package mutnemom.android.kotlindemo.toggle

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import mutnemom.android.kotlindemo.databinding.ActivitySwitchIconBinding

class SwitchIconActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySwitchIconBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivitySwitchIconBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setEvent()
    }

    private fun setEvent() {
        binding.toggleSpeaker.apply { setOnClickListener { switchState(animate = true) } }
        binding.toggleCheck.apply { setOnClickListener { switchState(animate = true) } }
        binding.toggleIcon.apply { setOnClickListener { switchState(animate = true) } }
    }

}
