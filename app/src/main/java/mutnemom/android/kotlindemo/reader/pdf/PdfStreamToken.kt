package mutnemom.android.kotlindemo.reader.pdf

class PdfStreamToken {

    companion object {

        /* begin bracket &lt;  */
        const val BRKB = 11

        /* end bracket &gt;  */
        const val BRKE = 10

        /* begin array [  */
        const val ARYB = 9

        /* end array ]  */
        const val ARYE = 8

        /* String (, readString looks for trailing )  */
        const val STR = 7

        /* begin brace {  */
        const val BRCB = 5

        /* end brace }  */
        const val BRCE = 4

        /* number  */
        const val NUM = 3

        /* keyword  */
        const val CMD = 2

        /* name (begins with /)  */
        const val NAME = 1

        /* unknown token  */
        const val UNK = 0

        /* end of stream  */
        const val EOF = -1
    }

    /* the string value of a STR, NAME, or CMD token  */
    var name: String? = null

    /* the value of a NUM token  */
    var value = 0.0

    /* the type of the token  */
    var type = 0

    override fun toString(): String {
        return when (type) {
            NAME -> "NAME: $name"
            ARYB -> "ARY ["
            ARYE -> "ARY ]"
            NUM -> "NUM: $value"
            STR -> "STR: ($name"
            CMD -> "CMD: $name"
            UNK -> "UNK"
            EOF -> "EOF"
            else -> "some kind of brace ($type)"
        }
    }

}
