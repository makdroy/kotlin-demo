package mutnemom.android.kotlindemo.reader.pdf

import android.util.Log
import java.lang.ref.SoftReference

/**
 * a cross reference representing a line in the PDF cross referencing
 * table.
 * <p>
 * There are two forms of the PdfXRef, destinguished by absolutely nothing.
 * The first type of PdfXRef is used as indirect references in a PdfObject.
 * In this type, the id is an index number into the object cross reference
 * table.  The id will range from 0 to the size of the cross reference
 * table.
 * <p>
 * The second form is used in the Java representation of the cross reference
 * table.  In this form, the id is the file position of the start of the
 * object in the PDF file.  See the use of both of these in the
 * PdfFile.dereference() method, which takes a PdfXRef of the first form,
 * and uses (internally) a PdfXRef of the second form.
 * <p>
 * This is an unhappy state of affairs, and should be fixed.  Fortunately,
 * the two uses have already been factored out as two different methods.
 *
 * @author Mike Wessler
 */
class PdfXRef(
    var id: Int = -1,
    var generation: Int = -1,
    var compressed: Boolean = false,
    line: ByteArray? = null
) {

    // this field is only used in PDFFile.objIdx
    var reference: SoftReference<PdfObject>? = null

    val fileLocation: Int
        get() = id

    init {
        line?.also {
            id = String(it, 0, 10).toInt()
            generation = String(it, 11, 5).toInt()
        }

        compressed = false
    }

    @Suppress("UNUSED")
    fun print() {
        Log.e(
            this::class.java.simpleName,
            "-> id:$id," +
                    " generation:$generation," +
                    " compressed:$compressed," +
                    " reference:$reference"
        )
    }

}
