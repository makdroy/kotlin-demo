package mutnemom.android.kotlindemo.custom

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ValueAnimator
import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.view.ViewGroup
import android.view.animation.AccelerateDecelerateInterpolator
import androidx.appcompat.widget.AppCompatTextView

class ExpandableTextView : AppCompatTextView, View.OnClickListener {

    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet) : this(context, attrs, 0)
    constructor(context: Context, attrs: AttributeSet, defaultStyleAttr: Int)
            : super(context, attrs, defaultStyleAttr)

    companion object {
        private const val MAX_LINES_COLLAPSED = 3
    }

    private val isCollapsed: Boolean
        get() = maxLines != Int.MAX_VALUE

    private var mAnimator: ValueAnimator? = null
    private var isCollapsing: Boolean = false

    init {
        maxLines = MAX_LINES_COLLAPSED
        setOnClickListener(this)
        initAnimator()
    }

//    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
//        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
//    }

    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        super.onLayout(changed, left, top, right, bottom)
        isClickable = lineCount > MAX_LINES_COLLAPSED
    }

    override fun onClick(v: View?) {
        if (mAnimator?.isRunning == true) {
            animatorReverse()
            return
        }

        val startPosition = measuredHeight.toFloat()
        val endPosition = animateTo().toFloat()

        mAnimator?.setFloatValues(startPosition, endPosition)
        animatorStart()
    }

    private fun initAnimator() {
        mAnimator = ValueAnimator.ofFloat(-1f, -1f)

        mAnimator?.apply {
            duration = 300
            interpolator = AccelerateDecelerateInterpolator()
            addUpdateListener { animator ->
                updateHeight(animator.animatedValue as Float)
            }

            addListener(object : AnimatorListenerAdapter() {
                override fun onAnimationStart(animation: Animator) {
                    isCollapsing = !isCollapsed
                    if (isCollapsed) {
                        maxLines = Int.MAX_VALUE
                    }
                }

                override fun onAnimationEnd(animation: Animator) {
                    if (!isCollapsed && isCollapsing) {
                        maxLines = MAX_LINES_COLLAPSED
                    }

                    setWrapContent()
                }
            })
        }
    }

    private fun updateHeight(animatedValue: Float) {
        val params = layoutParams
        params.height = animatedValue.toInt()
        post { layoutParams = params }
    }

    private fun setWrapContent() {
        val params = layoutParams
        params.height = ViewGroup.LayoutParams.WRAP_CONTENT
        post { layoutParams = params }
    }

    private fun animateTo(): Int = when {
        isCollapsed -> layout.height + getPaddingHeight()
        else -> layout.getLineBottom(MAX_LINES_COLLAPSED - 1) +
                layout.bottomPadding +
                getPaddingHeight()
    }

    private fun getPaddingHeight(): Int = compoundPaddingBottom + compoundPaddingTop

    private fun animatorStart() {
        mAnimator?.start()
    }

    private fun animatorReverse() {
        isCollapsing = !isCollapsing
        mAnimator?.reverse()
    }

}
