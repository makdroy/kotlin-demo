package mutnemom.android.kotlindemo.animations.text

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.transition.TransitionManager
import mutnemom.android.kotlindemo.databinding.ActivityExpandableTextViewBinding

class ExpandableTextViewActivity : AppCompatActivity() {

    private lateinit var binding: ActivityExpandableTextViewBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityExpandableTextViewBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setEvent()
    }

    private fun setEvent() {
        binding.apply {
            btnCollapse.setOnClickListener { toggleReadMoreTextView(3) }
            btnExpand.setOnClickListener { toggleReadMoreTextView() }
        }
    }

    private fun toggleReadMoreTextView(collapseLine: Int = Int.MAX_VALUE) {
        if (binding.txtExpandable.maxLines != Int.MAX_VALUE) {
            binding.txtExpandable.maxLines = Int.MAX_VALUE
        } else {
            binding.txtExpandable.maxLines = collapseLine
        }

        // start animation
        TransitionManager.beginDelayedTransition(binding.root)
    }

}
