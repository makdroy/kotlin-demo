package mutnemom.android.kotlindemo.reader.pdf.function

import android.util.Log
import mutnemom.android.kotlindemo.reader.pdf.PdfObject
import java.nio.ByteBuffer
import kotlin.math.*

class FunctionType0 : PdfFunction(TYPE_0) {

    companion object {

        /* Perform a linear interpolation.
         * Given a value x, and two points,
         * (xmin, ymin), (xmax, ymax), where xmin <= x <= xmax, calculate a value
         * y on the line from (xmin, ymin) to (xmax, ymax).
         *
         * @param x the x value of the input
         * @param xmin the minimum x value
         * @param ymin the minimum y value
         * @param xmax the maximum x value
         * @param ymax the maximum y value
         * @return the y value interpolated from the given x
         */
        fun interpolate(x: Float, xMin: Float, xMax: Float, yMin: Float, yMax: Float): Float {
            var value = (yMax - yMin) / (xMax - xMin)
            value *= x - xMin
            value += yMin
            return value
        }
    }

    private var size: IntArray = intArrayOf()
    private var bitsPerSample = 0
    private var order = 1

    /* the optional encoding array, tells how to map input parameters to values  */
    private var encode: FloatArray = floatArrayOf()

    /* the optional decoding array, tells how to map output parameters to values  */
    private var decode: FloatArray = floatArrayOf()

    /* the actual samples, converted to integers.
     * The first index is input values (from 0 to size[m - 1] * size[m - 2] * ... * size[0]),
     * and the second is the output dimension within the sample (from 0 to n)
     */
    private var samples: Array<IntArray> = arrayOf()

    override fun doFunction(
        inputs: FloatArray,
        inputOffset: Int,
        outputs: FloatArray,
        outputOffset: Int
    ) {

        // calculate the encoded values for each input
        val encoded = FloatArray(numInputs)
        for (i in 0 until numInputs) {
            // encode -- interpolate(x<i>, domain<2i>, domain<2i+1>, encode<2i>, encode<2i+1>)
            encoded[i] = interpolate(
                inputs[i + inputOffset],
                getDomain(2 * i),
                getDomain(2 * i + 1),
                getEncode(2 * i),
                getEncode(2 * i + 1)
            )

            // clip to size of sample table -- min(max(e<i>, 0), size<i> - 1)
            encoded[i] = max(encoded[i], 0f)
            encoded[i] = min(encoded[i], (size[i] - 1).toFloat())
        }

        // do some magic
        for (i in 0 until numOutputs) {
            if (order == 1) {
                outputs[i + outputOffset] = multilinearInterpolate(encoded, i)
            } else {
                outputs[i + outputOffset] = multicubicInterpolate(encoded, i)
            }
        }

        // now adjust the output to be within range
        for (i in outputs.indices) {
            // decode -- interpolate(r<i>, 0, 2^bps - 1, decode<2i>, decode<2i+1>)
            outputs[i + outputOffset] = interpolate(
                outputs[i + outputOffset],
                0f,
                2.toDouble().pow(bitsPerSample.toDouble()).toFloat() - 1,
                getDecode(2 * i),
                getDecode(2 * i + 1)
            )
        }
    }

    override fun parse(obj: PdfObject) {
        // read the size array (required)
        obj.getDictRef("Size")
            ?.also {
                it.getArray()?.also { arr ->
                    val size = IntArray(arr.size)
                    arr.forEachIndexed { index, pdfObject ->
                        size[index] = pdfObject.getIntValue()
                    }
                    setSize(size)
                }
            }
            ?: throw IllegalStateException("Size required for function type 0!")

        // read the # bits per sample (required)
        obj.getDictRef("BitsPerSample")
            ?.also { bitsPerSample = it.getIntValue() }
            ?: throw IllegalStateException("BitsPerSample required for function type 0!")

        // read the order (optional)
        obj.getDictRef("Order")
            ?.also { order = it.getIntValue() }

        // read the encode array (optional)
        obj.getDictRef("Encode")?.getArray()
            ?.also { encodeArr ->
                val encode = FloatArray(encodeArr.size)
                encodeArr.forEachIndexed { index, pdfObject ->
                    encode[index] = pdfObject.getFloatValue()
                }
                setEncode(encode)
            }

        // read the decode array (optional)
        obj.getDictRef("Decode")?.getArray()
            ?.also { decodeArr ->
                val decode = FloatArray(decodeArr.size)
                decodeArr.forEachIndexed { index, pdfObject ->
                    decode[index] = pdfObject.getFloatValue()
                }
                setDecode(decode)
            }

        // finally, read the samples
        obj.getStreamBuffer()
            ?.also { setSamples(readSamples(it)) }
    }

    private fun getSize(dimension: Int): Int {
        return size[dimension]
    }

    private fun setSize(intArr: IntArray) {
        size = intArr
    }

