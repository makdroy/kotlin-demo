/*
 * $Id: Type3Font.java,v 1.3 2009/02/12 13:53:54 tomoke Exp $
 *
 * Copyright 2004 Sun Microsystems, Inc., 4150 Network Circle,
 * Santa Clara, California 95054, U.S.A. All rights reserved.
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 * 
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
 */
package mutnemom.android.kotlindemo.reader.pdf.font

import mutnemom.android.kotlindemo.reader.pdf.PdfFont
import mutnemom.android.kotlindemo.reader.pdf.PdfFontDescriptor
import mutnemom.android.kotlindemo.reader.pdf.PdfGlyph
import mutnemom.android.kotlindemo.reader.pdf.PdfObject

class Type3Font(
    baseFont: String?,
    fontObj: PdfObject,
    private val resources: HashMap<String, PdfObject>?,
    descriptor: PdfFontDescriptor?
) : PdfFont(baseFont, descriptor) {

//    var charProcs: Map<*, *>
//    var bbox: RectF?
//    var at: Matrix
//    var widths: FloatArray
//    var firstChar: Int
//    var lastChar: Int

//    init {
//        // get the transform matrix
//        val matrix = fontObj.getDictRef("FontMatrix")
//        val matrixAry = FloatArray(6)
//        for (i in 0..5) {
//            matrixAry[i] = matrix.getAt(i).getFloatValue()
//        }
//        at = Utils.createMatrix(matrixAry)
//
//        // get the scale from the matrix
//        val scale = matrixAry[0] + matrixAry[2]
//
//        // put all the resources in a Hash
//        val rsrcObj: PDFObject = fontObj.getDictRef("Resources")
//        if (rsrcObj != null) {
//            rsrc.putAll(rsrcObj.getDictionary())
//        }
//
//        // get the character processes, indexed by name
//        charProcs = fontObj.getDictRef("CharProcs").getDictionary()
//
//        // get the font bounding box
//        val bboxdef: Array<PDFObject> = fontObj.getDictRef("FontBBox").getArray()
//        val bboxfdef = FloatArray(4)
//        for (i in 0..3) {
//            bboxfdef[i] = bboxdef[i].getFloatValue()
//        }
//        bbox = RectF(
//            bboxfdef[0], bboxfdef[1],
//            bboxfdef[2] - bboxfdef[0],
//            bboxfdef[3] - bboxfdef[1]
//        )
//        if (bbox!!.isEmpty) {
//            bbox = null
//        }
//
//        // get the widths
//        val widthArray: Array<PDFObject> = fontObj.getDictRef("Widths").getArray()
//        widths = FloatArray(widthArray.size)
//        for (i in widthArray.indices) {
//            widths[i] = widthArray[i].getFloatValue()
//        }
//
//        // get first and last chars
//        firstChar = fontObj.getDictRef("FirstChar").getIntValue()
//        lastChar = fontObj.getDictRef("LastChar").getIntValue()
//    }

    override fun getGlyph(src: Char, name: String?): PdfGlyph? {
        return null
//        requireNotNull(name) {
//            "Glyph name required for Type3 font!" +
//                    "Source character: " + src.code
//        }
//        val pageObj: PDFObject = charProcs[name] as PDFObject?
//            ?: // glyph not found.  Return an empty glyph...
//            return PDFGlyph(src, name, Path(), PointF(0, 0))
//        return try {
//            val page = PDFPage(bbox, 0)
//            page.addXform(at)
//            val prc = PDFParser(page, pageObj.getStream(), rsrc)
//            prc.go(true)
//            val width = widths[src.code - firstChar]
//            val advance = PointF(width, 0)
//            val pts = floatArrayOf(advance.x, advance.y)
//            at.mapPoints(pts)
//            advance.x = pts[0]
//            advance.y = pts[1]
//            PDFGlyph(src, name, page, advance)
//        } catch (ioe: IOException) {
//            // help!
//            println("IOException in Type3 font: $ioe")
//            ioe.printStackTrace()
//            null
//        }
    }

}
