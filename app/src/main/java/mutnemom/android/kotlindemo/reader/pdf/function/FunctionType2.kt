package mutnemom.android.kotlindemo.reader.pdf.function

import mutnemom.android.kotlindemo.reader.pdf.PdfObject
import kotlin.math.pow

class FunctionType2 : PdfFunction(TYPE_2) {

    /* the function's value at zero for the n outputs  */
    private var c0 = floatArrayOf(0f)

    /* the function's value at one for the n outputs  */
    private var c1 = floatArrayOf(1f)

    /* the exponent  */
    private var exponent = 0f

    /* Calculate the function value for the input.
     * For each output (j), the function value is:
     * C0(j) + x^N * (C1(j) - C0(j))
     */
    override fun doFunction(
        inputs: FloatArray,
        inputOffset: Int,
        outputs: FloatArray,
        outputOffset: Int
    ) {
        // read the input value
        val input = inputs[inputOffset]

        // calculate the output values
        for (i in 0 until numOutputs) {
            outputs[i + outputOffset] = getC0(i) +
                    (input.toDouble().pow(exponent.toDouble()) * (getC1(i) - getC0(i))).toFloat()
        }
    }

    /* Read the zeros, ones and exponent */
    override fun parse(obj: PdfObject) {
        // read the exponent (required)
        obj.getDictRef("N")
            ?.also { exponent = it.getFloatValue() }
            ?: throw IllegalStateException("Exponent required for function type 2!")

        // read the zeros array (optional)
        obj.getDictRef("C0")?.getArray()?.also { cZeroArr ->
            val cZero = FloatArray(cZeroArr.size)
            cZeroArr.forEachIndexed { index, pdfObject ->
                cZero[index] = pdfObject.getFloatValue()
            }
            setC0(cZero)
        }

        // read the ones array (optional)
        obj.getDictRef("C1")?.getArray()?.also { cOneArr ->
            val cOne = FloatArray(cOneArr.size)
            cOneArr.forEachIndexed { index, pdfObject ->
                cOne[index] = pdfObject.getFloatValue()
            }
            setC1(cOne)
        }
    }

    /* Get the values at zero */
    private fun getC0(index: Int): Float {
        return c0[index]
    }

    /* Set the values at zero */
    private fun setC0(fArr: FloatArray) {
        c0 = fArr
    }

    /* Get the values at one */
    private fun getC1(index: Int): Float {
        return c1[index]
    }

    /* Set the values at one */
    private fun setC1(fArr: FloatArray) {
        c1 = fArr
    }

}
