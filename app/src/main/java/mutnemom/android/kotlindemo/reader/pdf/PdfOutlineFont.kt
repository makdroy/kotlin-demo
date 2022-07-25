package mutnemom.android.kotlindemo.reader.pdf

import android.graphics.Path
import android.graphics.PointF

abstract class PdfOutlineFont(
    baseFont: String?,
    fontObj: PdfObject,
    descriptor: PdfFontDescriptor?
) : PdfFont(baseFont, descriptor) {

    /* the first character code  */
    private var firstChar = -1

    /* the last character code  */
    private var lastChar = -1

    /* the widths for each character code  */
    private var widths: FloatArray? = null

    init {
        fontObj.apply {
            firstChar = getDictRef("FirstChar")?.getIntValue() ?: -1
            lastChar = getDictRef("LastChar")?.getIntValue() ?: -1
            getDictRef("Widths")?.getArray()?.also { arr ->
                widths = FloatArray(arr.size)
                for (i in arr.indices) {
                    widths!![i] = arr[i].getFloatValue() / getDefaultWidth()
                }
            }
        }
    }

    /* Get the first character code  */
    open fun getFirstChar(): Int {
        return firstChar
    }

    /* Get the last character code  */
    open fun getLastChar(): Int {
        return lastChar
    }

    /* Get the default width in text space  */
    open fun getDefaultWidth(): Int {
        return 1000
    }

    /* Get the number of characters  */
    open fun getCharCount(): Int {
        return getLastChar() - getFirstChar() + 1
    }

    /* Get the width of a given character  */
    open fun getWidth(code: Char, name: String?): Float {
        val idx = (code.code and 0xff) - getFirstChar()

        // make sure we're in range
        return if (idx < 0 || widths == null || idx >= widths!!.size) {
            // try to get the missing width from the font descriptor
            descriptor?.missingWidth?.toFloat() ?: 0f

        } else widths!![idx]
    }

    override fun getGlyph(src: Char, name: String?): PdfGlyph? {
        var outline: Path? = null
        val width = getWidth(src, name)

        // first try by name
        if (name != null) {
            outline = getOutline(name, width)
        }

        // now try by character code (guaranteed to return)
        if (outline == null) {
            outline = getOutline(src, width)
        }

        // calculate the advance
        val advance = PointF(width, 0f)
        return PdfGlyph(
            src = src,
            name = name,
            shape = outline,
            advance = advance
        )
    }

    protected abstract fun getOutline(name: String?, width: Float): Path?

    protected abstract fun getOutline(src: Char, width: Float): Path?

}
