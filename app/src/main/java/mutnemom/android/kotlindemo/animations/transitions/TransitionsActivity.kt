package mutnemom.android.kotlindemo.animations.transitions

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.TransitionDrawable
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.view.animation.AccelerateInterpolator
import android.view.animation.DecelerateInterpolator
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
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
        binding.btnCrossFadeDrawable.setOnClickListener { startDrawableCrossFadeTransition() }
        binding.btnCrossFadeBgColor.setOnClickListener { startColorCrossFadeTransition() }
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

    private fun startDrawableCrossFadeTransition() {
        (ContextCompat.getDrawable(this, R.drawable.cross_fade_transition) as? TransitionDrawable)
            ?.also { transitionDrawable ->
                binding.imgCrossFading.setImageDrawable(transitionDrawable)
                transitionDrawable.startTransition(700)
            }
    }

    private fun startColorCrossFadeTransition() {
        val colors = arrayOf(ColorDrawable(Color.BLACK), ColorDrawable(Color.MAGENTA))
        TransitionDrawable(colors).also { transition ->
            binding.imgCrossFadingBgColor.background = transition
            transition.startTransition(700)
        }
    }

}
