package mutnemom.android.kotlindemo.reader.pdf

import android.graphics.Color
import android.graphics.RectF

data class ImageInfo(
    var width: Int,
    var height: Int,
    var clip: RectF?,
    var bgColor: Int = Color.WHITE
) {

//    // a hashcode that uses width, height and clip to generate its number
//    override fun hashCode(): Int {
//        var code = width xor height shl 16
//        if (clip != null) {
//            code = code xor (clip!!.width().toInt() or clip!!.height().toInt() shl 8)
//            code = code xor (clip!!.left.toInt() or clip!!.top.toInt())
//        }
//        return code
//    }
//
//    // an equals method that compares values
//    override fun equals(o: Any?): Boolean {
//        if (o !is ImageInfo) {
//            return false
//        }
//        val ii = o
//        return if (width != ii.width || height != ii.height) {
//            false
//        } else if (clip != null && ii.clip != null) {
//            clip == ii.clip
//        } else if (clip == null && ii.clip == null) {
//            true
//        } else {
//            false
//        }
//    }

}
