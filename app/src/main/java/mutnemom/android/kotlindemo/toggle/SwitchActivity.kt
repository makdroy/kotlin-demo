package mutnemom.android.kotlindemo.toggle

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_switch.*
import mutnemom.android.kotlindemo.R

class SwitchActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_switch)
        setEvent()
    }

    private fun setEvent() {
        btnSwitchIcon?.setOnClickListener { openSwitchIconPage() }
    }

    private fun openSwitchIconPage() {
        startActivity(Intent(this, SwitchIconActivity::class.java))
    }

}
