package mutnemom.android.kotlindemo.bottomsheet

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_bottom_sheet.*
import mutnemom.android.kotlindemo.R

class BottomSheetActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_bottom_sheet)

        setEvent()
    }

    private fun setEvent() {
        btnToggle?.setOnClickListener {
            KDBottomSheet().show(supportFragmentManager, "KDBottomSheet")
        }
    }

}