    private fun getEncode(index: Int): Float =
        encode.getOrNull(index)
            ?: when {
                index % 2 == 0 -> 0f
                else -> (getSize(index / 2) - 1).toFloat()
            }

    private fun setEncode(floatArr: FloatArray) {
        encode = floatArr
    }

    private fun getDecode(index: Int): Float =
        decode.getOrNull(index) ?: getRange(index)

    private fun setDecode(floatArr: FloatArray) {
        decode = floatArr
    }

    private fun getSample(intArr: IntArray, outputDimension: Int): Int {
        var mult = 1
        var index = 0
        intArr.forEachIndexed { i, itemValue ->
            index += mult * itemValue
            mult *= getSize(i)
        }
        return samples[index][outputDimension]
    }

    private fun getSample(encoded: FloatArray, map: Int, outputDimension: Int): Float {
        val controls = IntArray(encoded.size)

        // fill in the controls array with appropriate ints
        for (i in controls.indices) {
            if (map and (0x1 shl i) == 0) {
                controls[i] = floor(encoded[i].toDouble()).toInt()
            } else {
                controls[i] = ceil(encoded[i].toDouble()).toInt()
            }
        }

        // now return the actual sample
        return getSample(controls, outputDimension).toFloat()
    }

    private fun setSamples(intArr2D: Array<IntArray>) {
        samples = intArr2D
    }

    /* Read the samples from the input stream.
     * Each sample is made up of *n* components,
     * each of which has length *bitsPerSample* bits.
     * The samples are arranged by dimension, then range
     */
    private fun readSamples(buf: ByteBuffer): Array<IntArray> {
        // calculate the number of samples in the table
        var size = 1
        for (i in 0 until numInputs) {
            size *= getSize(i)
        }

        // create the samples table
        val samples = Array(size) {
            IntArray(numOutputs)
        }

        // the current location in the buffer, in bits from byteLoc
        var bitLoc = 0

        // the current location in the buffer, in bytes
        var byteLoc = 0

        // the current index in the samples array
        var index = 0
        for (i in 0 until numInputs) {
            for (j in 0 until getSize(i)) {
                for (k in 0 until numOutputs) {
                    /* [JK FIXME one bit at a time is really inefficient  */
                    var value = 0
                    var toRead: Int = bitsPerSample
                    var curByte: Byte = buf.get(byteLoc)
                    while (toRead > 0) {
                        val nextBit: Int = (curByte.toInt() shr (7 - bitLoc)) and 0x1
                        value = value or (nextBit shl toRead - 1)
                        if (++bitLoc == 8) {
                            bitLoc = 0
                            byteLoc++
                            if (toRead > 1) {
                                curByte = buf.get(byteLoc)
                            }
                        }
                        toRead--
                    }
                    samples[index][k] = value
                }
                index++
            }
        }
        return samples
    }

    /* Perform a piecewise multilinear interpolation.
     * The provides a close approximation to the standard linear interpolation,
     * at a far lower cost, since every element is not evaluated at every iteration.
     * Instead, a walk of the most significant axes is performed,
     * following the algorithm described at:
     * http://osl.iu.edu/~tveldhui/papers/MAScThesis/node33.html
     *
     * @param encoded the encoded input values
     * @param od the output dimension
     */
    private fun multilinearInterpolate(encoded: FloatArray, od: Int): Float {
        // first calculate the distances -- the differences between
        // each encoded value and the integer below it.
        val distances = FloatArray(encoded.size)
        for (i in distances.indices) {
            distances[i] = (encoded[i] - floor(encoded[i].toDouble())).toFloat()
        }

        // initialize the map of axes.  Each bit in this map represents
        // whether the control value in that dimension should be the integer
        // above or below encoded[i]
        var map = 0

        // the initial values
        var sampleValue: Float = getSample(encoded, map, od)
        var prev = sampleValue

        // walk the axes
        for (i in distances.indices) {
            // find the largest value of dist remaining
            var idx = 0
            var largest = -1f
            for (c in distances.indices) {
                if (distances[c] > largest) {
                    largest = distances[c]
                    idx = c
                }
            }

            // now find the sample with that axis set to 1
            map = map or (0x1 shl idx)
            val cur: Float = getSample(encoded, map, od)

            // calculate the value and remember it
            sampleValue += distances[idx] * (cur - prev)
            prev = sampleValue

            // make sure we won't find this distance again
            distances[idx] = (-1).toFloat()
        }

        // voila
        return sampleValue
    }

    /* Perform a multicubic interpolation
     *
     * @param encoded the encoded input values
     * @param od the output dimension
     */
    private fun multicubicInterpolate(encoded: FloatArray, od: Int): Float {
        Log.w(this::class.java.simpleName, "Cubic interpolation not supported!")
        return multilinearInterpolate(encoded, od)
    }

}
