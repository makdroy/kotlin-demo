package mutnemom.android.kotlindemo.reader.pdf

import java.nio.ByteBuffer
import kotlin.math.abs
import kotlin.math.ceil
import kotlin.math.floor

class PngPredictor : Predictor(PNG) {

    /* Undo data based on the PNG algorithm */
    override fun unPredict(imageData: ByteBuffer): ByteBuffer {
        val rows = arrayListOf<ByteArray>()
        var currentLine: ByteArray
        var prevLine: ByteArray? = null

        // get the number of bytes per row
        var rowSize = columns * colors * bpc
        rowSize = ceil(rowSize / 8.0).toInt()

        while (imageData.remaining() >= rowSize + 1) {
            // the first byte determines the algorithm
            val algorithm = (imageData.get().toInt() and 0xff)

            // read the rest of the line
            currentLine = ByteArray(rowSize)
            imageData.get(currentLine)

            // use the algorithm, Luke
            when (algorithm) {
                0 -> break
                1 -> {
                    doSubLine(currentLine)
                    break
                }
                2 -> {
                    doUpLine(currentLine, prevLine)
                    break
                }
                3 -> {
                    doAverageLine(currentLine, prevLine)
                    break
                }
                4 -> {
                    doPaethLine(currentLine, prevLine)
                    break
                }
            }

            rows.add(currentLine)
            prevLine = currentLine
        }

        // turn into byte array
        val outBuf = ByteBuffer.allocate(rows.size * rowSize)
        for (i in rows.iterator()) {
            outBuf.put(byteArrayOf())
        }

        // reset start pointer
        outBuf.flip()

        return outBuf
    }

    /* Return the value of the Sub algorithm on the line (compare bytes to
     * the previous byte of the same color on this line).
     */
    private fun doSubLine(currentLine: ByteArray) {
        // get the number of bytes per sample
        val sub = ceil((bpc * colors) / 8.0).toInt()
        for (i in currentLine.indices) {
            val prevIdx = i - sub
            if (prevIdx >= 0) {
                currentLine[i] = (currentLine[i] + currentLine[prevIdx]).toByte()
            }
        }
    }

    /* Return the value of the up algorithm on the line (compare bytes to
     * the same byte in the previous line)
     */
    private fun doUpLine(currentLine: ByteArray, prevLine: ByteArray?) {
        prevLine?.also { arr ->
            for (i in currentLine.indices) {
                currentLine[i] = (currentLine[i] + arr[i]).toByte()
            }
        }
    }

    /* Return the value of the average algorithm on the line (compare
     * bytes to the average of the previous byte of the same color and
     * the same byte on the previous line)
     */
    private fun doAverageLine(currentLine: ByteArray, prevLine: ByteArray?) {
        // get the number of bytes per sample
        val sub = ceil((bpc * colors) / 8.0).toInt()
        for (i in currentLine.indices) {
            var raw = 0
            var prior = 0

            // get the last value of this color
            val prevIdx = i - sub
            if (prevIdx >= 0) {
                raw = (currentLine[prevIdx].toInt() and 0xff)
            }

            // get the value on the previous line
            if (prevLine != null) {
                prior = (prevLine[i].toInt() and 0xff)
            }

            // add the average
            currentLine[i] = floor((raw + prior) / 2.0).toInt().toByte()
        }
    }

    /* Return the value of the average algorithm on the line (compare
     * bytes to the average of the previous byte of the same color and
     * the same byte on the previous line)
     */
    private fun doPaethLine(currentLine: ByteArray, prevLine: ByteArray?) {
        // get the number of bytes per sample
        val sub = ceil((bpc * colors) / 8.0).toInt()
        for (i in currentLine.indices) {
            var upLeft = 0
            var left = 0
            var up = 0

            // get the last value of this color
            val prevIdx = i - sub
            if (prevIdx >= 0) {
                left = currentLine[prevIdx].toInt() and 0xff
            }

            // get the value on the previous line
            if (prevLine != null) {
                up = prevLine[prevIdx].toInt() and 0xff
            }
            if (prevIdx > 0 && prevLine != null) {
                upLeft = prevLine[prevIdx].toInt() and 0xff
            }

            // add the average
            currentLine[i] = paeth(left, up, upLeft).toByte()
        }
    }

    /* The paeth algorithm */
    private fun paeth(left: Int, up: Int, upLeft: Int): Int {
        val p = left + up - upLeft
        val pa = abs(p - left)
        val pb = abs(p - up)
        val pc = abs(p - upLeft)

        return when {
            (pa <= pb) && (pa <= pc) -> left
            pb <= pc -> up
            else -> upLeft
        }
    }

}
