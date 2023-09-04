package mutnemom.android.kotlindemo.custom

import android.content.Context
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatImageView
import mutnemom.android.kotlindemo.R

class RatioImageView : AppCompatImageView {

    companion object {
        private const val REC_3_2 = 1
        private const val SQUARE = 0

        private const val PIXEL_PERFECT = 1
        private const val LANDSCAPE = 1
        private const val PORTRAIT = 0
    }

    private var imageOrientation: Int? = 0
    private var imageRatio: Int? = 0

    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet) : this(context, attrs, 0)
    constructor(context: Context, attrs: AttributeSet, defaultStyleAttr: Int)
            : super(context, attrs, defaultStyleAttr) {

        val typedArray = context.obtainStyledAttributes(attrs, R.styleable.RatioImageView)
            .apply {
                imageOrientation = getInt(R.styleable.RatioImageView_imageOrientation, 0)
                imageRatio = getInt(R.styleable.RatioImageView_imageRatio, 0)
            }

        typedArray.recycle()
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val width = MeasureSpec.getSize(widthMeasureSpec)
        var height = MeasureSpec.getSize(heightMeasureSpec)

        when (imageOrientation) {
            LANDSCAPE -> height = calculateRatioLandscape(width + PIXEL_PERFECT)
            PORTRAIT -> height = calculateRatioPortrait(width + PIXEL_PERFECT)
        }

        setMeasuredDimension(width, height)
        layoutParams.width = width
        layoutParams.height = height
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
    }

    private fun calculateRatioLandscape(measureSpec: Int): Int = when (imageRatio) {
        REC_3_2 -> (measureSpec * (2.0f / 3.0f)).toInt()
        SQUARE -> measureSpec
        else -> 0
    }

    private fun calculateRatioPortrait(measureSpec: Int): Int = when (imageRatio) {
        REC_3_2 -> (measureSpec * (3.0f / 2.0f)).toInt()
        SQUARE -> measureSpec
        else -> 0
    }

}
