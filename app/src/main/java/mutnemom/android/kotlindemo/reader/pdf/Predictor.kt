package mutnemom.android.kotlindemo.reader.pdf

import okio.IOException
import java.nio.ByteBuffer

abstract class Predictor(val algorithm: Int) {

    companion object {
        const val PNG = 1

        fun getPredictor(params: PdfObject): Predictor? {
            // get the algorithm (required)
            return params
                .getDictRef("Predictor")
                ?.let {
                    // create the predictor object
                    val predictor = when (val algorithm = it.getIntValue()) {
                        1 -> null
                        2 -> throw IOException("Tiff Predictor not supported")
                        10, 11, 12, 13, 14, 15 -> PngPredictor()
                        else -> throw IOException("Unknown predictor: $algorithm")
                    }

                    // read the colors (optional)
                    val colorsObj = params.getDictRef("Colors")
                    if (colorsObj != null) {
                        predictor?.colors = colorsObj.getIntValue()
                    }

                    // read the bits per component (optional)
                    val bpcObj = params.getDictRef("BitsPerComponent")
                    if (bpcObj != null) {
                        predictor?.bpc = bpcObj.getIntValue()
                    }

                    // read the columns (optional)
                    val columnsObj = params.getDictRef("Columns")
                    if (columnsObj != null) {
                        predictor?.columns = columnsObj.getIntValue()
                    }

                    predictor
                }
        }
    }

    /* the number of colors per sample */
    var colors: Int = 1

    /* the number of columns per row */
    var columns = 1

    /* the number of bits per color component */
    var bpc = 8

    abstract fun unPredict(imageData: ByteBuffer): ByteBuffer?

}
