package mutnemom.android.kotlindemo.reader.pdf

import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RectF

class PdfPaint(color: Int) {

    val paint: Paint = Paint()

    companion object {
        var s_doAntiAlias = false

        fun getColorPaint(color: Int): PdfPaint {
            val result = PdfPaint(color)
            result.paint.style = Paint.Style.STROKE
            return result
        }

        fun getPaint(p: Int): PdfPaint {
            val result = PdfPaint(p)
            result.paint.style = Paint.Style.FILL
            return result
        }
    }

    fun fill(state: PdfRendererSync?, g: Canvas, s: Path): RectF {
        g.drawPath(s, paint)
        val bounds = RectF()
        val result = RectF()
        s.computeBounds(bounds, false)
        g.matrix.mapRect(result, bounds)
        return bounds
    }

    init {
        paint.color = color
        paint.isAntiAlias = s_doAntiAlias
    }

}
