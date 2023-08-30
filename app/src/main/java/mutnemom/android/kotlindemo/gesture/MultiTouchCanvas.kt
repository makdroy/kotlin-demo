package mutnemom.android.kotlindemo.gesture

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Path
import android.view.MotionEvent
import android.view.View

class MultiTouchCanvas(context: Context) : View(context) {

    private val pointerIdMapToPath = mutableMapOf<Int, Path>()
    private val paint = Paint()

    init {
        paint.style = Paint.Style.STROKE
        paint.isAntiAlias = true
        paint.strokeJoin = Paint.Join.ROUND
        paint.strokeCap = Paint.Cap.ROUND
        paint.strokeWidth = 5f
    }

    override fun onDraw(canvas: Canvas) {
        pointerIdMapToPath.forEach { (_, path) ->
            canvas.drawPath(path, paint)
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent?): Boolean {
        when (event?.actionMasked) {
            MotionEvent.ACTION_DOWN,
            MotionEvent.ACTION_POINTER_DOWN -> {
                val index = event.actionIndex
                val pointerIdToAdd = event.getPointerId(index)
                val pathToAdd = Path().apply { moveTo(event.getX(index), event.getY(index)) }
                pointerIdMapToPath[pointerIdToAdd] = pathToAdd
            }
            MotionEvent.ACTION_MOVE -> {
                for (i in 0 until event.pointerCount) {
                    val path = pointerIdMapToPath[event.getPointerId(i)]
                    path?.lineTo(event.getX(i), event.getY(i))
                }
            }
            MotionEvent.ACTION_UP,
            MotionEvent.ACTION_POINTER_UP -> {
            }
            else -> return false
        }

        invalidate()
        return true
    }

}
