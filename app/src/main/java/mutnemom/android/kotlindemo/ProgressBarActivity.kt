package mutnemom.android.kotlindemo

import android.animation.ObjectAnimator
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.view.animation.DecelerateInterpolator
import kotlinx.android.synthetic.main.activity_progress_bar.*

class ProgressBarActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_progress_bar)
    }


    override fun onStart() {
        super.onStart()

        progress?.also {
            Handler().postDelayed({
                val animation = ObjectAnimator.ofInt(it, "progress", 40)
                animation.duration = 200
                animation.interpolator = DecelerateInterpolator()
                animation.start()
            }, 1_000)

            Handler().postDelayed({
                val animation = ObjectAnimator.ofInt(it, "progress", 80)
                animation.duration = 200
                animation.interpolator = DecelerateInterpolator()
                animation.start()
            }, 2_000)
        }
    }
}
