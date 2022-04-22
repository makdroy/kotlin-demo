package mutnemom.android.kotlindemo.progress

import android.os.Bundle
import android.view.Gravity
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity
import mutnemom.android.kotlindemo.databinding.ActivityVerticalProgressBarBinding

class VerticalProgressBarActivity : AppCompatActivity() {

    private lateinit var binding: ActivityVerticalProgressBarBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityVerticalProgressBarBinding.inflate(layoutInflater)
        setContentView(binding.root)

        calculateHeight()
    }

    private fun calculateHeight() {
        binding.bar1.post {
            val width = binding.bar1.layoutParams.width
            // val height = Resources.getSystem().displayMetrics.heightPixels
            val height = binding.bar1.height
            val params = LinearLayout.LayoutParams(width, (height * .5).toInt())
            params.gravity = Gravity.BOTTOM
            params.marginStart = 100
            binding.bar1.layoutParams = params
        }
    }

}
