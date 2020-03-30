package mutnemom.android.kotlindemo.toggle

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_switch_icon.*
import mutnemom.android.kotlindemo.R

class SwitchIconActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_switch_icon)
        setEvent()
    }

    private fun setEvent() {
        toggleSpeaker?.apply { setOnClickListener { switchState(animate = true) } }
        toggleCheck?.apply { setOnClickListener { switchState(animate = true) } }
        toggleIcon?.apply { setOnClickListener { switchState(animate = true) } }
    }

}
