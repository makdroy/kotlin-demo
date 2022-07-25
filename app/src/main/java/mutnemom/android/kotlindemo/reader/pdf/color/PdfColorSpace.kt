package mutnemom.android.kotlindemo.reader.pdf.color

import mutnemom.android.kotlindemo.reader.pdf.PdfObject
import mutnemom.android.kotlindemo.reader.pdf.PdfPaint
import mutnemom.android.kotlindemo.reader.pdf.calculateColorSpace

abstract class PdfColorSpace {

    companion object {

        /* the name of the device-dependent gray color space  */
        const val COLOR_SPACE_GRAY = 0

        /* the name of the device-dependent RGB color space  */
        const val COLOR_SPACE_RGB = 1

        /* the name of the device-dependent CMYK color space  */
        const val COLOR_SPACE_CMYK = 2

        /* the name of the pattern color space  */
        const val COLOR_SPACE_PATTERN = 3

        const val COLOR_SPACE_INDEXED = 4

        const val COLOR_SPACE_ALTERNATE = 5

        val patternSpace: PdfColorSpace = RGBColorSpace()
        val graySpace: PdfColorSpace = GrayColorSpace()
        val cmykSpace: PdfColorSpace = CMYKColorSpace()
        val rgbSpace: PdfColorSpace = RGBColorSpace()


        /* Get a color space by name
         *
         * @param name the name of one of the device-dependent color spaces
         */
        fun getColorSpace(name: Int): PdfColorSpace {
            return when (name) {
                COLOR_SPACE_PATTERN -> patternSpace
                COLOR_SPACE_GRAY -> graySpace
                COLOR_SPACE_CMYK -> cmykSpace
                COLOR_SPACE_RGB -> rgbSpace
                else -> throw IllegalArgumentException("Unknown Color Space name: $name")
            }
        }

        fun getColorSpace(csObj: PdfObject, resources: Map<String, PdfObject>?): PdfColorSpace? {
            val colorSpaces = if (csObj.type == PdfObject.NAME) {
                when (val key = csObj.getStringValue()) {
                    "DeviceCMYK", "CMYK" -> getColorSpace(COLOR_SPACE_CMYK)
                    "DeviceRGB", "RGB" -> getColorSpace(COLOR_SPACE_RGB)
                    "DeviceGray", "G" -> getColorSpace(COLOR_SPACE_GRAY)
                    "Pattern" -> getColorSpace(COLOR_SPACE_PATTERN)
                    else -> key?.let { keyName ->
                        resources?.get("ColorSpace")
                            ?.getDictRef(keyName)?.calculateColorSpace(resources)
                    }
                }
            } else {
                csObj.calculateColorSpace(resources)
            }

            return colorSpaces
        }
    }

    /* get the number of components expected in the getPaint command */
    abstract fun getNumComponents(): Int

    /* get the type of this color space */
    abstract fun getType(): Int

    /* get the name of this color space */
    abstract fun getName(): String?

    abstract fun toColor(arr: FloatArray): Int

    abstract fun toColor(arr: IntArray): Int

    override fun toString(): String {
        return "ColorSpace[${getName()}]"
    }

    /* get the PDFPaint representing the color described by the given color components
     *
     * @param components the color components corresponding to the given colorspace
     * @return a PDFPaint object representing the closest Color to the
     * given components.
     */
    fun getPaint(components: FloatArray?): PdfPaint? {
        return components?.let { PdfPaint.getColorPaint(toColor(it)) }
    }

    fun getFillPaint(components: FloatArray?): PdfPaint? {
        return components?.let { PdfPaint.getPaint(toColor(it)) }
    }

}
