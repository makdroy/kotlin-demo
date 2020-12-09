package mutnemom.android.kotlindemo.toggle

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import mutnemom.android.kotlindemo.databinding.ActivitySwitchBinding

class SwitchActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySwitchBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivitySwitchBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setEvent()
    }

    private fun setEvent() {
        binding.btnSwitchIcon.setOnClickListener { openSwitchIconPage() }
    }

    private fun openSwitchIconPage() {
        startActivity(Intent(this, SwitchIconActivity::class.java))
    }

}
