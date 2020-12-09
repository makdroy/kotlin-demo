package mutnemom.android.kotlindemo

import android.animation.ObjectAnimator
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.animation.DecelerateInterpolator
import androidx.appcompat.app.AppCompatActivity
import mutnemom.android.kotlindemo.databinding.ActivityProgressBarBinding

class ProgressBarActivity : AppCompatActivity() {

    private lateinit var binding: ActivityProgressBarBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityProgressBarBinding.inflate(layoutInflater)
        setContentView(binding.root)
    }

    override fun onStart() {
        super.onStart()

        binding.progress.also {
            Handler(Looper.myLooper()!!).postDelayed({
                val animation = ObjectAnimator.ofInt(it, "progress", 40)
                animation.duration = 200
                animation.interpolator = DecelerateInterpolator()
                animation.start()
            }, 1_000)

            Handler(Looper.myLooper()!!).postDelayed({
                val animation = ObjectAnimator.ofInt(it, "progress", 80)
                animation.duration = 200
                animation.interpolator = DecelerateInterpolator()
                animation.start()
            }, 2_000)
        }
    }
}
