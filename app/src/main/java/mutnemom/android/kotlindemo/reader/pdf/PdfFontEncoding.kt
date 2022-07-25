package mutnemom.android.kotlindemo.reader.pdf

import mutnemom.android.kotlindemo.reader.pdf.font.FontSupport
import mutnemom.android.kotlindemo.reader.pdf.font.Type0Font
import java.io.IOException

class PdfFontEncoding(fontType: String, encoding: PdfObject) {

    companion object {
        private const val TYPE_ENCODING = 0
        private const val TYPE_CMAP = 1
    }

    private var baseEncoding: IntArray? = null
    private var differences: MutableMap<Char, String>? = null
    private var type = 0

    init {
        if (encoding.type == PdfObject.NAME) {
            // if the encoding is a String, it is the name of an encoding
            // or the name of a CMap, depending on the type of the font
            if (fontType == "Type0") {
                type = TYPE_CMAP
            } else {
                type = TYPE_ENCODING
                differences = HashMap()
                baseEncoding = getBaseEncoding(encoding.getStringValue() ?: "")
            }
        } else {
            // look at the "Type" entry of the encoding to determine the type
            when (encoding.getDictRef("Type")?.getStringValue()) {
                "Encoding" -> {
                    // it is an encoding
                    type = TYPE_ENCODING
                    parseEncoding(encoding)
                }
                "CMap" -> {
                    // it is a CMap
                    type = TYPE_CMAP
                }
                else -> throw IllegalArgumentException("Uknown encoding type: $type")
            }
        }
    }

    fun getGlyphs(font: PdfFont, text: String): List<PdfGlyph> {
        val outList: MutableList<PdfGlyph> = ArrayList<PdfGlyph>(text.length)

        // go character by character through the text
        val cArr = text.toCharArray()
        var i = 0
        while (i < cArr.size) {
            when (type) {
                TYPE_ENCODING -> getGlyphFromEncoding(font, cArr[i])
                    ?.also { outList.add(it) }

                TYPE_CMAP -> {
                    // 2 bytes -> 1 character in a CMap
                    var c: Char = (cArr[i].code and 0xff shl 8).toChar()
                    if (i < cArr.size - 1) {
                        c = (c.code or (cArr[++i].code and 0xff)).toChar()
                    }

                    getGlyphFromCMap(font, c)?.also { outList.add(it) }
                }
            }
            i++
        }
        return outList
    }

    private fun getGlyphFromEncoding(font: PdfFont, src: Char): PdfGlyph? {
        var charName: String? = null

        // only deal with one byte of source
        val outChar = (src.code and 0xff).toChar()

        // see if this character is in the differences list
        if (differences!!.containsKey(outChar)) {
            charName = differences!![outChar]
        } else if (baseEncoding != null) {
            // get the character name from the base encoding
            val charID = baseEncoding!![outChar.code]
            charName = FontSupport.getName(charID)
        }
        return font.getCachedGlyph(outChar, charName)
    }

    private fun getGlyphFromCMap(font: PdfFont, src: Char): PdfGlyph? {
        var outFont: PdfFont = font
        if (font is Type0Font) {
            font.getDescendantFont(0)
                ?.also { outFont = it }
        }

        return outFont.getCachedGlyph(src, null)
    }

    @Throws(IOException::class)
    fun parseEncoding(encoding: PdfObject) {
        differences = mutableMapOf()

        // figure out the base encoding, if one exists
        encoding.getDictRef("BaseEncoding")?.getStringValue()
            ?.also { baseEncoding = getBaseEncoding(it) }

        // parse the differences array
        encoding.getDictRef("Differences")?.getArray()
            ?.also { arr ->
                var curPosition = -1
                for (i in arr.indices) {
                    when (arr[i].type) {
                        PdfObject.NUMBER -> curPosition = arr[i].getIntValue()
                        PdfObject.NAME -> {
                            val key = curPosition.toChar()
                            arr[i].getStringValue()
                                ?.also { differences!![key] = it }

                            curPosition++
                        }
                        else -> throw IllegalArgumentException(
                            "Unexpected type in diff array: ${arr[i]}"
                        )
                    }
                }
            }
    }

    private fun getBaseEncoding(encodingName: String): IntArray =
        when (encodingName) {
            "MacExpertEncoding" -> FontSupport.type1CExpertCharset
            "MacRomanEncoding" -> FontSupport.macRomanEncoding
            "WinAnsiEncoding" -> FontSupport.winAnsiEncoding
            else -> throw IllegalArgumentException("Unknown encoding: $encodingName")
        }

}
