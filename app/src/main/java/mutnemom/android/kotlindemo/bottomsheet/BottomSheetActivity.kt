package mutnemom.android.kotlindemo.bottomsheet

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import mutnemom.android.kotlindemo.databinding.ActivityBottomSheetBinding

class BottomSheetActivity : AppCompatActivity() {

    private lateinit var binding: ActivityBottomSheetBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityBottomSheetBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setEvent()
    }

    private fun setEvent() {
        binding.btnToggle.setOnClickListener {
            KDBottomSheet().show(supportFragmentManager, "KDBottomSheet")
        }
    }

}
