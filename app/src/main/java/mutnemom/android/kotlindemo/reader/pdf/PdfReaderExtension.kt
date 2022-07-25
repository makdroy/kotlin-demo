package mutnemom.android.kotlindemo.reader.pdf

/* Is the argument a white space character according to the PDF spec?.
 * ISO Spec 32000-1:2008 - Table 1
 */
fun Byte.isWhiteSpace(): Boolean = when (this.toInt()) {
    0, // nul_char
    12, // From Feed (FF)
    '\t'.code,
    '\n'.code,
    '\r'.code,
    ' '.code -> true
    else -> false
}

/* Is the argument a delimiter according to the PDF spec?
 * ISO 32000-1:2008 - Table 2
 */
fun Byte.isDelimiter(): Boolean = when (this.toInt().toChar()) {
    '(', ')', '<', '>', '[', ']', '{', '}', '/', '%' -> true
    else -> false
}

fun Byte.isChar(char: Char): Boolean = this.toInt() == char.code

fun Byte.isDigit(): Boolean = this.toInt().toChar().isDigit()

fun Byte.isEngLetters(): Boolean = this.toInt().toChar().uppercaseChar() in 'A'..'Z'

fun Byte.isNumberOrSign(): Boolean = this.isDigit()
        || this.isChar('-')
        || this.isChar('+')
        || this.isChar('.')

fun Byte.isRegularCharacter(): Boolean = !(isWhiteSpace() || isDelimiter())
