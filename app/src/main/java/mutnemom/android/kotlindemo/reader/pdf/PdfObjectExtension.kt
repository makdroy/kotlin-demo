package mutnemom.android.kotlindemo.reader.pdf

import mutnemom.android.kotlindemo.reader.pdf.color.PdfColorSpace
import mutnemom.android.kotlindemo.reader.pdf.function.*
import java.io.IOException

fun PdfObject.calculateColorSpace(resources: Map<String, PdfObject>?): PdfColorSpace =
    getCache()
        ?.let { it as PdfColorSpace }
        ?: run {
            // obj is [/name <<dict>>]
            val arr: Array<PdfObject>? = getArray()
            val csValue: PdfColorSpace = when (val name = arr?.getOrNull(0)?.getStringValue()) {
                "ICCBased" -> PdfColorSpace.rgbSpace
                "CalGray" -> PdfColorSpace.graySpace
                "CalRGB" -> PdfColorSpace.rgbSpace
                "Lab" -> PdfColorSpace.rgbSpace
                "Separation", "DeviceN" -> {
                    val alternate = PdfColorSpace.getColorSpace(arr[2], resources)
                    val function: PdfFunction? = arr.getOrNull(3)?.getFunction()
                    AlternateColorSpace(alternate, function)
                }
                "Indexed", "I" -> {
                    /* 4.5.5 [/Indexed baseColor hival lookup] */
                    val refSpace = PdfColorSpace.getColorSpace(arr[1], resources)

                    // number of indices= ary[2], data is in ary[3];
                    val count = arr[2].getIntValue()
                    IndexedColor(
                        count = count,
                        base = refSpace,
                        stream = arr.getOrNull(3)
                    )
                }
                "Pattern" -> PdfColorSpace.rgbSpace
                else -> {
                    val obj = arr?.getOrNull(1)
                    throw IOException("Unknown color space: $name with $obj")
                }
            }

            setCache(csValue)
            csValue
        }

fun PdfObject.getFunction(): PdfFunction {
    val function: PdfFunction
    val type: Int
    var range: FloatArray? = null

    // read the function type (required)
    val typeObj: PdfObject = getDictRef("FunctionType")
        ?: throw IOException("No FunctionType specified in function!")

    type = typeObj.getIntValue()

    // read the function's domain (required)
    val domainObj: PdfObject = getDictRef("Domain")
        ?: throw IOException("No Domain specified in function!")

    val domainAry: Array<PdfObject> = domainObj.getArray() ?: arrayOf()
    val domain = FloatArray(domainAry.size)
    for (i in domainAry.indices) {
        domain[i] = domainAry[i].getFloatValue()
    }

    // read the function's range (optional)
    val rangeObj = getDictRef("Range")
    if (rangeObj != null) {
        val rangeAry: Array<PdfObject> = rangeObj.getArray() ?: arrayOf()
        range = FloatArray(rangeAry.size)
        for (i in rangeAry.indices) {
            range[i] = rangeAry[i].getFloatValue()
        }
    }

    // now create the actual function object
    when (type) {
        PdfFunction.TYPE_0 -> {
            if (rangeObj == null) {
                throw IOException("No Range specified in Type 0 Function!")
            }
            function = FunctionType0()
        }

        PdfFunction.TYPE_2 -> function = FunctionType2()
        PdfFunction.TYPE_3 -> function = FunctionType3()
        PdfFunction.TYPE_4 -> {
            if (rangeObj == null) {
                throw IOException("No Range specified in Type 4 Function!")
            }
            function = FunctionType4()
        }
        else -> throw IOException("Unsupported function type: $type")
    }

    // fill in the domain and optionally the range
    function.setDomain(domain)
    if (range != null) {
        function.setRange(range)
    }

    // now initialize the function
    function.parse(this)

    return function
}
