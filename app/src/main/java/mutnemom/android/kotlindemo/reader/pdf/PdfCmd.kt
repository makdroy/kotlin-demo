package mutnemom.android.kotlindemo.reader.pdf

import android.graphics.RectF

abstract class PdfCmd {

    /* mark the page or change the graphics state
     *
     * @param state the current graphics state;  may be modified during execution.
     * @return the region of the page made dirty
     * by executing this command or null if no region was touched.
     * Note this value should be in the coordinates of the image touched, not the page.
     */
    abstract fun execute(state: PdfRendererSync): RectF?

    /* a human readable representation of this command */
    override fun toString(): String {
        val name = javaClass.name
        val lastDot = name.lastIndexOf('.')
        return if (lastDot >= 0) {
            name.substring(lastDot + 1)
        } else {
            name
        }
    }

}
