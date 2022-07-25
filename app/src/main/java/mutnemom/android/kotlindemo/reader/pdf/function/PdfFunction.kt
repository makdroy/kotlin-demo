package mutnemom.android.kotlindemo.reader.pdf.function

import mutnemom.android.kotlindemo.reader.pdf.PdfObject
import java.io.IOException
import kotlin.math.max
import kotlin.math.min

abstract class PdfFunction(val type: Int) {

    companion object {

        /* Sampled function  */
        const val TYPE_0 = 0

        /* Exponential interpolation function  */
        const val TYPE_2 = 2

        /* Stitching function.  */
        const val TYPE_3 = 3

        /* PostScript calculator function.  */
        const val TYPE_4 = 4
    }

    /* the input domain of this function, an array of 2 * *m* floats  */
    private var domain: FloatArray? = null

    /* the output range of this functions, and array of 2 * *n* floats.
     * required for type 0 and 4 functions
     */
    private var range: FloatArray? = null

    val numInputs: Int
        get() = domain?.let { it.size / 2 } ?: 0

    val numOutputs: Int
        get() = range?.let { it.size / 2 } ?: 0

    fun getDomain(index: Int): Float = domain?.getOrNull(index) ?: -1f

    fun setDomain(domain: FloatArray) {
        this.domain = domain
    }

    fun getRange(index: Int) =
        range?.getOrNull(index)
            ?: run { if (index % 2 == 0) Float.MIN_VALUE else Float.MAX_VALUE }

    fun setRange(range: FloatArray) {
        this.range = range
    }

    @Throws(IllegalStateException::class)
    open fun calculate(
        inputs: FloatArray,
        inputOffset: Int = 0,
        outputs: FloatArray = FloatArray(numOutputs),
        outputOffset: Int = 0
    ): FloatArray {
        // check the inputs
        if (inputs.size - inputOffset < numInputs) {
            IllegalStateException("Wrong number of inputs to function!")
        }

        // check the outputs
        if (range != null && outputs.size - outputOffset < numOutputs) {
            IllegalStateException("Wrong number of outputs for function!")
        }

        // clip the inputs to domain
        for (i in inputs.indices) {
            // clip to the domain -- min(max(x<i>, domain<2i>), domain<2i+1>)
            inputs[i] = max(inputs[i], getDomain(2 * i))
            inputs[i] = min(inputs[i], getDomain(2 * i + 1))
        }

        // do the actual calculation
        doFunction(inputs, inputOffset, outputs, outputOffset)

        // clip the outputs to range
        var i = 0
        while (range != null && i < outputs.size) {
            // clip to range -- min(max(r<i>, range<2i>), range<2i + 1>)
            outputs[i] = max(outputs[i], getRange(2 * i))
            outputs[i] = min(outputs[i], getRange(2 * i + 1))
            i++
        }
        return outputs
    }

    abstract fun doFunction(
        inputs: FloatArray,
        inputOffset: Int,
        outputs: FloatArray,
        outputOffset: Int
    )

    @Throws(IOException::class)
    abstract fun parse(obj: PdfObject)

}
