package mutnemom.android.kotlindemo.reader.pdf

import android.graphics.Matrix
import android.graphics.Path
import android.graphics.Path.FillType
import android.util.Log
import mutnemom.android.kotlindemo.reader.pdf.color.PdfColorSpace
import mutnemom.android.kotlindemo.reader.pdf.commands.PdfShapeCmd
import java.io.IOException
import java.lang.ref.WeakReference
import java.nio.ByteBuffer
import java.util.*

class PdfParser(
    command: PdfPage,
    private val stream: ByteArray,
    private var resources: MutableMap<String, PdfObject>? = null
) : BaseWatchable() {

    companion object {
        const val RELEASE = true

        const val PDF_CMDS_RANGE1_MIN = 1
        const val PDF_CMDS_RANGE1_MAX = Int.MAX_VALUE
        const val PDF_CMDS_RANGE2_MIN = 0
        const val PDF_CMDS_RANGE2_MAX = 0
        const val PDF_CMDS_RANGE3_MIN = 0
        const val PDF_CMDS_RANGE3_MAX = 0
    }

    // the current render state
    private var state: ParserState? = null

    private var parserStates: Stack<ParserState>? = null
    private var stack: Stack<Any>? = null
    private var path: Path? = null
    private var clip = 0
    private var loc = 0

    /* a weak reference to the page we render into.
     * For the page to remain available,
     * some other code must retain a strong reference to it.
     */
    private var pageRef: WeakReference<PdfPage> = WeakReference(command)

    /** the actual command, for use within a singe iteration.  Note that
     * this must be released at the end of each iteration to assure the
     * page can be collected if not in use
     */
    private var command: PdfPage? = null
    private var cmdCount = 0


    private var catchExceptions = false // Indicates state of BX...EX

    private val logTag: String
        get() = this::class.java.simpleName

    init {
        resources ?: run { resources = HashMap() }
    }

    /* Called to prepare for some iterations */
    override fun setup() {
        Log.d(this::class.java.simpleName, "setup() called")

        stack = Stack<Any>()
        parserStates = Stack<ParserState>()
        path = Path()
        loc = 0
        clip = 0

        //initialize the ParserState
        state = ParserState().apply {
            fillCS = PdfColorSpace.getColorSpace(PdfColorSpace.COLOR_SPACE_GRAY)
            strokeCS = PdfColorSpace.getColorSpace(PdfColorSpace.COLOR_SPACE_GRAY)
            textFormat = PdfTextFormat()
        }
    }

    override fun iterate(): Int {
        Log.d(this::class.java.simpleName, "iterate() called")

        // make sure the page is still available,
        // and create the reference to it for use within this iteration
        return pageRef.get()
            ?.apply { command = this }
            ?.let {
                parseObject()
                    ?.let { obj ->
                        var watchableState = Watchable.RUNNING

                        if (obj is PdfStreamToken) {
                            // it's a command.  figure out what to do.
                            // (if not, the token will be "pushed" onto the stack)
                            val cmd: String = obj.name ?: ""
                            if (!RELEASE) {
                                cmdCount++
                                if (!(cmdCount in PDF_CMDS_RANGE1_MIN..PDF_CMDS_RANGE1_MAX
                                            || cmdCount in PDF_CMDS_RANGE2_MIN..PDF_CMDS_RANGE2_MAX
                                            || cmdCount in PDF_CMDS_RANGE3_MIN..PDF_CMDS_RANGE3_MAX)
                                ) {
                                    stack?.setSize(0)
                                    watchableState = Watchable.RUNNING
                                }

                                Log.i(
                                    this::class.java.simpleName,
                                    "Command [$cmdCount]: $cmd " +
                                            "(stack size is ${stack?.size}: ${dump(stack)})"
                                )
                            }

                            try {
                                val condition =
                                    (if (cmd.isNotEmpty()) cmd[0].code else 0) +
                                            (if (cmd.length > 1) cmd[1].code shl 8 else 0) +
                                            if (cmd.length > 2) cmd[2].code shl 16 else 0

                                when (condition) {
                                    'E'.code + ('M'.code shl 8) + ('C'.code shl 16),
                                    'r'.code + ('i'.code shl 8) -> {
                                        // no implement
                                        Log.e(
                                            logTag,
                                            "-> iterate found: ${condition.toChar()}, $condition"
                                        )
                                    }

                                    'Q'.code -> processQCmd()
                                    'w'.code -> command?.addStrokeWidth(popFloat())
                                    'J'.code -> command?.addEndCap(popInt())
                                    'j'.code -> command?.addLineJoin(popInt())
                                    'M'.code -> command?.addMiterLimit(popFloat()) // popInt()
                                    'i'.code -> popFloat()
                                    'h'.code -> path?.close()
                                    'W'.code -> clip = PdfShapeCmd.CLIP
                                    'g'.code + ('s'.code shl 8) -> setGSState(popString())

                                    'Q'.code + ('q'.code shl 8) -> {
                                        processQCmd()
                                        // 'q'-cmd
                                        //   push the parser state
                                        parserStates!!.push(state!!.clone() as ParserState)
                                        //   push graphics state
                                        command?.addPush()
                                    }
                                    'Q'.code + ('B'.code shl 8) + ('T'.code shl 16) -> {
                                        processQCmd()
                                        processBTCmd()
                                    }
                                    'd'.code + ('1'.code shl 8) -> popFloat(6)
                                    'd'.code + ('0'.code shl 8) -> popFloat(2)
                                    'B'.code + ('D'.code shl 8) + ('C'.code shl 16) -> {
                                        val ref = stack!!.pop()
                                        popString()
                                    }
                                    'B'.code + ('M'.code shl 8) + ('C'.code shl 16) -> popString()
                                    'D'.code + ('P'.code shl 8) -> {
                                        val ref = stack!!.pop()
                                        popString()
                                    }
                                    'M'.code + ('P'.code shl 8) -> popString()
                                    'E'.code + ('X'.code shl 8) -> catchExceptions = false
                                    'B'.code + ('X'.code shl 8) -> catchExceptions = true
                                    'B'.code + ('I'.code shl 8) -> parseInlineImage()
                                    'T'.code + ('J'.code shl 8) -> {
                                        state?.textFormat?.doText(command, popArray())
                                    }
//                                    '\"'.code -> {
//                                        // draw string on new line with char & word spacing:
//                                        // aw Tw ac Tc string '
//                                        val string: String = popString()
//                                        val ac: Float = popFloat()
//                                        val aw: Float = popFloat()
//                                        state!!.textFormat.setWordSpacing(aw)
//                                        state!!.textFormat.setCharSpacing(ac)
//                                        state!!.textFormat.doText(cmds, string)
//                                    }
//                                    '\''.code -> {
//                                        state!!.textFormat.carriageReturn()
//                                        state!!.textFormat.doText(cmds, popString())
//                                    }
//                                    'T'.code + ('j'.code shl 8) -> {
//                                        state!!.textFormat.doText(cmds, popString())
//                                    }
//                                    'T'.code + ('*'.code shl 8) -> {
//                                        state!!.textFormat.carriageReturn()
//                                    }
//                                    'T'.code + ('m'.code shl 8) -> {
//                                        state!!.textFormat.setMatrix(popFloat(6))
//                                    }
//                                    'T'.code + ('D'.code shl 8) -> {
//                                        val y: Float = popFloat()
//                                        val x: Float = popFloat()
//                                        state!!.textFormat.setLeading(-y)
//                                        state!!.textFormat.carriageReturn(x, y)
//                                    }
//                                    'T'.code + ('d'.code shl 8) -> {
//                                        val y: Float = popFloat()
//                                        val x: Float = popFloat()
//                                        state!!.textFormat.carriageReturn(x, y)
//                                    }
//                                    'T'.code + ('s'.code shl 8) -> {
//                                        state!!.textFormat.setRise(popFloat())
//                                    }
//                                    'T'.code + ('r'.code shl 8) -> {
//                                        state!!.textFormat.setMode(popInt())
//                                    }
//                                    'T'.code + ('f'.code shl 8) -> {
//                                        val size = popFloat()
//                                        val fontRef = popString()
//                                        state!!.textFormat!!.setFont(getFontFrom(fontRef), size)
//                                    }
//                                    'T'.code + ('L'.code shl 8) -> {
//                                        state!!.textFormat.setLeading(popFloat())
//                                    }
//                                    'T'.code + ('z'.code shl 8) -> {
//                                        state!!.textFormat.setHorizontalScale(popFloat())
//                                    }
//                                    'T'.code + ('w'.code shl 8) -> {
//                                        state!!.textFormat.setWordSpacing(popFloat())
//                                    }
//                                    'T'.code + ('c'.code shl 8) -> {
//                                        state!!.textFormat.setCharSpacing(popFloat())
//                                    }
//                                    'E'.code + ('T'.code shl 8) -> state?.textFormat?.end()
//                                    'B'.code + ('T'.code shl 8) -> processBTCmd()
//                                    'D'.code + ('o'.code shl 8) -> {
//                                        val xObj = findResource(popString(), "XObject")
//                                        doXObject(xObj)
//                                    }
                                    'k'.code -> {
                                        state!!.fillCS = PdfColorSpace
                                            .getColorSpace(PdfColorSpace.COLOR_SPACE_CMYK)

                                        state!!.fillCS!!.getFillPaint(popFloat(4))
                                            ?.also { paint -> command?.addFillPaint(paint) }
                                    }
                                    'K'.code -> {
                                        state!!.strokeCS = PdfColorSpace
                                            .getColorSpace(PdfColorSpace.COLOR_SPACE_CMYK)

                                        state!!.strokeCS!!.getPaint(popFloat(4))
                                            ?.also { paint -> command?.addStrokePaint(paint) }
                                    }
                                    'r'.code + ('g'.code shl 8) -> {
                                        state!!.fillCS = PdfColorSpace
                                            .getColorSpace(PdfColorSpace.COLOR_SPACE_RGB)

                                        state!!.fillCS!!.getFillPaint(popFloat(3))
                                            ?.also { paint -> command?.addFillPaint(paint) }
                                    }
                                    'R'.code + ('G'.code shl 8) -> {
                                        state!!.strokeCS = PdfColorSpace
                                            .getColorSpace(PdfColorSpace.COLOR_SPACE_RGB)

                                        state!!.strokeCS!!.getPaint(popFloat(3))
                                            ?.also { paint -> command?.addStrokePaint(paint) }
                                    }
                                    'g'.code -> {
                                        state!!.fillCS = PdfColorSpace
                                            .getColorSpace(PdfColorSpace.COLOR_SPACE_GRAY)

                                        state!!.fillCS!!.getFillPaint(popFloat(1))
                                            ?.also { paint -> command?.addFillPaint(paint) }
                                    }
                                    'G'.code -> {
                                        state!!.strokeCS = PdfColorSpace
                                            .getColorSpace(PdfColorSpace.COLOR_SPACE_GRAY)

                                        state!!.strokeCS!!.getPaint(popFloat(1))
                                            ?.also { paint -> command?.addStrokePaint(paint) }
                                    }
                                    's'.code + ('c'.code shl 8) + ('n'.code shl 16) -> {
                                        val n = state!!.fillCS!!.getNumComponents()
                                        state!!.fillCS!!.getFillPaint(popFloat(n))
                                            ?.also { paint -> command?.addFillPaint(paint) }
                                    }
                                    's'.code + ('c'.code shl 8) -> {
                                        val n = state!!.fillCS!!.getNumComponents()
                                        state!!.fillCS!!.getFillPaint(popFloat(n))
                                            ?.also { paint -> command?.addFillPaint(paint) }
                                    }
                                    'S'.code + ('C'.code shl 8) + ('N'.code shl 16) -> {
                                        val n = state!!.strokeCS!!.getNumComponents()
                                        state!!.strokeCS!!.getPaint(popFloat(n))
                                            ?.also { paint -> command?.addStrokePaint(paint) }
                                    }
                                    'S'.code + ('C'.code shl 8) -> {
                                        val n = state!!.strokeCS!!.getNumComponents()
                                        state!!.strokeCS!!.getPaint(popFloat(n))
                                            ?.also { paint -> command?.addStrokePaint(paint) }
                                    }
//                                    'C'.code + ('S'.code shl 8) -> {
//                                        state?.strokeCS = parseColorSpace(PdfObject(stack?.pop()))
//                                    }
//                                    'c'.code + ('s'.code shl 8) -> {
//                                        state?.fillCS = parseColorSpace(PdfObject(stack?.pop()))
//                                    }
//                                    's'.code + ('h'.code shl 8) -> {
//                                        // shade a region that is defined by the shader itself.
//                                        // shading the current space from a dictionary
//                                        // should only be used for limited-dimension shadings
//                                        val gDictName: String = popString()
//                                        // set up the pen to do a gradient fill according
//                                        // to the dictionary
//                                        val shadingObj = findResource(gDictName, "Shading")
//                                        doShader(shadingObj)
//                                    }
                                    'W'.code + ('*'.code shl 8) -> {
                                        path?.fillType = FillType.EVEN_ODD
                                        clip = PdfShapeCmd.CLIP
                                    }
                                    'n'.code -> {
                                        if (clip != 0) {
                                            path?.also { command?.addPath(it, clip) }
                                        }
                                        clip = 0
                                        path = Path()
                                    }
                                    'b'.code + ('*'.code shl 8) -> {
                                        path?.also {
                                            it.close()
                                            it.fillType = FillType.EVEN_ODD
                                            command?.addPath(it, PdfShapeCmd.BOTH or clip)
                                        }
                                        clip = 0
                                        path = Path()
                                    }
                                    'b'.code -> {
                                        path?.also {
                                            it.close()
                                            command?.addPath(it, PdfShapeCmd.BOTH or clip)
                                        }
                                        clip = 0
                                        path = Path()
                                    }
                                    'B'.code + ('*'.code shl 8) -> {
                                        path?.also {
                                            it.fillType = FillType.EVEN_ODD
                                            command?.addPath(it, PdfShapeCmd.BOTH or clip)
                                        }
                                        clip = 0
                                        path = Path()
                                    }
                                    'B'.code -> {
                                        path?.also {
                                            command?.addPath(it, PdfShapeCmd.BOTH or clip)
                                        }
                                        clip = 0
                                        path = Path()
                                    }
                                    'f'.code + ('*'.code shl 8) -> {
                                        path?.also {
                                            it.fillType = FillType.EVEN_ODD
                                            command?.addPath(it, PdfShapeCmd.FILL or clip)
                                        }
                                        clip = 0
                                        path = Path()
                                    }
                                    'f'.code, 'F'.code -> {
                                        path?.also {
                                            command?.addPath(it, PdfShapeCmd.FILL or clip)
                                        }
                                        clip = 0
                                        path = Path()
                                    }
                                    's'.code -> {
                                        path?.also {
                                            it.close()
                                            command?.addPath(it, PdfShapeCmd.STROKE or clip)
                                        }
                                        clip = 0
                                        path = Path()
                                    }
                                    'S'.code -> {
                                        path?.also {
                                            command?.addPath(it, PdfShapeCmd.STROKE or clip)
                                        }
                                        clip = 0
                                        path = Path()
                                    }
                                    'r'.code + ('e'.code shl 8) -> {
                                        val a = popFloat(4)
                                        path?.apply {
                                            moveTo(a[0], a[1])
                                            lineTo(a[0] + a[2], a[1])
                                            lineTo(a[0] + a[2], a[1] + a[3])
                                            lineTo(a[0], a[1] + a[3])
                                            close()
                                        }
                                    }
                                    'y'.code -> {
                                        val a = popFloat(4)
                                        path?.cubicTo(a[0], a[1], a[2], a[3], a[2], a[3])
                                    }
                                    'v'.code -> {
                                        val a = popFloat(4)
                                        path?.quadTo(a[0], a[1], a[2], a[3])
                                    }
                                    'c'.code -> {
                                        val a = popFloat(6)
                                        path?.cubicTo(a[0], a[1], a[2], a[3], a[4], a[5])
                                    }
                                    'l'.code -> {
                                        val y = popFloat()
                                        val x = popFloat()
                                        path?.lineTo(x, y)
                                    }
                                    'm'.code -> {
                                        val y = popFloat()
                                        val x = popFloat()
                                        path?.moveTo(x, y)
                                    }
//                                    'd'.code -> {
//                                        val phase = popFloat()
//                                        val dashArr = popFloatArray()
//                                        command.addDash(dashArr, phase)
//                                    }
                                    'q'.code -> {
                                        // push the parser state
                                        state?.clone()
                                            ?.let { it as? ParserState }
                                            ?.also { parserStates?.push(it) }

                                        // push graphics state
                                        command?.addPush()
                                    }
                                    'c'.code + ('m'.code shl 8) -> {
                                        // set transform to array of values
                                        val fArr: FloatArray = popFloat(6)
                                        val xForm = Matrix()
                                        xForm.setMatValues(fArr)
                                        command?.addXForm(xForm)
                                    }

                                    else -> handleUnknownCommand(cmd)
                                }

                            } catch (e: Throwable) {
                                e.printStackTrace()
                            }

                            if (stack!!.size != 0) {
                                if (!RELEASE) {
                                    Log.i(
                                        this::class.java.simpleName,
                                        "**** WARNING! Stack not zero! (cmd=$cmd, " +
                                                "size=${stack?.size}) *************************",
                                    )
                                }

                                stack!!.setSize(0)
                            }

                        } else {
                            stack?.push(obj)
                        }

                        // release or reference to the page object,
                        // so that it can be gc'd if it is no longer in use
                        command = null
                        watchableState
                    }
                    ?: Watchable.COMPLETED // if there's nothing left to parse, we're done
            }
            ?: run {
                Log.i(this::class.java.simpleName, "Page gone.  Stopping")
                Watchable.STOPPED
            }
    }

    private fun handleUnknownCommand(cmd: String) {
        if (catchExceptions) {
            if (!RELEASE) {
                Log.w(logTag, "**** WARNING: Unknown command: $cmd **************")
            }
        } else {
            throw IllegalStateException("Unknown command: $cmd")
        }
    }

    /* Parse an inline image.
     * An inline image starts with BI (already read,
     * contains a dictionary until ID, and then image data until EI.
     */
    @Throws(IOException::class)
    private fun parseInlineImage() {
        // build dictionary until ID, then read image until EI
        val hm: HashMap<String, PdfObject> = HashMap<String, PdfObject>()
        while (true) {
            val t: PdfStreamToken = nextToken()
            if (t.type == PdfStreamToken.CMD && t.name == "ID") {
                break
            }

            // it should be a name;
            var name: String = t.name ?: ""
            if (!RELEASE) {
                LogUtil.logInfo(this, "ParseInlineImage, token: $name")
            }

            when (name) {
                "BPC" -> name = "BitsPerComponent"
                "DP" -> name = "DecodeParms"
                "CS" -> name = "ColorSpace"
                "IM" -> name = "ImageMask"
                "I" -> name = "Interpolate"
                "D" -> name = "Decode"
                "F" -> name = "Filter"
                "H" -> name = "Height"
                "W" -> name = "Width"
            }

            parseObject()?.also { hm[name] = PdfObject(value = it) }
        }

        if (stream[loc].toInt() == '\r'.code) {
            loc++
        }

        if (stream[loc].toInt() == '\n'.code || stream[loc].toInt() == ' '.code) {
            loc++
        }

        val imObj: PdfObject? = hm["ImageMask"]
        if (imObj != null && imObj.getBooleanValue()) {
            // [PATCHED by michal.busta@gmail.com] - default value according to PDF spec. is [0, 1]
            // there is no need to swap array - PDF image should handle this values
            val decode = arrayOf(0.0, 1.0)
            val decodeObj = hm["Decode"]
            if (decodeObj != null) {
                decode[0] = decodeObj.getArray()?.getOrNull(0)?.getDoubleValue() ?: 0.0
                decode[1] = decodeObj.getArray()?.getOrNull(1)?.getDoubleValue() ?: 1.0
            }

            hm["Decode"] = PdfObject(value = decode)
        }

        val obj = PdfObject(null, PdfObject.DICTIONARY, hm)
        val dStart = loc

        // now skip data until a whitespace followed by EI
        while (!stream[loc].isWhiteSpace()
            || stream[loc + 1].toInt() != 'E'.code
            || stream[loc + 2].toInt() != 'I'.code
        ) {
            loc++
        }

        // data runs from dStart to loc
        val data = ByteArray(loc - dStart)
        System.arraycopy(stream, dStart, data, 0, loc - dStart)
        obj.setStream(ByteBuffer.wrap(data))
        loc += 3
//        doImage(obj)
    }

    /* Parse the next object out of the PDF stream.
     * This could be a Double, a String, a HashMap (dictionary), Object[] array,
     * or a Tok containing a PDF command.
     */
    private fun parseObject(): Any? {
        val token: PdfStreamToken = nextToken()

        when (token.type) {
            PdfStreamToken.STR,
            PdfStreamToken.NAME -> return token.name
            PdfStreamToken.NUM -> return token.value
            PdfStreamToken.CMD -> return token

            PdfStreamToken.BRKB -> {
                val hm: HashMap<String, PdfObject> = HashMap<String, PdfObject>()
                var name: String? = null

                var obj = parseObject()
                while (obj != null) {
                    if (name == null) {
                        name = obj as? String
                    } else {
                        hm[name] = PdfObject(value = obj)
                        name = null
                    }

                    obj = parseObject()
                }

                if (token.type != PdfStreamToken.BRKE) {
                    throw IllegalStateException("Inline dict should have ended with '>>'")
                }

                return hm
            }
            PdfStreamToken.ARYB -> {
                // build an array
                val ary = ArrayList<Any?>()
                var obj: Any?
                while (parseObject().also { obj = it } != null) {
                    ary.add(obj)
                }

                if (token.type != PdfStreamToken.ARYE) {
                    throw IllegalStateException("Expected ']'")
                }

                return ary.toTypedArray()
            }
        }

        if (!RELEASE) {
            Log.i(
                this::class.java.simpleName,
                "**** WARNING! parseObject unknown " +
                        "token! (t.type=${token.type}) *************************"
            )
        }
        return null
    }

    private fun nextToken(): PdfStreamToken {
        while (loc < stream.size && stream[loc].isWhiteSpace()) {
            loc++ // skip whitespace
        }

        val token = PdfStreamToken()
        if (loc >= stream.size) {
            token.type = PdfStreamToken.EOF
        } else {
            var c: Char = stream[loc++].toInt().toChar()
            while (c == '%') {
                // skip comments
                val comment = StringBuffer()
                while (loc < stream.size && c != '\n') {
                    comment.append(c)
                    c = stream[loc++].toInt().toChar()
                }

                if (loc < stream.size) {
                    c = stream[loc++].toInt().toChar() // eat the newline
                    if (c == '\r') {
                        c = stream[loc++].toInt().toChar() // eat a following return
                    }
                }

                if (!RELEASE) {
                    Log.i(this::class.java.simpleName, "Read comment: $comment")
                }
            }

            setStreamTokenType(token, c)
            if (!RELEASE) {
                Log.i(this::class.java.simpleName, "Read token: $token")
            }
        }

        return token
    }

    private fun setStreamTokenType(token: PdfStreamToken, char: Char) {
        when (char) {
            '[' -> token.type = PdfStreamToken.ARYB
            ']' -> token.type = PdfStreamToken.ARYE
            '(' -> {
                token.type = PdfStreamToken.STR
                token.name = readString()
            }
            '{' -> token.type = PdfStreamToken.BRCB
            '}' -> token.type = PdfStreamToken.BRCE
            '<' -> if (stream[loc++].toInt().toChar() == '<') {
                token.type = PdfStreamToken.BRKB
            } else {
                loc--
                token.type = PdfStreamToken.STR
                token.name = readByteArray()
            }
            '/' -> {
                token.type = PdfStreamToken.NAME
                token.name = readName()
            }
            '.', '-', '0', '1', '2', '3', '4', '5', '6', '7', '8', '9' -> {
                loc--
                token.type = PdfStreamToken.NUM
                token.value = readNum()
            }
            else -> if (char == '>' && stream[loc++].toInt().toChar() == '>') {
                token.type = PdfStreamToken.BRKE
            } else if (char in 'a'..'z' || char in 'A'..'Z' || char == '\'' || char == '"') {
                loc--
                token.type = PdfStreamToken.CMD
                token.name = readName()
            } else {
                Log.i(this::class.simpleName, "Encountered character: $$char ($char)")
                token.type = PdfStreamToken.UNK
            }
        }
    }

    /* read a floating point number from the stream */
    private fun readNum(): Double {
        var c = stream[loc++].toInt().toChar()
        val neg = (c == '-')
        var sawDot = (c == '.')
        var dotmult: Double = if (sawDot) 0.1 else 1.0
        var value = if (c in '0'..'9') (c - '0').toDouble() else 0.toDouble()
        while (true) {
            c = stream[loc++].toInt().toChar()
            if (c == '.') {
                if (sawDot) {
                    loc--
                    break
                }
                sawDot = true
                dotmult = 0.1

            } else if (c in '0'..'9') {
                val charIntValue = (c - '0')
                if (sawDot) {
                    value += (charIntValue * dotmult)
                    dotmult *= 0.1
                } else {
                    value = value * 10 + charIntValue
                }
            } else {
                loc--
                break
            }
        }
        return if (neg) -value else value
    }

    /* read a name (sequence of non-PDF-delimiting characters) from the stream. */
    private fun readName(): String {
        val start = loc
        while (loc < stream.size && stream[loc].isRegularCharacter()) {
            loc++
        }
        return String(stream, start, loc - start)
    }

    /* read a byte array from the stream.
     * Byte arrays begin with a '<' character,
     * which has already been read, and end with a '>' character.
     * Each byte in the array is made up of two hex characters,
     * the first being the high-order bit.
     *
     * We translate the byte arrays into char arrays by combining two bytes
     * into a character, and then translate the character array into a string.
     * [JK FIXME this is probably a really bad idea!]
     *
     * @return the byte array
     */
    private fun readByteArray(): String {
        val buf = StringBuffer()
        var count = 0
        var w = 0.toChar()

        // read individual bytes and format into a character array
        while (loc < stream.size && stream[loc].toInt().toChar() != '>') {
            val b: Byte = when (val c = stream[loc].toInt().toChar()) {
                in '0'..'9' -> (c - '0').toByte()
                in 'a'..'f' -> (10 + (c - 'a')).toByte()
                in 'A'..'F' -> (10 + (c - 'A')).toByte()
                else -> {
                    loc++
                    continue
                }
            }

            // calculate where in the current byte this character goes
            val offset = 1 - count % 2
            w = (w.code or (0xf and b.toInt() shl (offset shl 2))).toChar()

            // increment to the next char if we've written four bytes
            if (offset == 0) {
                buf.append(w)
                w = 0.toChar()
            }
            ++count
            ++loc
        }

        ++loc // ignore trailing '>'
        return buf.toString()
    }

    /* read a String from the stream.
     * Strings begin with a '(' character,
     * which has already been read, and end with a balanced ')' character.
     * A '\' character starts an escape sequence of up to three octal digits.
     *
     *
     * Parenthesis must be enclosed by a balanced set of parenthesis,
     * so a string may enclose balanced parenthesis.
     *
     * @return the string with escape sequences replaced with their values
     */
    private fun readString(): String {
        var parenLevel = 0
        val buf = StringBuffer()

        while (loc < stream.size) {
            var c = stream[loc++].toInt().toChar()
            when (c) {
                ')' -> if (parenLevel-- == 0) break
                '(' -> parenLevel++
                '\\' -> {
                    // escape sequences
                    c = stream[loc++].toInt().toChar()
                    when (c) {
                        in '0'..'7' -> {
                            var charIntValue = 0
                            var count = 0
                            while (c in '0'..'7' && count < 3) {
                                charIntValue = (charIntValue shl 3) + c.code - '0'.code
                                c = stream[loc++].toInt().toChar()
                                ++count
                            }
                            loc--
                            c = charIntValue.toChar()
                        }
                        'n' -> c = '\n'
                        'r' -> c = '\r'
                        't' -> c = '\t'
                        'b' -> c = '\b'
                        'f' -> c = '\u000c' // \f, 12
                    }
                }
            }
            buf.append(c)
        }
        return buf.toString()
    }

    /* abstracted command processing for Q command.
     * Used directly and as part of processing of mushed QBT command.
     */
    private fun processQCmd() {
        // pop graphics state ('Q')
        command?.addPop()
        // pop the parser state
        state = parserStates!!.pop() as ParserState
    }

    /* abstracted command processing for BT command. Used directly and as
     * part of processing of mushed QBT command.
     */
    private fun processBTCmd() {
        // begin text block:  reset everything.
        state?.textFormat?.reset()
    }

    /* pop a single integer value off the stack.
     * @return the integer value of the top of the stack
     * @throws PdfParseException if the top of the stack isn't a number.
     */
    @Throws(IllegalStateException::class)
    private fun popInt(): Int {
        val obj = stack?.pop()
        return if (obj is Double) {
            obj.toInt()
        } else {
            throw IllegalStateException("Expected a number here.")
        }
    }

    /* pop a single float value off the stack.
     * @return the float value of the top of the stack
     * @throws PDFParseException if the value on the top of the stack isn't a number
     */
    @Throws(IllegalStateException::class)
    private fun popFloat(): Float {
        val obj = stack?.pop()
        return if (obj is Double) {
            obj.toFloat()
        } else {
            throw IllegalStateException("Expected a number here.")
        }
    }

    /* pop an array of float values off the stack.
     * This is equivalent to filling an array from end to front by popping values off the stack.
     *
     * @param count the number of numbers to pop off the stack
     * @return an array of length <tt>count</tt>
     */
    private fun popFloat(count: Int): FloatArray {
        val ary = FloatArray(count)
        for (i in count - 1 downTo 0) {
            ary[i] = popFloat()
        }
        return ary
    }

    /* pop an array of integer values off the stack.
     * This is equivalent to filling an array from end to front by popping values off the stack.
     *
     * @param count the number of numbers to pop off the stack
     * @return an array of length <tt>count</tt>
     * @throws PDFParseException if any of the values popped off the
     * stack are not numbers.
     */
    @Throws(IllegalStateException::class)
    private fun popFloatArray(): FloatArray? {
        val obj = (stack!!.pop() as? Array<*>)
            ?: throw IllegalStateException("Expected an [array] here.")

        val ary = FloatArray(obj.size)
        for (i in ary.indices) {
            if (obj[i] is Double) {
                ary[i] = (obj[i] as Double).toFloat()
            } else {
                throw IllegalStateException("This array doesn't consist only of floats.")
            }
        }
        return ary
    }

    /* pop a String off the stack.
     * @return the String from the top of the stack
     * @throws PDFParseException if the top of the stack is not a NAME or STR.
     */
    @Throws(IllegalStateException::class)
    private fun popString(): String {
        val obj = stack?.pop()
        return if (obj !is String) {
            throw IllegalStateException("Expected string here: $obj")
        } else {
            obj
        }
    }

    @Throws(IllegalStateException::class)
    private fun popArray(): Array<*> {
        val obj = stack!!.pop()
        if (obj !is Array<*>) {
            throw IllegalStateException("Expected an [array] here: $obj")
        }
        return obj
    }

    /* add graphics state commands contained within a dictionary.
     * @param name the resource name of the graphics state dictionary
     */
    @Throws(IOException::class)
    private fun setGSState(name: String) {
        // obj must be a string that is a key to the "ExtGState" dict
//        val gsObj: PdfObject = findResource(name, "ExtGState")
//
//        // get LW, LC, LJ, Font, SM, CA, ML, D, RI, FL, BM, ca
//        // out of the reference, which is a dictionary
//        gsObj.getDictRef("LW")
//            ?.also { command?.addStrokeWidth(it.getFloatValue()) }
//
//        gsObj.getDictRef("LC")
//            ?.also { command?.addEndCap(it.getIntValue()) }
//
//        gsObj.getDictRef("LJ")
//            ?.also { command?.addLineJoin(it.getIntValue()) }
//
//        if (gsObj.getDictRef("Font").also { d = it } != null) {
//            state!!.textFormat!!.setFont(
//                getFontFrom(d.getAt(0).getStringValue()),
//                d.getAt(1).getFloatValue()
//            )
//        }
//
//        gsObj.getDictRef("ML")
//            ?.also { command?.addMiterLimit(it.getFloatValue()) }
//
//        if (gsObj.getDictRef("D").also { d = it } != null) {
//            val pdash: Array<PDFObject> = d.getAt(0).getArray()
//            val dash = FloatArray(pdash.size)
//            for (i in pdash.indices) {
//                dash[i] = pdash[i].getFloatValue()
//            }
//            cmds.addDash(dash, d.getAt(1).getFloatValue())
//        }
//
//        gsObj.getDictRef("CA")
//            ?.also { command?.addStrokeAlpha(it.getFloatValue()) }
//
//        gsObj.getDictRef("ca")
//            ?.also { command?.addFillAlpha(it.getFloatValue()) }
    }

    private fun dump(stack: Stack<Any>?): String = when {
        stack == null -> "<null>"
        stack.size == 0 -> "[]"
        else -> {
            var result = ""
            var delimiter = "["
            for (obj in stack) {
                result += (delimiter + dumpObj(obj))
                delimiter = ","
            }

            result += "]"
            result
        }
    }

    private fun dumpObj(obj: Any?): String? = when (obj) {
        null -> "<null>"
        is Array<*> -> (obj as? Array<*>)
            ?.map { it as Any }?.toTypedArray()
            ?.let { dumpArray(it) }

        else -> obj.toString()
    }

    private fun dumpArray(arr: Array<Any>?): String = when {
        arr == null -> "<null>"
        arr.isEmpty() -> "[]"
        else -> {
            var result = ""
            var delimiter = "["
            for (obj in arr) {
                result += delimiter + dumpObj(obj)
                delimiter = ","
            }

            result += "]"
            result
        }
    }

    /* A class to store state needed while rendering.
     * This includes the stroke and fill color spaces,
     * as well as the text formatting parameters.
     */
    internal class ParserState : Cloneable {

        /* the fill color space  */
        var fillCS: PdfColorSpace? = null

        /* the stroke color space  */
        var strokeCS: PdfColorSpace? = null

        /* the text parameters  */
        var textFormat: PdfTextFormat? = null

        /* Clone the render state. */
        public override fun clone(): Any {
            val newState = ParserState()

            // no need to clone color spaces, since they are immutable
            // uncommented following 2 lines (mutable?)
            newState.fillCS = fillCS
            newState.strokeCS = strokeCS

            // we do need to clone the textFormat
            newState.textFormat = textFormat?.clone() as? PdfTextFormat
            return newState
        }
    }

}
