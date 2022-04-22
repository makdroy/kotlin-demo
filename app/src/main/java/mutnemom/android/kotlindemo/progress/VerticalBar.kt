package mutnemom.android.kotlindemo.progress

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.View

class VerticalBar : View {

    private var paint: Paint? = null
    private var path: Path? = null

    constructor(context: Context) : super(context) {
        init()
    }

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
        init()
    }

    constructor(
        context: Context,
        attrs: AttributeSet?,
        defStyle: Int
    ) : super(context, attrs, defStyle) {
        init()
    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)

        path ?: return
        paint ?: return
        canvas?.drawPath(path!!, paint!!)
    }

    private fun init() {
        paint = Paint()
        paint?.color = Color.BLUE
        paint?.strokeWidth = 30f
        paint?.style = Paint.Style.STROKE
        paint?.isAntiAlias = true
        paint?.isDither = true
        paint?.strokeJoin = Paint.Join.ROUND
        paint?.strokeCap = Paint.Cap.ROUND
        paint?.pathEffect = CornerPathEffect(paint?.strokeWidth ?: 0f)

        path = Path()
        path?.moveTo(50f, 50f)
        path?.lineTo(50f, 500f)
        path?.lineTo(200f, 500f)
        path?.lineTo(200f, 300f)
        path?.lineTo(350f, 300f)
    }

}
