package mutnemom.android.kotlindemo.reader.pdf

import android.graphics.RectF
import android.util.Log
import androidx.annotation.IntRange
import mutnemom.android.kotlindemo.reader.pdf.decryptor.IdentityDecryptor
import mutnemom.android.kotlindemo.reader.pdf.decryptor.PdfDecryptor
import java.io.IOException
import java.nio.ByteBuffer
import java.nio.MappedByteBuffer
import java.util.*

class PdfFile(
    private var mappedByteBuffer: MappedByteBuffer,
    private val password: PdfPassword? = null
) {

    companion object {
        /* the end of line character */
        /* the comment text to begin the file to determine it's version */
        private const val VERSION_COMMENT = "%PDF-"
    }

    private var version = "1.1"
    private var majorVersion = 1
    private var minorVersion = 1

    /* The Info PdfObject, from the trailer, for simple metadata */
    private var info: PdfObject? = null

    /* the root PdfObject, as specified in the PDF file */
    private var root: PdfObject? = null

    /* the Encrypt PdfObject, from the trailer  */
    private var encrypt: PdfObject? = null

    /* the cross reference table mapping object numbers to locations in the PDF file */
    private var objIdx: MutableList<PdfXRef> = mutableListOf()

    /* a mapping of page numbers to parsed PDF commands */
    private var cache: PdfPageCache = PdfPageCache()


    // The number of pages in this PDF file.
    val pages: Int
        get() = try {
            root?.getDictRef("Pages")
                ?.getDictRef("Count")
                ?.getIntValue()
                ?: 0

        } catch (e: Throwable) {
            e.printStackTrace()
            0
        }

    /* The default decryptor for streams and strings.
     * By default, no encryption is expected, and thus the IdentityDecryptor is used.
     * */
    var defaultDecryptor: PdfDecryptor = IdentityDecryptor.INSTANCE

    /* whether the file is printable or not (trailer -> Encrypt -> P & 0x4) */
    private var printable = true

    /* whether the file is savable or not (trailer -> Encrypt -> P & 0x10) */
    private var savable = true

    init {
        try {
            readVersion()
                ?.also { Log.e("tt", "-> parse version: $it") }

            val startXRefPosition = readStartXRefPosition()
                .also { Log.e("tt", "-> parse startxref: $it") }

            readTrailer(startXRefPosition, password)

        } catch (e: Throwable) {
            e.printStackTrace()
        }
    }

    /* Get the page commands for a given page.
     *
     * @param pageNumber the index of the page to get commands for
     * @param wait       if true, do not exit until the page is complete.
     */
    fun getPage(pageNumber: Int, isWait: Boolean = true): PdfPage? =
        when (pageNumber) {
            in 0 until pages -> {
                var parser = cache.getPageParser(pageNumber)
                val page = cache.getPage(pageNumber) ?: try {

                    // parsing pdf pages tree
                    val topPageObj = root?.getDictRef("Pages")
                    val resources = mutableMapOf<String, PdfObject>()
                    findPage(topPageObj, 0, pageNumber, resources)?.let { pageObj ->
                        getContents(pageObj)?.let { stream ->
                            val newPageObj = createPage(pageNumber, pageObj)
                            parser = PdfParser(newPageObj, stream, resources)
                            cache.addPage(pageNumber, newPageObj, parser!!)
                            newPageObj
                        }
                    }
                } catch (e: Throwable) {
                    e.printStackTrace()
                    null
                }

                if (parser?.isFinished != true) {
                    parser?.go(isWait)
                }

                page
            }
            else -> null
        }

    /* get the stream representing the content of a particular page.
     *
     * @param pageObj the page object to get the contents of
     * @return a concatenation of any content streams for the requested
     * page.
     */
    @Throws(IOException::class)
    private fun getContents(pageObj: PdfObject): ByteArray? {
        // concatenate all the streams
        pageObj.getDictRef("Contents")?.getArray()
            ?.let { contents ->
                if (contents.isEmpty()) {
                    throw IOException("No page contents!")
                }

                // see if we have only one stream (the easy case)
                return when (contents.size) {
                    1 -> contents[0].getStream()
                    else -> {
                        // first get the total length of all the streams
                        val totalSize = contents.sumOf {
                            it.getStream()?.size
                                ?: throw IOException("No stream on content: $it")
                        }

                        // now assemble them all into one object
                        val stream = ByteArray(totalSize)
                        var length = 0
                        for (i in contents.indices) {
                            val data = contents[i].getStream()!!
                            System.arraycopy(data, 0, stream, length, data.size)
                            length += data.size
                        }

                        stream
                    }
                }
            } ?: throw IOException("No page contents!")
    }

    /* Get the PdfObject representing the content of a particular page.
     * Note that the number of the page need not have anything to do with the label on that page.
     * If there are two blank pages, and then roman numerals for the page number,
     * then passing in 6 will get page (VI).
     *
     * @param pageDict  the top of the pages tree
     * @param start     the page number of the first page in this dictionary
     * @param getPage   the number of the page to find; NOT the page's label.
     * @param resources a HashMap that will be filled with any resource
     *                  definitions encountered on the search for the page
     */
    @Throws(java.io.IOException::class)
    private fun findPage(
        pageDict: PdfObject?,
        start: Int,
        getPage: Int,
        resources: MutableMap<String, PdfObject>
    ): PdfObject? {
        var beginFindingIndex = start
        pageDict?.getDictRef("Resources")?.getDictionary()
            ?.also { resources.putAll(it) }

        return when (pageDict?.getDictRef("Type")?.getStringValue().equals("Page")) {
            true -> pageDict // we found our page!
            else -> pageDict?.getDictRef("Kids")?.let { kidsObj ->
                // find the first child for which (start + count) > getPage
                var page: PdfObject? = null
                val kids: Array<PdfObject> = kidsObj.getArray() ?: arrayOf()
                for (i in kids.indices) {
                    // BUG: some PDFs (T1Format.pdf) don't have the Type tag.
                    // use the Count tag to indicate a Pages dictionary instead.
                    var count = 1
                    kids[i].getDictRef("Count")?.also { count = it.getIntValue() }

                    if (beginFindingIndex + count >= getPage) {
                        page = findPage(kids[i], beginFindingIndex, getPage, resources)
                    }

                    beginFindingIndex += count
                }

                page
            }
        }
    }

    /* Find a property value in a page that may be inherited.
     * If the value is not defined in the page itself,
     * follow the page's "parent" links until
     * the value is found or the top of the tree is reached.
     *
     * @param pageObj  the object representing the page
     * @param propName the name of the property we are looking for
     */
    @Throws(IOException::class)
    private fun getInheritedValue(pageObj: PdfObject, propName: String): PdfObject? {
        // see if we have the property
        return pageObj.getDictRef(propName)
            ?: pageObj.getDictRef("Parent")
                ?.let { getInheritedValue(it, propName) }
        // recursively see if any of our parent have it
    }

    /* get a Rectangle2D.Float representation for a PdfObject
     * that is an array of four Numbers.
     *
     * @param obj a PdfObject that represents an Array of exactly four Numbers.
     */
    @Throws(IOException::class)
    fun parseRect(obj: PdfObject): RectF {
        return if (obj.type == PdfObject.ARRAY) {
            val bounds: Array<PdfObject> = obj.getArray() ?: arrayOf()
            if (bounds.size == 4) {
                RectF(
                    bounds[0].getFloatValue(),
                    bounds[1].getFloatValue(),
                    bounds[2].getFloatValue(),
                    bounds[3].getFloatValue()
                )
            } else {
                throw IOException("Rectangle definition didn't have 4 elements")
            }
        } else {
            throw IOException("Rectangle definition not an array")
        }
    }

    /* Create a PDF Page object by finding the relevant inherited properties
     *
     * @param pageObj the PDF object for the page to be created
     */
    @Throws(IOException::class)
    private fun createPage(pageNumber: Int, pageObj: PdfObject): PdfPage {
        var rotation = 0
        var mediabox: RectF? = null // second choice, if no crop
        var cropbox: RectF? = null // first choice

        getInheritedValue(pageObj, "MediaBox")
            ?.also { mediabox = parseRect(it) }

        getInheritedValue(pageObj, "CropBox")
            ?.also { cropbox = parseRect(it) }

        getInheritedValue(pageObj, "Rotate")
            ?.also { rotation = it.getIntValue() }

        val boundingBox = cropbox ?: mediabox!!
        return PdfPage(pageNumber, boundingBox, rotation, cache)
    }

    private fun readNum(buffer: ByteArray?, position: Int, size: Int): Int {
        var result = 0
        buffer?.also {
            for (i in 0 until size) {
                result = (result shl 8) + (it[position + i].toInt() and 0xff)
            }
        }
        return result
    }

    /* read a number.
     * The initial digit or . or - is passed in as the
     * argument.
     */
    private fun readNumber(charStart: Char): PdfObject? {
        // we've read the first digit (it's passed in as the argument)
        val isNegativeNumber = charStart == '-'
        var isStartWithDot = charStart == '.'
        var sawDotMultiplier: Double = if (isStartWithDot) 0.1 else 1.0
        var value: Double = if (charStart in '0'..'9') {
            (charStart - '0').toDouble()
        } else {
            0.0
        }

        while (true) {
            when (val char = mappedByteBuffer.get().toInt().toChar()) {
                in '0'..'9' -> {
                    val valueInt = char - '0'
                    if (isStartWithDot) {
                        value += valueInt * sawDotMultiplier
                        sawDotMultiplier *= 0.1
                    } else {
                        value = value * 10 + valueInt
                    }
                }

                '.' -> {
                    if (isStartWithDot) {
                        throw IOException("Can't have two '.' in a number")
                    }

                    isStartWithDot = true
                    sawDotMultiplier = 0.1
                }

                else -> {
                    mappedByteBuffer.position(mappedByteBuffer.position().dec())
                    break
                }
            }
        }

        if (isNegativeNumber) {
            value *= -1.0
        }

        return PdfObject(this, PdfObject.NUMBER, value)
    }

    /* Read a line of text.
     */
    private fun readLine(): String {
        val strBuffer = StringBuffer()
        while (mappedByteBuffer.remaining() > 0) {
            when (val char = mappedByteBuffer.get().toInt().toChar()) {
                '\r' -> {
                    if (mappedByteBuffer.remaining() > 0) {
                        val nextChar = mappedByteBuffer
                            .get(mappedByteBuffer.position()).toInt().toChar()

                        if (nextChar == '\n') {
                            mappedByteBuffer.get()
                        }
                    }
                    break
                }

                '\n' -> break
                else -> strBuffer.append(char)
            }
        }

        return strBuffer.toString()
    }

    /* read the next object with a special catch for numbers
     * @param numScan if true, don't bother trying to see if a number is
     *  an object reference (used when already in the middle of testing for
     *  an object reference, and not otherwise)
     * @param objNum the object number of the object containing the object
     *  being read; negative only if the object number is unavailable (e.g., if
     *  reading from the trailer, or reading at the top level, in which
     *  case we can expect to be reading an object description)
     * @param objGen the object generation of the object containing the object
     *  being read; negative only if the objNum is unavailable
     * @param decryptor the decryptor to use
     */
    private fun readObject(
        objNum: Int,
        objGen: Int,
        numScan: Boolean,
        decryptor: PdfDecryptor
    ): PdfObject {
        var cByte: Byte
        var obj: PdfObject? = null

        while (obj == null) {
            // check has remaining
            if (!mappedByteBuffer.hasRemaining()) {
                break
            }

            cByte = mappedByteBuffer.get()
            while (cByte.isWhiteSpace()) {
                cByte = mappedByteBuffer.get()
            }

            // check character for special punctuation:
            when {
                cByte.isChar('<') -> {
                    // could be start of <hex data>, or start of <<dictionary>>
                    cByte = mappedByteBuffer.get()
                    obj = when (cByte.isChar('<')) {
                        true -> readDictionary(objNum, objGen, decryptor)
                        else -> {
                            mappedByteBuffer.position(mappedByteBuffer.position().dec())
                            readHexString(objNum, objGen, decryptor)
                        }
                    }
                }

                cByte.isChar('(') -> obj = readLiteralString(objNum, objGen, decryptor)

                // it's an array
                cByte.isChar('[') -> obj = readArray(objNum, objGen, decryptor)

                // it's a name
                cByte.isChar('/') -> obj = readName()

                // it's a comment
                cByte.isChar('%') -> readLine()

                // it's a number
                cByte.isNumberOrSign() -> {
                    obj = readNumber(cByte.toInt().toChar())
                    if (!numScan) {
                        // It could be the start of a reference.
                        // Check to see if there's another number, then "R".
                        //
                        // We can't use mark/reset, since this could be called
                        // from dereference, which already is using a mark

                        val startPos: Int = mappedByteBuffer.position()
                        val testNum = readObject(-1, -1, true, decryptor)
                        if (testNum.type == PdfObject.NUMBER) {
                            val testR = readObject(-1, -1, true, decryptor)
                            if (testR.type == PdfObject.KEYWORD) {
                                when (testR.getStringValue()) {
                                    "obj" -> {
                                        // it's an object description
                                        obj = readObjectDescription(
                                            obj!!.getIntValue(),
                                            testNum.getIntValue(),
                                            decryptor
                                        )
                                    }
                                    "R" -> {
                                        // yup.  it's a reference.
                                        val xref = PdfXRef(
                                            obj!!.getIntValue(),
                                            testNum.getIntValue()
                                        )

                                        // Create a placeholder that will be dereferenced
                                        // as needed
                                        obj = PdfObject(this, PdfObject.INDIRECT, xref)
                                    }
                                    else -> mappedByteBuffer.position(startPos)
                                }
                            } else {
                                mappedByteBuffer.position(startPos)
                            }
                        } else {
                            mappedByteBuffer.position(startPos)
                        }
                    }
                }

                // it's a keyword
                cByte.isEngLetters() -> obj = readKeyword(cByte.toInt().toChar())

                // it's probably a closing character. throwback
                else -> {
                    mappedByteBuffer.position(mappedByteBuffer.position().dec())
                    break
                }
            }
        }

        return obj ?: PdfObject.NULL_OBJ
    }

    /* read a /name.  The / has already been read. */
    private fun readName(): PdfObject {
        // we've already read the / that begins the name.
        // all we have to check for is #hh hex notations.
        val stringBuffer = StringBuffer()
        var cByte: Byte = mappedByteBuffer.get()
        while (cByte.isRegularCharacter()) {

            // out-of-range, should have been hex
            if (cByte.toInt() < '!'.code && cByte.toInt() > '~'.code) break

            // H.3.2.4 indicates version 1.1 did not do hex escapes
            if (cByte.isChar('#') && (majorVersion != 1 && minorVersion != 1)) {
                val hex = readHexPair()
                if (hex >= 0) {
                    cByte = hex.toByte()
                } else {
                    throw IOException("Bad #hex in /Name")
                }
            }

            stringBuffer.append(cByte.toInt().toChar())
            cByte = mappedByteBuffer.get()
        }

        mappedByteBuffer.position(mappedByteBuffer.position().dec())
        return PdfObject(this, PdfObject.NAME, stringBuffer.toString())
    }

    /* read a bare keyword.
     * The initial character is passed in as the argument.
     * */
    private fun readKeyword(start: Char): PdfObject {
        // we've read the first character (it's passed in as the argument)
        val stringBuffer = StringBuffer(start.toString())
        var cByte: Byte = mappedByteBuffer.get()
        while (cByte.isRegularCharacter()) {
            stringBuffer.append(cByte.toInt().toChar())
            cByte = mappedByteBuffer.get()
        }

        mappedByteBuffer.position(mappedByteBuffer.position().dec())
        return PdfObject(this, PdfObject.KEYWORD, stringBuffer.toString())
    }

    /* return the 8-bit value represented by the next two hex characters.
     * If the next two characters don't represent a hex value, return -1
     * and reset the read head.  If there is only one hex character,
     * return its value as if there were an implicit 0 after it.
     */
    @IntRange(from = 0, to = 255)
    private fun readHexPair(): Int {
        val first = readHexDigit()
        val second = readHexDigit()

        // multiply by 2^4
        return first.shl(4) + second
    }

    /* read a character, and return its value as if it were a hexadecimal digit.
     *
     * @return a number between 0 and 15 whose value matches the next hexadecimal character.
     * Returns -1 if the next character isn't in [0-9a-fA-F]
     */
    @Throws(IllegalStateException::class)
    @IntRange(from = 0, to = 15)
    private fun readHexDigit(): Int {
        var cByte: Byte = mappedByteBuffer.get()

        while (cByte.isWhiteSpace()) {
            cByte = mappedByteBuffer.get()
        }

        return when (val char = cByte.toInt().toChar()) {
            '0', '1', '2', '3', '4', '5', '6', '7', '8', '9' -> (char - '0')
            'a', 'b', 'c', 'd', 'e', 'f' -> (char - 'a' + 10)
            'A', 'B', 'C', 'D', 'E', 'F' -> (char - 'A' + 10)
            else -> {
                mappedByteBuffer.position(mappedByteBuffer.position().dec())
                throw IllegalStateException("The next character isn't in [0-9a-fA-F]")
            }
        }
    }

    /* read an [ array ].  The initial [ has already been read.  PDFObjects are read until ].
     *
     * @param objNum    the object number of the object containing the dictionary
     *                  being read; negative only if the object number is unavailable, which
     *                  should only happen if we're reading an array placed directly
     *                  in the trailer
     * @param objGen    the object generation of the object containing the object
     *                  being read; negative only if the objNum is unavailable
     * @param decryptor the decryptor to use
     */
    private fun readArray(objNum: Int, objGen: Int, decryptor: PdfDecryptor): PdfObject {
        // we've already read the [.  Now read objects until ]
        val ary = arrayListOf<PdfObject>()
        var obj = readObject(objNum, objGen, false, decryptor)
        while (obj.type != PdfObject.NULL) {
            ary.add(obj)
            obj = readObject(objNum, objGen, false, decryptor)
        }

        // checking ending tag ']'
        if (!mappedByteBuffer.get().isChar(']')) {
            mappedByteBuffer.position(mappedByteBuffer.position().dec())
        }

        return PdfObject(this, PdfObject.ARRAY, ary.toTypedArray())
    }

    /* read the stream portion of a PDFObject.
     * Calls decodeStream to un-filter the stream as necessary.
     *
     * @param dict the dictionary associated with this stream.
     * @return a ByteBuffer with the encoded stream data
     */
    private fun readStream(dict: PdfObject): ByteBuffer {
        // pointer is at the start of a stream.
        // read the stream and decode,
        // based on the entries in the dictionary

        var length = -1
        val lengthObj = dict.getDictRef("Length")
            ?.also { length = it.getIntValue() }

        if (length < 0) {
            throw IOException("Unknown length for stream")
        }

        // slice the data
        val start: Int = mappedByteBuffer.position()
        val streamBuf: ByteBuffer = mappedByteBuffer.slice()
        streamBuf.limit(length)

        // move the current position to the end of the data
        mappedByteBuffer.position(mappedByteBuffer.position() + length)
        val ending: Int = mappedByteBuffer.position()

        if (!nextItemIs("endstream")) {
            Log.e(this::class.java.simpleName, "read $length chars from $start to $ending")
            throw IOException("Stream ended inappropriately")
        }

        return streamBuf
    }

    /* read a ( character string ).  The initial ( has already been read.
     * Read until a *balanced* ) appears.
    * */
    private fun readLiteralString(
        objNum: Int,
        objGen: Int,
        decryptor: PdfDecryptor
    ): PdfObject {
        var cByte: Byte

        // we've already read the (.  now get the characters until a
        // *balanced* ) appears.  Translate \r \n \t \b \f \( \) \\ \ddd
        // if a cr/lf follows a backslash, ignore the cr/lf
        var openParenthesisCount = 1
        val stringBuffer = StringBuffer()

        while (openParenthesisCount > 0) {
            cByte = (mappedByteBuffer.get().toInt() and 0xFF).toByte()

            // process unescaped parenthesis
            if (cByte.isChar('(')) {
                openParenthesisCount++

            } else if (cByte.isChar(')')) {
                openParenthesisCount--
                if (openParenthesisCount == 0) {
                    cByte = (-1).toByte()
                    break
                }

            } else if (cByte.isChar('\\')) {
                // grab the next character to see what we're dealing with
                cByte = (mappedByteBuffer.get().toInt() and 0xFF).toByte()
                cByte = when {
                    cByte.toInt() >= '0'.code && cByte < '8'.code -> {
                        // \ddd form - one to three OCTAL digits
                        var count = 0
                        var value = 0
                        while (cByte.toInt() >= '0'.code && cByte.toInt() < '8'.code && count < 3) {
                            value = value * 8 + cByte.toInt() - '0'.code
                            cByte = (mappedByteBuffer.get().toInt() and 0xFF).toByte()
                            count++
                        }

                        // we'll have read one character too many
                        mappedByteBuffer.position(mappedByteBuffer.position().dec())
                        value.toByte()
                    }

                    cByte.isChar('n') -> '\n'.code.toByte()
                    cByte.isChar('r') -> '\r'.code.toByte()
                    cByte.isChar('t') -> '\t'.code.toByte()
                    cByte.isChar('b') -> '\b'.code.toByte()
                    cByte.isChar('f') -> '\u000c'.code.toByte() // \f, 12
                    cByte.isChar('\r') -> {
                        // escaped CR to be ignored, look for a following LF
                        cByte = (mappedByteBuffer.get().toInt() and 0xFF).toByte()
                        if (cByte.toInt() != '\n'.code) {
                            // not an LF, we'll consume this character on the next iteration
                            mappedByteBuffer.position(mappedByteBuffer.position().dec())
                        }

                        (-1).toByte()
                    }

                    // escaped LF to be ignored
                    cByte.isChar('\n') -> (-1).toByte()

                    else -> cByte
                }
            }

            if (cByte >= 0) {
                stringBuffer.append(cByte.toInt().toChar())
            }
        }

        return PdfObject(
            this,
            PdfObject.STRING,
            decryptor.decryptString(objNum, objGen, stringBuffer.toString())
        )
    }

    /* read an entire PDFObject.  The intro line, which looks something
     * like "4 0 obj" has already been read.
     *
     * @param objNum    the object number of the object being read, being
     *                  the first number in the intro line (4 in "4 0 obj")
     * @param objGen    the object generation of the object being read, being
     *                  the second number in the intro line (0 in "4 0 obj").
     * @param decryptor the decryptor to use
     */
    private fun readObjectDescription(
        objNum: Int,
        objGen: Int,
        decryptor: PdfDecryptor
    ): PdfObject {
        // we've already read the 4 0 obj bit.
        // Next thing up is the object.
        // object descriptions end with the keyword endobj

        val debugPos: Int = mappedByteBuffer.position()
        val obj = readObject(objNum, objGen, false, decryptor)

        // see if it's a dictionary.
        // If so, this could be a stream.
        var endKey = readObject(objNum, objGen, false, decryptor)
        if (endKey.type != PdfObject.KEYWORD) {
            throw IOException("Expected 'stream' or 'endobj'")
        }

        if (obj.type == PdfObject.DICTIONARY && endKey.getStringValue() == "stream") {
            // skip until we see \n
            readLine()
            val data = readStream(obj) ?: ByteBuffer.allocate(0)
            obj.setStream(data)
            endKey = readObject(objNum, objGen, false, decryptor)
        }

        // at this point, obj is the object, keyword should be "endobj"
        val endCheck = endKey.getStringValue() ?: ""
        if (endCheck != "endobj") {
            Log.w(this::class.java.simpleName, "object at $debugPos didn't end with 'endobj'")
        }

        obj.setObjectId(objNum, objGen)
        return obj
    }

    /* read an entire &lt;&lt; dictionary &gt;&gt;.  The initial
     * &lt;&lt; has already been read.
     *
     * @param objNum    the object number of the object containing the dictionary
     *                  being read; negative only if the object number is unavailable, which
     *                  should only happen if we're reading a dictionary placed directly
     *                  in the trailer
     * @param objGen    the object generation of the object containing the object
     *                  being read; negative only if the objNum is unavailable
     * @param decryptor the decryptor to use
     * @return the Dictionary as a PDFObject.
     */
    private fun readDictionary(objNum: Int, objGen: Int, decryptor: PdfDecryptor): PdfObject {
        val mapDict = hashMapOf<String, PdfObject>()

        // we've already read the <<.  Now get /Name obj pairs until >>
        var name: PdfObject = readObject(objNum, objGen, false, decryptor)
        while (name.type != PdfObject.NULL) {
            // make sure first item is a NAME
            if (name.type != PdfObject.NAME) {
                throw IOException("First item in dictionary must be a /Name.  (Was $name)")
            }

            name.getStringValue()?.also { key ->
                mapDict[key] = readObject(objNum, objGen, false, decryptor)
            }

            name = readObject(objNum, objGen, false, decryptor)
        }

        if (!nextItemIs(">>")) {
            throw IOException("End of dictionary wasn't '>>'")
        }

        return PdfObject(this, PdfObject.DICTIONARY, mapDict)
    }

    /* read a < hex string >.  The initial < has already been read.
     *
     * @param objNum    the object number of the object containing the dictionary
     *                  being read; negative only if the object number is unavailable, which
     *                  should only happen if we're reading a string placed directly
     *                  in the trailer
     * @param objGen    the object generation of the object containing the object
     *                  being read; negative only if the objNum is unavailable
     * @param decryptor the decryptor to use
     */
    private fun readHexString(objNum: Int, objGen: Int, decryptor: PdfDecryptor): PdfObject {
        // we've already read the <. Now get the hex bytes until >
        val strBuf = StringBuffer()
        var cByte = mappedByteBuffer.get()
        while (!cByte.isChar('>')) {
            // step back 1 position
            mappedByteBuffer.position(mappedByteBuffer.position().dec())

            strBuf.append(readHexPair())
            cByte = mappedByteBuffer.get()
        }

        val decryptedStr = decryptor.decryptString(objNum, objGen, strBuf.toString())
        return PdfObject(this, PdfObject.STRING, decryptedStr)
    }

    /* read the cross reference table from a PDF file.  When this method
     * is called, the file pointer must point to the start of the word
     * "xref" in the file.  Reads the xref table and the trailer dictionary.
     * If dictionary has a /Prev entry, move file pointer
     * and read new trailer
     *
     * @param password
     */
    private fun readTrailer(startXRef: Int, password: PdfPassword? = null) {
        mappedByteBuffer.position(startXRef)

        // the table of XRefs
        val pos = mappedByteBuffer.position()
        var newDefaultDecryptor: PdfDecryptor? = null

        // read a bunch of nested trailer tables
        while (true) {
            // make sure we are looking at an XRef table
            if (!nextItemIs("xref")) {
                mappedByteBuffer.position(pos)
                readTrailer15(password)
                return
            }

            while (true) {
                // read until the word "trailer"
                var obj = readObject(-1, -1, false, defaultDecryptor)

                if (obj.type == PdfObject.KEYWORD && obj.getStringValue() == "trailer") {
                    break
                }

                // read the starting position of the reference
                if (obj.type != PdfObject.NUMBER) {
                    throw IOException("Expected number for first xref entry, found: ${obj.type}")
                }
                val refStart = obj.getIntValue()

                // read the size of the reference table
                obj = readObject(-1, -1, false, defaultDecryptor)
                if (obj.type != PdfObject.NUMBER) {
                    throw IOException("Expected number for length of xref table")
                }
                val refLength = obj.getIntValue()

                // skip a line
                readLine()

                // read reference lines
                for (refId in refStart until (refStart + refLength)) {
                    // each reference line is 20 bytes long
                    val refLine = ByteArray(20)
                    mappedByteBuffer.get(refLine)

                    // ignore this line if the object ID is already defined
                    if (objIdx.getOrNull(refId) == null) {
                        // see if it's an active object
                        when (refLine.getOrNull(17)?.toInt()?.toChar()) {
                            'n', 'f' -> objIdx.add(refId, PdfXRef(line = refLine))
                            else -> objIdx.add(refId, PdfXRef())
                        }
                    }
                }
            }

            // at this point, the "trailer" word (not EOL) has been read.
            val trailerDict =
                readObject(-1, -1, false, defaultDecryptor)

            if (trailerDict.type != PdfObject.DICTIONARY) {
                throw IOException("Expected dictionary after \"trailer\"")
            }

            // read the root object location
            if (root == null) {
                root = trailerDict.getDictRef("Root")
                if (root != null) {
                    root!!.setObjectId(PdfObject.OBJ_NUM_TRAILER, PdfObject.OBJ_NUM_TRAILER)
                }
            }

            // read the encryption information
            if (encrypt == null) {
                encrypt = trailerDict.getDictRef("Encrypt")
                if (encrypt != null) {
                    encrypt?.setObjectId(PdfObject.OBJ_NUM_TRAILER, PdfObject.OBJ_NUM_TRAILER)
                }

//                newDefaultDecryptor = PdfDecryptorFactory.createDecryptor(
//                    encrypt,
//                    trailerDict.getDictRef("ID"),
//                    password
//                )
            }

            if (info == null) {
                info = trailerDict.getDictRef("Info")
                if (info != null) {
                    if (!info!!.isIndirect) {
                        throw IOException("Info in trailer must be an indirect reference")
                    }

                    info!!.setObjectId(PdfObject.OBJ_NUM_TRAILER, PdfObject.OBJ_NUM_TRAILER)
                }
            }

            val xRefStmPos = trailerDict.getDictRef("XRefStm")
            if (xRefStmPos != null) {
                val pos14 = mappedByteBuffer.position()
                mappedByteBuffer.position(xRefStmPos.getIntValue())
                readTrailer15(password)
                mappedByteBuffer.position(pos14)
            }

            // read the location of the previous XRef table
            val prevLoc = trailerDict.getDictRef("Prev")
            if (prevLoc != null) {
                mappedByteBuffer.position(prevLoc.getIntValue())
            } else {
                break
            }

            // see if we have an optional Version entry
            root?.getDictRef("Version")
                ?.getStringValue()
                ?.also { version -> processVersion(version) }
        }

        // make sure we found a root
        if (root == null) {
            throw IOException("No /Root key found in trailer dictionary")
        }

        // check what permissions are relevant
        encrypt?.also {
            val permission = it.getDictRef("P")
            if (permission != null && newDefaultDecryptor?.isOwnerAuthorised() != true) {
                val perms = permission.getIntValue()
                printable = (perms and 4) != 0
                savable = (perms and 16) != 0
            }

            // Install the new default decryptor only after the trailer has been read,
            // as nothing we're reading passing through is encrypted
            newDefaultDecryptor
                ?.also { decryptor -> defaultDecryptor = decryptor }
        }

        // dereference the root object
        root?.dereference()
    }

    /* read the cross reference table from a PDF file.
     * When this method is called,
     * the file pointer must point to the start of the word "xref" in the file.
     * Reads the xref table and the trailer dictionary.
     * If dictionary has a /Prev entry, move file pointer and read new trailer
     *
     * @param password
     */
    fun readTrailer15(password: PdfPassword? = null) {
        Log.d("tt", "readTrailer15() called with: password = ${password?.passwordString}")

//        // the table of xrefs is already initialized and perhaps filled in readTrailer()
//        // objIdx = new PDFXref[50];
//        var newDefaultDecryptor: PdfDecryptor? = null
//
//        while (true) {
//            val xRefObj = readObject(-1, -1, false, defaultDecryptor)
//            val wNum = xRefObj.getDictionary()?.get("W")?.getArray()
//            val l1 = wNum?.get(0)?.getIntValue() ?: 0
//            val l2 = wNum?.get(1)?.getIntValue() ?: 0
//            val l3 = wNum?.get(3)?.getIntValue() ?: 0
//            val entrySize = l1 + l2 + l3
//
//            val size = xRefObj.getDictionary()?.get("Size")?.getIntValue() ?: 0
//            val streamBuf = xRefObj.getStream()
//            var streamPos = 0
//
//            val idxNum = xRefObj.getDictionary()?.get("Index")
//            var idxArray: IntArray = intArrayOf()
//            if (idxNum == null) {
//                idxArray = intArrayOf(0, size)
//            } else {
//                idxNum.getArray()?.also { idxNumArr ->
//                    idxArray = IntArray(idxNumArr.size)
//                    for (i in idxNumArr.indices) {
//                        idxArray[i] = idxNumArr[i].getIntValue()
//                    }
//                }
//            }
//
//            val idxLength = idxArray.size
//            var idxPos = 0
//            while (idxPos < idxLength) {
//                var refStart = idxArray[idxPos++]
//                var refLength = idxArray[idxPos++]
//
//                // read reference lines
//                for (refId in refStart until (refStart + refLength)) {
//                    val type = readNum(streamBuf, streamPos, l1)
//                    streamPos += l1
//
//                    val id = readNum(streamBuf, streamPos, l2)
//                    streamPos += l2
//
//                    val gen = readNum(streamBuf, streamPos, l3)
//                    streamPos += l3
//
//                    // ignore this line if the object ID is already defined
//                    if (objIdx[refId] != null) {
//                        continue
//                    }
//
//                    // see if it's an active object
//                    when (type) {
//                        0 -> objIdx[refId] = PdfXRef(line = null)
//                        1 -> objIdx[refId] = PdfXRef(id, gen)
//                        else -> objIdx[refId] = PdfXRef(id, gen, true)
//                    }
//                }
//            }
//
//            val trailerDict = xRefObj.getDictionary()
//
//            // read the root object location
//            if (root == null) {
//                root = trailerDict?.get("Root")
//                if (root != null) {
//                    root!!.setObjectId(PdfObject.OBJ_NUM_TRAILER, PdfObject.OBJ_NUM_TRAILER)
//                }
//            }
//
//            // read the encryption information
//            if (encrypt == null) {
//                encrypt = trailerDict?.get("Encrypt")
//                if (encrypt != null) {
//                    encrypt!!.setObjectId(PdfObject.OBJ_NUM_TRAILER, PdfObject.OBJ_NUM_TRAILER)
//                }
//
////                newDefaultDecryptor =
////                    PdfDecryptorFactory.createDecryptor(encrypt, trailerDict?.get("ID"), password)
//            }
//
//            if (info == null) {
//                info = trailerDict?.get("Info")
//                if (info != null) {
//                    if (!info!!.isIndirect) {
//                        throw IOException("Info in trailer must be an indirect reference")
//                    }
//
//                    info!!.setObjectId(PdfObject.OBJ_NUM_TRAILER, PdfObject.OBJ_NUM_TRAILER)
//                }
//            }
//
//            // read the location of the previous XRef table
//            val prevLoc = trailerDict?.get("Prev")
//            if (prevLoc != null) {
//                mappedByteBuffer.position(prevLoc.getIntValue())
//            } else {
//                break
//            }
//
//            // see if we have an optional version entry
//            if (root?.getDictRef("Version") != null) {
//                root?.getDictRef("Version")?.getStringValue()
//                    ?.also { processVersion(it) }
//            }
//        }
//
//        // make sure we found a root
//        if (root == null) {
//            throw IOException("No /Root key found in trailer dictionary")
//        }
//
//        // check what permissions ar relevant
//        if (encrypt != null) {
//            val permissions = encrypt!!.getDictRef("P")
//            if (permissions != null && newDefaultDecryptor?.isOwnerAuthorised() != true) {
//                val perms = permissions?.getIntValue() ?: 0
//                if (permissions != null) {
//                    printable = (perms and 4) != 0
//                    savable = (perms and 16) != 0
//                }
//            }
//
//            // Install the new default decryptor only after the trailer has been read,
//            // as nothing we're reading passing through is encrypted
//            newDefaultDecryptor?.also { defaultDecryptor = it }
//        }
//
//        // dereference the root object
//        root?.dereference()
    }

    /* Used internally to track down PdfObject references.
     * You should never need to call this.
     *
     * Since this is the only public method for tracking down PDF objects, it is synchronized.
     * This means that the PdfFile can only hunt down one object at a time,
     * preventing the file's location from getting messed around.
     *
     * This call stores the current buffer position before any changes are made
     * and restores it afterwards, so callers need not know that the position
     * has changed.
     *
     */
    @Synchronized
    fun dereference(objNum: Int, objGen: Int, decryptor: PdfDecryptor): PdfObject =
        objIdx.getOrNull(objNum)
            ?.let { xRefObj ->
                // check to see if this is already dereferenced
                xRefObj.reference?.get() ?: run {
                    // store the current position in the backup variable
                    val backupPosition: Int = mappedByteBuffer.position()

                    val deRefObj = if (xRefObj.compressed) {
                        dereferenceCompressed(objNum, decryptor)
                    } else {
                        val location = xRefObj.fileLocation
                        when {
                            location < 0 -> PdfObject.NULL_OBJ
                            else -> {
                                // move to where this object is
                                mappedByteBuffer.position(location)

                                // read the object and cache the reference
                                readObject(objNum, objGen, false, decryptor)
                            }
                        }
                    }

                    // reset to the previous position
                    mappedByteBuffer.position(backupPosition)

                    deRefObj
                }
            } ?: PdfObject.NULL_OBJ

    private fun dereferenceCompressed(index: Int, decryptor: PdfDecryptor): PdfObject {
        var obj: PdfObject = PdfObject.NULL_OBJ
        val compressId = objIdx[index]?.id ?: -1
        val idx = objIdx[index]?.generation ?: -1

        if (idx < 0) {
            obj = PdfObject.NULL_OBJ

        } else {
            val compObj = dereference(compressId, 0, decryptor)

            val first = compObj?.getDictionary()?.get("First")?.getIntValue() ?: -1
//            val length = compObj?.getDictionary()?.get("Length")?.getIntValue() ?: -1
            val n = compObj?.getDictionary()?.get("N")?.getIntValue() ?: -1

            if (idx >= n) {
                obj = PdfObject.NULL_OBJ

            } else {
                compObj?.getStreamBuffer()?.also { streamBuf ->
                    val oldBuffer = mappedByteBuffer
                    mappedByteBuffer = streamBuf

                    // skip other nums
                    for (i in 0 until idx) {
                        // skip1Num
                        readObject(-1, -1, true, decryptor)
                        // skip2Num
                        readObject(-1, -1, true, decryptor)
                    }

                    val objNumPO =
                        readObject(-1, -1, true, decryptor)

                    val offsetPO =
                        readObject(-1, -1, true, decryptor)

                    val objNum = objNumPO.getIntValue()
                    val offset = offsetPO.getIntValue()

                    if (objNum != index) {
                        obj = PdfObject.NULL_OBJ

                    } else {
                        mappedByteBuffer.position(first + offset)
                        obj = readObject(objNum, 0, false, decryptor)
                        mappedByteBuffer = oldBuffer
                    }
                } ?: run { obj = PdfObject.NULL_OBJ }
            }
        }

        return obj
    }

    private fun nextItemIs(match: String): Boolean {
        var cByte = mappedByteBuffer.get()

        // skip whitespace
        while (cByte.isWhiteSpace()) {
            cByte = mappedByteBuffer.get()
        }

        var matchingResult = true
        for (i in match.indices) {
            if (i > 0) {
                cByte = mappedByteBuffer.get()
            }

            if (cByte.toInt().toChar() != match[i]) {
                matchingResult = false
                break
            }
        }

        return matchingResult
    }


    // ------------------------------------------------------>

    private fun processVersion(versionString: String) {
        try {
            val tokens = StringTokenizer(versionString, ".")
            majorVersion = tokens.nextToken().toInt()
            minorVersion = tokens.nextToken().toInt()
            version = versionString
        } catch (e: Throwable) {
            e.printStackTrace()
        }
    }

    private fun readVersion(): String? = try {
        // start at the beginning of the file
        mappedByteBuffer.rewind()

        val versionLine = readLine()
        when (versionLine.startsWith(VERSION_COMMENT)) {
            true -> versionLine
                .substring(VERSION_COMMENT.length)
                .also { processVersion(it) }

            else -> null
        }
    } catch (e: Throwable) {
        e.printStackTrace()
        null
    }

    private fun readStartXRefPosition(): Int = try {
        mappedByteBuffer.rewind()

        // backup about 32 characters from the end of the file to find 'startxref\n'
        val scan = ByteArray(32)
        var scanPos = mappedByteBuffer.remaining() - scan.size
        var loc = 0

        while (scanPos >= 0) {
            mappedByteBuffer.position(scanPos)
            mappedByteBuffer.get(scan)

            // find 'startxref' in scan
            val scans = String(scan)
            loc = scans.indexOf("startxref")
            if (loc > 0) {
                if (scanPos + loc + scan.size <= mappedByteBuffer.limit()) {
                    scanPos += loc
                    loc = 0
                }

                break
            }

            scanPos -= scan.size - 10
        }

        if (scanPos < 0) {
            throw IOException("This may not be a PDF File")
        }

        mappedByteBuffer.position(scanPos)
        mappedByteBuffer.get(scan)
        val scans = String(scan)

        // skip over 'startxref' and first EOL char
        loc += 10

        // skip over possible 2nd EOL char
        if (scans[loc].code < 32) {
            ++loc
        }

        // skip over possible leading blanks
        while (scans[loc].code == 32) {
            ++loc
        }

        // read number
        val numStart = loc
        while (loc < scans.length && scans[loc] >= '0' && scans[loc] <= '9') {
            ++loc
        }

        scans.substring(numStart, loc).toIntOrNull() ?: -1
    } catch (e: Throwable) {
        e.printStackTrace()
        -1
    }

}
