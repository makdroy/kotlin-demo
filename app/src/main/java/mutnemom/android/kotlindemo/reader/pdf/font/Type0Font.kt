package mutnemom.android.kotlindemo.reader.pdf.font

import mutnemom.android.kotlindemo.reader.pdf.PdfFont
import mutnemom.android.kotlindemo.reader.pdf.PdfFontDescriptor
import mutnemom.android.kotlindemo.reader.pdf.PdfGlyph
import mutnemom.android.kotlindemo.reader.pdf.PdfObject

class Type0Font(
    baseFont: String?,
    fontObj: PdfObject?,
    descriptor: PdfFontDescriptor?
) : PdfFont(baseFont, descriptor) {

    private lateinit var fonts: Array<PdfFont?>

    init {
        fontObj?.getDictRef("DescendantFonts")?.getArray()?.also { descendantFonts ->
            fonts = arrayOfNulls(descendantFonts.size)
            for (i in descendantFonts.indices) {
                fonts[i] = getFont(descendantFonts[i], null)
            }
        }
    }

    fun getDescendantFont(fontId: Int): PdfFont? {
        return fonts[fontId]
    }

    override fun getGlyph(src: Char, name: String?): PdfGlyph? {
        return getDescendantFont(0)?.getGlyph(src, name)
    }

}
