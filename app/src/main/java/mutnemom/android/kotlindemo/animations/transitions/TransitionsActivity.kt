package mutnemom.android.kotlindemo.animations.transitions

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.view.animation.AccelerateInterpolator
import android.view.animation.DecelerateInterpolator
import androidx.appcompat.app.AppCompatActivity
import androidx.transition.TransitionManager
import mutnemom.android.kotlindemo.R
import mutnemom.android.kotlindemo.databinding.ActivityTransitionsBinding

class TransitionsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityTransitionsBinding

    private var isMenuShow = true
    private var isFirst = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityTransitionsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setEvent()
        binding.btnToggleMenu.text = resources.getString(R.string.btn_transitions_toggle)
    }

    override fun onResume() {
        super.onResume()
        if (isFirst) {
            isFirst = false
            Handler(Looper.myLooper()!!).postDelayed({
                binding.btnToggleMenu.performClick()
            }, 600)
        }
    }

    private fun setEvent() {
        binding.btnToggleMenu.setOnClickListener {
            when {
                isMenuShow -> {
                    isMenuShow = false
                    binding.topBar
                        .animate()
                        ?.translationY(-200f)
                        ?.setDuration(200L)
                        ?.setInterpolator(AccelerateInterpolator())
                        ?.start()

                    binding.bottomSheet
                        .animate()
                        ?.translationY(600f)
                        ?.setDuration(150L)
                        ?.setInterpolator(AccelerateInterpolator())
                        ?.start()
                }
                else -> {
                    isMenuShow = true
                    binding.topBar
                        .animate()
                        ?.translationY(0f)
                        ?.setDuration(150L)
                        ?.setInterpolator(DecelerateInterpolator())
                        ?.start()

                    binding.bottomSheet
                        .animate()
                        ?.translationY(0f)
                        ?.setDuration(120L)
                        ?.setInterpolator(DecelerateInterpolator())
                        ?.start()
                }
            }
        }

        binding.containerFirst.let { parent ->
            binding.btnFirst.setOnClickListener {
                TransitionManager.beginDelayedTransition(parent)
                binding.txtFirst.apply {
                    visibility = when (visibility) {
                        View.VISIBLE -> View.GONE
                        else -> View.VISIBLE
                    }
                }
            }
        }
    }

}
