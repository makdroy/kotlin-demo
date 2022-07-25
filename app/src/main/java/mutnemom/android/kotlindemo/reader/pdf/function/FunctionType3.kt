package mutnemom.android.kotlindemo.reader.pdf.function

import mutnemom.android.kotlindemo.reader.pdf.PdfObject

class FunctionType3 : PdfFunction(TYPE_3) {

    override fun doFunction(
        inputs: FloatArray,
        inputOffset: Int,
        outputs: FloatArray,
        outputOffset: Int
    ) {
    }

    override fun parse(obj: PdfObject) {
        // read the Functions array (required)
        obj.getDictRef("Functions")
            ?.also {
                it.getArray()?.also { functions ->
                    val size = IntArray(functions.size)
                    functions.forEachIndexed { index, pdfObject ->
                        size[index] = pdfObject.getIntValue()
                    }
                }
            }
            ?: throw IllegalStateException("Functions required for function type 3!")

        // read the Bounds array (required)
        obj.getDictRef("Bounds")
            ?.also {
                it.getArray()?.also { bounds ->
                    val size = IntArray(bounds.size)
                    bounds.forEachIndexed { index, pdfObject ->
                        size[index] = pdfObject.getIntValue()
                    }
                }
            }
            ?: throw IllegalStateException("Bounds required for function type 3!")

        // read the encode array (required)
        obj.getDictRef("Encode")
            ?.also {
                it.getArray()?.also { encodes ->
                    val encode = FloatArray(encodes.size)
                    encodes.forEachIndexed { index, pdfObject ->
                        encode[index] = pdfObject.getFloatValue()
                    }
                }
            }
            ?: throw IllegalStateException("Encode required for function type 3!")
    }

}
