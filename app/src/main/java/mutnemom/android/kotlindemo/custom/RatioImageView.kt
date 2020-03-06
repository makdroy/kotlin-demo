package mutnemom.android.kotlindemo.custom

import android.content.Context
import android.util.AttributeSet
import androidx.annotation.Nullable
import androidx.appcompat.widget.AppCompatImageView
import mutnemom.android.kotlindemo.R

class RatioImageView : AppCompatImageView {

    companion object {
        private const val SQUARE = 0
        private const val PORTRAIT = 0
        private const val LANDSCAPE = 1
        private const val RECTANGLE = 1
        private const val PIXEL_PERFECT = 1
        private const val RECTANGLE_DIVISION = 0.5
    }

    var imageOrientation: Int? = 0
    var imageRatio: Int? = 0


    constructor(context: Context) : super(context)
    constructor(context: Context, @Nullable attrs: AttributeSet) : this(context, attrs, 0)
    constructor(context: Context, @Nullable attrs: AttributeSet, defaultStyleAttr: Int)
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
        val height = MeasureSpec.getSize(heightMeasureSpec)

        when (imageOrientation) {

        }
    }

}
