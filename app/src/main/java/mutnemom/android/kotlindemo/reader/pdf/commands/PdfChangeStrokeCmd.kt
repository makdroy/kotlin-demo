package mutnemom.android.kotlindemo.reader.pdf.commands

import android.graphics.Paint.Cap
import android.graphics.Paint.Join
import android.graphics.RectF
import mutnemom.android.kotlindemo.reader.pdf.PdfCmd
import mutnemom.android.kotlindemo.reader.pdf.PdfRendererSync

class PdfChangeStrokeCmd : PdfCmd() {

    var limit: Float = PdfRendererSync.NOLIMIT
    var phase: Float = PdfRendererSync.NOPHASE
    var join: Join? = PdfRendererSync.NOJOIN
    var ary: FloatArray? = PdfRendererSync.NODASH
    var cap: Cap? = PdfRendererSync.NOCAP
    var w: Float = PdfRendererSync.NOWIDTH

//    fun setDash(ary: FloatArray?, phase: Float) {
//        if (ary != null) {
//            // make sure no pairs start with 0, since having no opaque
//            // region doesn't make any sense.
//            var i = 0
//            while (i < ary.size - 1) {
//                if (ary[i] == 0) {
//                    /* Give a very small value, since 0 messes java up */
//                    ary[i] = 0.00001f
//                    break
//                }
//                i += 2
//            }
//        }
//        this.ary = ary
//        this.phase = phase
//    }

    override fun execute(state: PdfRendererSync): RectF? {
        state.setStrokeParts(w, cap, join, limit, ary, phase)
        return null
    }

    override fun toString(): String {
        return "STROKE: w=" + w + " cap=" + cap + " join=" + join + " limit=" + limit +
                " ary=" + ary + " phase=" + phase
    }

}
