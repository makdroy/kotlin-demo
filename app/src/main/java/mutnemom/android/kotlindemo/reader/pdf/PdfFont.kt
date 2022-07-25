package mutnemom.android.kotlindemo.reader.pdf

abstract class PdfFont(
    val baseFont: String?,
    val descriptor: PdfFontDescriptor?
) {

    companion object {
        var sUseFontSubstitution = false

        @Throws(IllegalStateException::class)
        fun getFont(obj: PdfObject, resources: HashMap<String, PdfObject>?): PdfFont? {
            return null
//            return obj.getCache()
//                ?: run {
//                    val font: PdfFont
//                    var baseFont: String? = null
//                    var encoding: PdfFontEncoding? = null
//                    val descriptor: PdfFontDescriptor?
//
//                    var subType: String? = obj.getDictRef("Subtype").getStringValue()
//                    if (subType == null) {
//                        subType = obj.getDictRef("S")!!.getStringValue()
//                    }
//
//                    var baseFontObj = obj.getDictRef("BaseFont")
//                    val encodingObj = obj.getDictRef("Encoding")
//                    val descObj = obj.getDictRef("FontDescriptor")
//
//                    if (baseFontObj != null) {
//                        baseFont = baseFontObj.getStringValue()
//                    } else {
//                        baseFontObj = obj.getDictRef("Name")
//                        if (baseFontObj != null) {
//                            baseFont = baseFontObj.getStringValue()
//                        }
//                    }
//
//                    if (encodingObj != null) {
//                        encoding = PdfFontEncoding(subType!!, encodingObj)
//                    }
//
//                    descriptor = if (descObj != null) {
//                        PdfFontDescriptor(descObj)
//                    } else {
//                        PdfFontDescriptor(fontName = baseFont)
//                    }
//
//                    when (subType) {
//                        "Type0" -> font = Type0Font(baseFont, obj, descriptor)
//                        "Type1" -> {
//                            // load a type1 font
//                            when {
//                                descriptor == null -> font = BuiltinFont(baseFont, obj)
//                                descriptor.getFontFile() != null -> {
//                                    // it's a Type1 font, included.
//                                    font = Type1Font(baseFont, obj, descriptor)
//                                }
//                                descriptor.getFontFile3() != null -> {
//                                    // it's a CFF (Type1C) font
//                                    font = Type1CFont(baseFont, obj, descriptor)
//                                }
//                                else -> {
//                                    // no font info. Fake it based on the FontDescriptor
//                                    //		System.out.println("Fakeout native font");
//                                    font = BuiltinFont(baseFont, obj, descriptor)
//                                }
//                            }
//                        }
//                        "TrueType" -> {
//                            if (descriptor.getFontFile2() != null) {
//                                // load a TrueType font
//                                font = TTFFont(baseFont, obj, descriptor)
//                            } else {
//                                // fake it with a built-in font
//                                font = BuiltinFont(baseFont, obj, descriptor)
//                            }
//                        }
//                        "Type3" -> font = Type3Font(baseFont, obj, resources, descriptor)
//                        "CIDFontType2" -> font = CIDFontType2(baseFont, obj, descriptor)
//                        "CIDFontType0" -> font = CIDFontType2(baseFont, obj, descriptor)
//                        else -> throw IllegalStateException("Cannot handle a '$subType' font")
//                    }
//
//                    font.encoding = encoding
//                    font.subtype = subType
//
//                    obj.setCache(font)
//                    font
//                }
        }
    }

    /* the font SubType of this font  */
    var subtype: String? = null

    /* the font encoding (maps character ids to glyphs)  */
    var encoding: PdfFontEncoding? = null

    /* a cache of glyphs indexed by character  */
    private var charCache: MutableMap<Char, PdfGlyph>? = null

    open fun getGlyphs(text: String): List<PdfGlyph?> {
        val outList = listOf<PdfGlyph>()
//        val outList: MutableList<PdfGlyph?> = encoding.getGlyphs(this, text)
//            ?: run {
//                // use the default mapping
//                val cArr = text.toCharArray()
//                val list = ArrayList<PdfGlyph>(cArr.size)
//                for (i in cArr.indices) {
//                    // only look at 2 bytes when there is no encoding
//                    val src: Char = (cArr[i].code and 0xff).toChar()
//                    getCachedGlyph(src, null)
//                        ?.also { list.add(it) }
//                }
//
//                list.toMutableList()
//            }

        return outList
    }

    open fun getCachedGlyph(src: Char, name: String?): PdfGlyph? {
        if (charCache == null) {
            charCache = mutableMapOf()
        }

        // try the cache
        var glyph: PdfGlyph? = charCache!![src]

        // if it's not there, add it to the cache
        if (glyph == null) {
            getGlyph(src, name)?.also {
                glyph = it
                charCache!![src] = it
            }
        }
        return glyph
    }

    abstract fun getGlyph(src: Char, name: String?): PdfGlyph?

    override fun equals(other: Any?): Boolean =
        when (other) {
            is PdfFont -> other.baseFont == this.baseFont
            else -> false
        }

    override fun hashCode(): Int = baseFont.hashCode()

}
