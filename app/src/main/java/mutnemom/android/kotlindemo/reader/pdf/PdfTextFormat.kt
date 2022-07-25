package mutnemom.android.kotlindemo.reader.pdf

import android.graphics.Matrix
import android.graphics.PointF
import mutnemom.android.kotlindemo.reader.pdf.commands.PdfNativeTextCmd

class PdfTextFormat {

    /* current matrix transform  */
    private var cur = Matrix()

    /* matrix transform at start of line  */
    private val line = Matrix()

    /* are we between BT and ET?  */
    private var inuse = false

    /* build text rep of word  */
    private val word = StringBuffer()

    /* location of the end of the previous hunk of text  */
    private val prevEnd: PointF = PointF(-100f, -100f)

    private var horizontalScaling = 1f
    private var textFormatMode = 2 /* PdfShapeCmd.FILL */
    private var charSpacing = 0f
    private var wordSpacing = 0f
    private var leading = 0f
    private var rise = 0f

    private var font: PdfFont? = null
    private var fontSize = 1f

    private fun setFont(f: PdfFont, size: Float) {
        font = f
        fontSize = size
    }

    fun doText(cmd: PdfPage, text: String) {
        if (PdfFont.sUseFontSubstitution) {
            doTextFontSubst(cmd, text)
        } else {
            doTextNormal(cmd, text)
        }
    }

    @Throws(IllegalStateException::class)
    fun doText(cmd: PdfPage?, arr: Array<*>?) {
        cmd ?: return
        arr ?: return

        var i = 0
        val to = arr.size
        while (i < to) {
            if (arr[i] is String) {
                doText(cmd, (arr[i] as String))
            } else if (arr[i] is Double) {
                val doubleValue = (arr[i] as Double).toFloat() / 1000f
                cur.preTranslate(-doubleValue * fontSize * horizontalScaling, 0f)
            } else {
                throw IllegalStateException("Bad element in TJ array")
            }
            ++i
        }
    }

    fun clone(): Any {
        val newFormat = PdfTextFormat()

        // copy values
        newFormat.horizontalScaling = horizontalScaling
        newFormat.charSpacing = charSpacing
        newFormat.wordSpacing = wordSpacing
        newFormat.leading = leading
        newFormat.textFormatMode = textFormatMode
        newFormat.rise = rise

        // copy immutable fields
        font?.also { newFormat.setFont(it, fontSize) }

        // clone transform (mutable)
        // newFormat.getTransform().setTransform(getTransform());
        return newFormat
    }

    /* reset the PDFTextFormat for a new run */
    fun reset() {
        cur.reset()
        line.reset()
        inuse = true
        word.setLength(0)
    }

    private fun doTextFontSubst(cmd: PdfPage, text: String) {
        val zero = PointF()
        val scale = Matrix()
        scale.setMatValues(fontSize, 0f, 0f, fontSize * horizontalScaling, 0f, rise)

        val at = Matrix()
        at.set(cur)
        at.preConcat(scale)
        val ntx = PdfNativeTextCmd(text, at)
        cmd.addCommand(ntx)

        // calc widths
        for (i in text.indices) {
            val c = text[0]
            var width = 0.6f
            if (font is PdfOutlineFont) {
                width = (font as PdfOutlineFont).getWidth(c, null)
            }

            var advanceX: Float = width * fontSize + charSpacing
            if (c == ' ') {
                advanceX += wordSpacing
            }

            advanceX *= horizontalScaling
            cur.preTranslate(advanceX, 0f)
        }

        val src = floatArrayOf(zero.x, zero.y)
        val dst = FloatArray(src.size)
        cur.mapPoints(dst, src)
        prevEnd.set(dst[0], dst[1])
    }

    private fun doTextNormal(cmd: PdfPage, text: String) {
        val zero = PointF()
        val scale = Matrix()
        scale.setMatValues(fontSize, 0f, 0f, fontSize * horizontalScaling, 0f, rise)

        val mtx = Matrix()
        val l: List<PdfGlyph> = font?.getGlyphs(text) as List<PdfGlyph>
        for (glyph in l) {
            mtx.set(cur)
            mtx.preConcat(scale)
            val advance: PointF = glyph.addCommands(cmd, mtx, textFormatMode)!!
            var advanceX: Float = advance.x * fontSize + charSpacing
            if (glyph.src == ' ') {
                advanceX += wordSpacing
            }

            advanceX *= horizontalScaling
            cur.preTranslate(advanceX, advance.y)
        }

        val src = floatArrayOf(zero.x, zero.y)
        val dst = FloatArray(src.size)
        cur.mapPoints(dst, src)
        prevEnd.set(dst[0], dst[1])
    }

}
