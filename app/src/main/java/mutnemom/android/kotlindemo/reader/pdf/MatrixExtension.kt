package mutnemom.android.kotlindemo.reader.pdf

import android.graphics.Matrix

fun Matrix.setMatValues(m00: Int, m01: Int, m10: Int, m11: Int, m02: Int, m12: Int) {
    val fArr = floatArrayOf(
        m00.toFloat(),
        m10.toFloat(),
        m02.toFloat(),
        m01.toFloat(),
        m11.toFloat(),
        m12.toFloat(),
        0f,
        0f,
        1f
    )
    setValues(fArr)
}

fun Matrix.setMatValues(m00: Float, m01: Float, m10: Float, m11: Float, m02: Float, m12: Float) {
    val fArr = floatArrayOf(m00, m10, m02, m01, m11, m12, 0f, 0f, 1f)
    setValues(fArr)
}

fun Matrix.setMatValues(fArr: FloatArray) {
    setMatValues(fArr[0], fArr[1], fArr[2], fArr[3], fArr[4], fArr[5])
}