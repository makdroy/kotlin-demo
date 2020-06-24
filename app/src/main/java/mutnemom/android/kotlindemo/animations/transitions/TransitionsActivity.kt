package mutnemom.android.kotlindemo.animations.transitions

import android.os.Bundle
import android.os.Handler
import android.view.View
import android.view.animation.AccelerateInterpolator
import android.view.animation.DecelerateInterpolator
import androidx.appcompat.app.AppCompatActivity
import androidx.transition.TransitionManager
import kotlinx.android.synthetic.main.activity_transitions.*
import mutnemom.android.kotlindemo.R

class TransitionsActivity : AppCompatActivity() {

    private var isMenuShow = true
    private var isFirst = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_transitions)
        setEvent()
        btnToggleMenu?.text = resources.getString(R.string.btn_transitions_toggle)
    }

    override fun onResume() {
        super.onResume()
        if (isFirst) {
            isFirst = false
            Handler().postDelayed({
                btnToggleMenu?.performClick()
            }, 600)
        }
    }

    private fun setEvent() {
        btnToggleMenu?.setOnClickListener {
            when {
                isMenuShow -> {
                    isMenuShow = false
                    topBar
                        ?.animate()
                        ?.translationY(-200f)
                        ?.setDuration(200L)
                        ?.setInterpolator(AccelerateInterpolator())
                        ?.start()

                    bottomSheet
                        ?.animate()
                        ?.translationY(600f)
                        ?.setDuration(150L)
                        ?.setInterpolator(AccelerateInterpolator())
                        ?.start()
                }
                else -> {
                    isMenuShow = true
                    topBar
                        ?.animate()
                        ?.translationY(0f)
                        ?.setDuration(150L)
                        ?.setInterpolator(DecelerateInterpolator())
                        ?.start()

                    bottomSheet
                        ?.animate()
                        ?.translationY(0f)
                        ?.setDuration(120L)
                        ?.setInterpolator(DecelerateInterpolator())
                        ?.start()
                }
            }
        }

        containerFirst?.let { parent ->
            btnFirst.setOnClickListener {
                TransitionManager.beginDelayedTransition(parent)
                txtFirst?.apply {
                    visibility = when (visibility) {
                        View.VISIBLE -> View.GONE
                        else -> View.VISIBLE
                    }
                }
            }
        }
    }

}
