package mutnemom.android.kotlindemo.custom

import android.animation.LayoutTransition
import android.os.Bundle
import android.text.TextUtils
import android.view.View
import android.view.ViewGroup
import android.view.ViewTreeObserver
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity
import mutnemom.android.kotlindemo.databinding.ActivityCustomViewBinding

class CustomViewActivity : AppCompatActivity() {

    companion object {
        private const val LONG_TEXT = "Lorem Ipsum is simply dummy text of the printing" +
                " and typesetting industry. Lorem Ipsum has been the industry's standard" +
                " dummy text ever since the 1500s, when an unknown printer took a galley" +
                " of type and scrambled it to make a type specimen book. It has survived" +
                " not only five centuries, but also the leap into electronic typesetting," +
                " remaining essentially unchanged. It was popularised in the 1960s with" +
                " the release of Letraset sheets containing Lorem Ipsum passages, and more" +
                " recently with desktop publishing software like Aldus PageMaker including" +
                " versions of Lorem Ipsum."

        private const val SHORT_TEXT = "For the 2009 model" +
                " the G35 sedan was replaced by the G37 sedan."

        private const val MAX_LINES_COLLAPSED = 3

        private const val IDLE_ANIMATION_STATE = 1
        private const val EXPANDING_ANIMATION_STATE = 2
        private const val COLLAPSING_ANIMATION_STATE = 3
    }

    private lateinit var binding: ActivityCustomViewBinding

    private val isCollapsed: Boolean
        get() = binding.expandableTextView.maxLines == MAX_LINES_COLLAPSED

    private val isExpanded: Boolean
        get() = binding.expandableTextView.maxLines == Int.MAX_VALUE

    private val isCanBeCollapse: Boolean
        get() = binding.expandableTextView.lineCount > MAX_LINES_COLLAPSED

    private val isTrimmedWithLimitLines: Boolean
        get() = binding.expandableTextView.lineCount > binding.expandableTextView.maxLines

    private val isIdle: Boolean
        get() = mCurrentAnimationState == IDLE_ANIMATION_STATE

    private val isRunning: Boolean
        get() = !isIdle

    private var mCurrentAnimationState = IDLE_ANIMATION_STATE

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityCustomViewBinding.inflate(layoutInflater)
        setContentView(binding.root)

        applyLayoutTransition(binding.expandableTextView)
        setEvent()
        initState()
    }

    private fun initState() {
        binding.expandableTextView.apply {
            maxLines = MAX_LINES_COLLAPSED
            text = LONG_TEXT
        }

        binding.tvDummy.apply {
            maxLines = MAX_LINES_COLLAPSED
            text = LONG_TEXT
            ellipsize = TextUtils.TruncateAt.END
        }
    }

    private fun setEvent() {
        binding.switchShortText.setOnCheckedChangeListener { _, isChecked ->
            when (isChecked) {
                true -> updateWithNewText(SHORT_TEXT)
                else -> updateWithNewText(LONG_TEXT)
            }
        }

        binding.expandableTextView.setOnClickListener {

            if (isRunning) {
                (binding.expandableTextView.parent as? LinearLayout)?.apply {
                    val replaceTransition = layoutTransition
                    layoutTransition = replaceTransition
                }
            }

            when (isCollapsed) {
                true -> {
                    mCurrentAnimationState = EXPANDING_ANIMATION_STATE
                    binding.expandableTextView.maxLines = Int.MAX_VALUE
                }
                else -> {
                    mCurrentAnimationState = COLLAPSING_ANIMATION_STATE
                    binding.expandableTextView.maxLines = MAX_LINES_COLLAPSED
                }
            }
            updateWithNewText(LONG_TEXT)
        }

        binding.switchShortText2.setOnCheckedChangeListener { _, isChecked ->
            when (isChecked) {
                true -> binding.tvCustomExpandable.text = SHORT_TEXT
                else -> binding.tvCustomExpandable.text = LONG_TEXT
            }
        }
    }

    private fun updateWithNewText(newText: String) {
        binding.expandableTextView.apply {
            viewTreeObserver.addOnGlobalLayoutListener(object :
                ViewTreeObserver.OnGlobalLayoutListener {
                override fun onGlobalLayout() {
                    isClickable = if (isExpanded) isCanBeCollapse else isTrimmedWithLimitLines
                    binding.expandableTextView.viewTreeObserver
                        .removeOnGlobalLayoutListener(this)
                }
            })

            text = newText
        }
    }

    private fun applyLayoutTransition(transitionView: View) {
        val transition = LayoutTransition()

        // 6_000L for test reverse transition, else 300L
        transition.setDuration(300L)

        transition.enableTransitionType(LayoutTransition.CHANGING)
        transition.addTransitionListener(object : LayoutTransition.TransitionListener {
            override fun endTransition(p0: LayoutTransition?, p1: ViewGroup?, p2: View?, p3: Int) {
//                binding.scrollRoot.post {
//                    binding.scrollRoot.fullScroll(View.FOCUS_DOWN)
//                }

                mCurrentAnimationState = IDLE_ANIMATION_STATE
            }

            override fun startTransition(
                p0: LayoutTransition?,
                p1: ViewGroup?,
                p2: View?,
                p3: Int
            ) {
            }
        })

        (transitionView.parent as? LinearLayout)
            ?.layoutTransition = transition
    }

}
