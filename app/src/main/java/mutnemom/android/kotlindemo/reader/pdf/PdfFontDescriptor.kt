package mutnemom.android.kotlindemo.reader.pdf

import android.graphics.RectF
import java.io.IOException

class PdfFontDescriptor(
    private val descObj: PdfObject? = null,
    private var fontName: String? = null
) {

    companion object {
        const val FIXED_PITCH = 1 shl 1 - 1
        const val SERIF = 1 shl 2 - 1
        const val SYMBOLIC = 1 shl 3 - 1
        const val SCRIPT = 1 shl 4 - 1
        const val NON_SYMBOLIC = 1 shl 6 - 1
        const val ITALIC = 1 shl 7 - 1
        const val ALL_CAP = 1 shl 17 - 1
        const val SMALL_CAP = 1 shl 18 - 1
        const val FORCE_BOLD = 1 shl 19 - 1
    }

    var ascent = 0
    var capHeight = 0
    var descent = 0
    var flags = 0
    var fontFamily: String? = null

    // var fontName: String? = null
    var fontStretch: String? = null
    var fontWeight = 0
    var italicAngle = 0
    var stemV = 0
    var avgWidth = 0
    var fontFile: PdfObject? = null
    var fontFile2: PdfObject? = null
    var fontFile3: PdfObject? = null
    var leading = 0
    var maxWidth = 0
    var missingWidth = 0
    var stemH = 0
    var xHeight = 0
    var charSet: PdfObject? = null
    var fontBBox: RectF? = null

    init {
        descObj?.also { setupFontDescription(it) }
    }

    @Throws(IOException::class)
    private fun setupFontDescription(obj: PdfObject) {
        // required parameters
        ascent = obj.getDictRef("Ascent")?.getIntValue() ?: 0
        capHeight = obj.getDictRef("CapHeight")?.getIntValue() ?: 0
        descent = obj.getDictRef("Descent")?.getIntValue() ?: 0
        flags = obj.getDictRef("Flags")?.getIntValue() ?: 0
        fontName = obj.getDictRef("FontName")?.getStringValue() ?: ""
        italicAngle = obj.getDictRef("ItalicAngle")?.getIntValue() ?: 0
        stemV = obj.getDictRef("StemV")?.getIntValue() ?: 0

        // font bounding box
        obj.getDictRef("FontBBox")?.getArray()?.also { bBoxDef ->
            val bBoxFDef = FloatArray(4)
            for (i in 0..3) {
                bBoxFDef[i] = bBoxDef[i].getFloatValue()
            }

            fontBBox = RectF(
                bBoxFDef[0], bBoxFDef[1],
                bBoxFDef[2] - bBoxFDef[0],
                bBoxFDef[3] - bBoxFDef[1]
            )
        }

        // optional parameters
        avgWidth = obj.getDictRef("AvgWidth")?.getIntValue() ?: 0
        fontFile = obj.getDictRef("FontFile")
        fontFile2 = obj.getDictRef("FontFile2")
        fontFile3 = obj.getDictRef("FontFile3")
        leading = obj.getDictRef("Leading")?.getIntValue() ?: 0
        maxWidth = obj.getDictRef("MaxWidth")?.getIntValue() ?: 0
        missingWidth = obj.getDictRef("MissingWidth")?.getIntValue() ?: 0
        stemH = obj.getDictRef("StemH")?.getIntValue() ?: 0
        xHeight = obj.getDictRef("XHeight")?.getIntValue() ?: 0
        charSet = obj.getDictRef("CharSet")
        fontFamily = obj.getDictRef("FontFamily")?.getStringValue()
        fontWeight = obj.getDictRef("FontWeight")?.getIntValue() ?: 0
        fontStretch = obj.getDictRef("FontStretch")?.getStringValue()
    }

}
