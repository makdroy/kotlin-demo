package mutnemom.android.kotlindemo.progress

import android.content.Context
import android.graphics.Canvas
import android.util.AttributeSet
import android.view.MotionEvent
import androidx.appcompat.widget.AppCompatSeekBar
import androidx.core.content.ContextCompat
import mutnemom.android.kotlindemo.R

class VerticalSeekBar : AppCompatSeekBar {

    constructor(context: Context) : super(context) {
        initView(context)
    }

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
        initView(context)
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        setMeasuredDimension(measuredHeight, measuredWidth)
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (!isEnabled) {
            return false
        }

        when (event.action) {
            MotionEvent.ACTION_DOWN, MotionEvent.ACTION_MOVE, MotionEvent.ACTION_UP -> {
                progress = max - (max * event.y / height).toInt()
                onSizeChanged(width, height, 0, 0)
            }

            MotionEvent.ACTION_CANCEL -> {}
        }
        return true
    }

    override fun onDraw(canvas: Canvas?) {
        canvas?.rotate(-90f)
        canvas?.translate(-height.toFloat(), 0f)
        super.onDraw(canvas)
    }

    private fun initView(context: Context) {
        progressDrawable = ContextCompat.getDrawable(context, R.drawable.vertical_progress_selector)
        thumb = ContextCompat.getDrawable(context, R.drawable.vertical_progress_thumb)
    }

}
