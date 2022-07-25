package mutnemom.android.kotlindemo.reader.pdf

import android.util.Log
import mutnemom.android.kotlindemo.reader.pdf.decoder.PdfDecoder
import mutnemom.android.kotlindemo.reader.pdf.decryptor.IdentityDecryptor
import mutnemom.android.kotlindemo.reader.pdf.decryptor.PdfDecryptor
import java.lang.ref.SoftReference
import java.nio.ByteBuffer
import java.nio.MappedByteBuffer

class PdfObject(
    /* the PdfFile from which this object came, * used for dereferences */
    private var owner: PdfFile? = null,
    /* the type of this object */
    var type: Int = NULL,
    /* the value of this object. It can be a wide number of things, defined by type */
    private var value: Any? = null
) {

    companion object {

        /* When a value of [objNum][.getObjGen] or [objGen][.getObjGen],
         * indicates that the object is not top-level,
         * and is embedded in another object.
         */
        const val OBJ_NUM_EMBEDDED = -2

        /* When a value of [objNum][.getObjGen] or [objGen][.getObjGen],
         * indicates that the object is not top-level,
         * and is embedded directly in the trailer.
         */
        const val OBJ_NUM_TRAILER = -1

        /* an indirect reference (PDFXref) */
        const val INDIRECT = 0

        /* a Boolean  */
        const val BOOLEAN = 1

        /* a Number, represented as a double  */
        const val NUMBER = 2

        /* a String  */
        const val STRING = 3

        /* (String) a special string, seen in PDF files as /Name  */
        const val NAME = 4

        /* an array of PDFObjects  */
        const val ARRAY = 5

        /* a Hashmap that maps String names to PDFObjects */
        const val DICTIONARY = 6

        /* a Stream: a Hashmap with a byte array */
        const val STREAM = 7

        /* The NULL Object (there is only one) */
        const val NULL = 8

        /* (String) a special PDF bare word, like R, obj, true, false, etc  */
        const val KEYWORD = 9

        /* The NULL PdfObject */
        val NULL_OBJ: PdfObject by lazy { PdfObject(null, NULL, null) }
    }


    /* the encoded stream, if this is a STREAM object  */
    private var stream: ByteBuffer? = null

    /* a cached version of the decoded stream  */
    private var decodedStream: SoftReference<ByteBuffer>? = null


    /* Identify whether the object is currently an indirect/cross-reference
     * @return whether currently indirect
     */
    val isIndirect: Boolean
        get() = (type == INDIRECT)

    val decryptor: PdfDecryptor
        get() = owner?.defaultDecryptor ?: IdentityDecryptor.INSTANCE

    init {
        if (owner == null && value != null) {
            if (value is Double || value is Int) {
                type = NUMBER
            } else if (value is String) {
                type = NAME
            } else if (value is Array<*>) {
                val srcArr = value as Array<*>
                val dstArr = mutableListOf<PdfObject>()

                srcArr.forEachIndexed { index, any ->
                    dstArr[index] = any as PdfObject
                }

                value = dstArr.toTypedArray()
                type = ARRAY
            } else if (value is HashMap<*, *>) {
                type = DICTIONARY
            } else if (value is Boolean) {
                type = BOOLEAN
            } else if (value is PdfStreamToken) {
                val tok = value as PdfStreamToken
                when (tok.name) {
                    "true" -> {
                        value = true
                        type = BOOLEAN
                    }
                    "false" -> {
                        value = false
                        type = BOOLEAN
                    }
                    else -> {
                        value = tok.name
                        type = NAME
                    }
                }
            } else {
                throw IllegalStateException("Bad type for raw PdfObject: $value")
            }
        }
    }


    /* get the value in the cache.
     * May become null at any time.
     * @return the cached value, or null if the value has been garbage collected.
     */
    fun getCache(): Any? = when {
        type == INDIRECT -> dereference()?.getCache()
        cache != null -> cache?.get()
        else -> null
    }

    /* set the cached value.
     * The object may be garbage collected if no other reference exists to it.
     * @param obj the object to be cached
     */
    fun setCache(obj: Any) {
        if (type == INDIRECT) {
            dereference()?.setCache(obj)
            return
        } else {
            cache = SoftReference<Any>(obj)
        }
    }

    /* a cache of translated data.
     * This data can be garbage collected at any time,
     * after which it will have to be rebuilt.
     */
    private var cache: SoftReference<Any>? = null

    private var objNum: Int = OBJ_NUM_EMBEDDED
    private var objGen: Int = OBJ_NUM_EMBEDDED

    /* get the stream from this object.
     * Will return null if this object isn't a STREAM.
     * @return the stream, or null, if this isn't a STREAM.
     */
    fun getStream(): ByteArray? = when (type) {
        STREAM -> stream?.let {
            var data = arrayListOf<Byte>()
            synchronized(it) {
                decodeStream()?.let { streamBuf ->
                    // First try to use the array with no copying.
                    // This can only be done if the buffer has a backing array,
                    // and is not a slice
                    if (streamBuf.hasArray() && streamBuf.arrayOffset() == 0) {
                        val ary = streamBuf.array()

                        // make sure there is no extra data in the buffer
                        if (ary.size == streamBuf.remaining()) {
                            return@let ary
                        }
                    }

                    // Can't use the direct buffer, so copy the data (bad)
                    data = arrayListOf(streamBuf.remaining().toByte())
                    streamBuf.get(data.toByteArray())

                    // return the stream to its starting position
                    streamBuf.flip()
                }
            }

            data.toByteArray()
        }

        INDIRECT -> dereference()?.getStream()
        STRING -> getStringValue()?.toByteArray()
        else -> null
    }

    /* get the stream from this object as a byte buffer.  Will return null if
     * this object isn't a STREAM.
     * @return the buffer, or null, if this isn't a STREAM.
     */
    fun getStreamBuffer(): MappedByteBuffer? = when {
        type == STREAM && stream != null -> synchronized(stream!!) {
            decodeStream()
                ?.let { it.duplicate() as? MappedByteBuffer }
        }

        type == STRING -> getStringValue()
            ?.let { src -> ByteBuffer.wrap(src.toByteArray()) as? MappedByteBuffer }

        type == INDIRECT -> dereference()?.getStreamBuffer()
        else -> null
    }

    /* get the dictionary as a HashMap.
     * If this isn't a DICTIONARY or a STREAM, returns null
     */
    fun getDictionary(): HashMap<String, PdfObject>? = when (type) {
        DICTIONARY, STREAM -> value as? HashMap<String, PdfObject>
        INDIRECT -> dereference()?.getDictionary()
        else -> HashMap()
    }

    /* get the value associated with a particular key in the
     * dictionary.  If this isn't a DICTIONARY or a STREAM,
     * or there is no such key, returns null.
     */
    fun getDictRef(key: String): PdfObject? = when (type) {
        DICTIONARY, STREAM -> {
            val hm = value as? HashMap<*, *>
            hm?.get(key.intern().intern()) as? PdfObject
        }
        INDIRECT -> dereference()?.getDictRef(key)
        else -> null
    }

    /* get the value as an int.
     * Will return 0 if this object isn't a NUMBER.
     */
    fun getIntValue(): Int = when (type) {
        INDIRECT -> dereference()?.getIntValue() ?: 0
        NUMBER -> (value as? Double)?.toInt() ?: 0
        else -> 0
    }

    /* get the value as a float.
     * Will return 0 if this object isn't a NUMBER
     */
    fun getFloatValue(): Float = when (type) {
        INDIRECT -> dereference()!!.getFloatValue()
        NUMBER -> (value as Double).toFloat()
        else -> 0f
    }

    /* get the value as a double.
     * Will return 0 if this object isn't a NUMBER.
     */
    fun getDoubleValue(): Double = when (type) {
        INDIRECT -> dereference()!!.getDoubleValue()
        NUMBER -> (value as Double).toDouble()
        else -> 0.0
    }

    fun getStringValue(): String? = when (type) {
        STRING, NAME, KEYWORD -> value as? String
        INDIRECT -> dereference()?.getStringValue()
        else -> null
    }

    fun getBooleanValue(): Boolean = when (type) {
        INDIRECT -> dereference()?.getBooleanValue() ?: false
        BOOLEAN -> (value as? Boolean) == true
        else -> false
    }

    fun getXRefValue(): String = (value as? PdfXRef)
        ?.let { "id: ${it.id}, generation: ${it.generation}" }
        ?: ""

    /* get the value as a PDFObject[].
     * If this object is an ARRAY, will return the array.
     * Otherwise, will return an array of one element with this object as the element.
     */
    fun getArray(): Array<PdfObject>? = when (type) {
        INDIRECT -> dereference()?.getArray()
        ARRAY -> value as? Array<PdfObject>
        else -> arrayOf(this)
    }

    /* Make sure that this object is dereferenced.
     * Use the cache of an indirect object to cache the dereferenced value, if possible.
     */
    fun dereference(): PdfObject? =
        if (type == INDIRECT) {
            var obj: PdfObject? = null

            if (cache != null) {
                obj = cache?.get() as? PdfObject
            }

            if (obj?.value == null) {
                if (owner == null) {
                    Log.i(this::class.java.simpleName, "-> Bad seed owner is null")
                    Log.i(this::class.java.simpleName, "-> Object: $this")
                }

                val objNum = (value as PdfXRef).id
                val objGen = (value as PdfXRef).generation
                obj = owner!!.dereference(objNum, objGen, decryptor)

                cache = SoftReference(obj)
            }

            obj
        } else null

    fun decodeStream(): ByteBuffer? {
        var outStream: ByteBuffer? = null

        // first try the cache
        if (decodedStream != null) {
            outStream = decodedStream?.get()
        }

        // no luck in the cache, do the actual decoding
        if (outStream == null) {
            stream?.also {
                it.rewind()
                outStream = PdfDecoder.decodeStream(this, it)
                decodedStream = SoftReference(outStream)
            }
        }

        return outStream
    }

    fun setObjectId(objNum: Int, objGen: Int) {
        assert(objNum >= OBJ_NUM_TRAILER)
        assert(objGen >= OBJ_NUM_TRAILER)
        this.objNum = objNum
        this.objGen = objGen
    }

    /* set the stream of this object.
     * It should have been a DICTIONARY before the call.
     * @param data the data, as a ByteBuffer.
     */
    fun setStream(data: ByteBuffer) {
        this.type = STREAM
        this.stream = data
    }

}
