package mutnemom.android.kotlindemo.reader.pdf

import java.util.*

class PdfPageCache {

    /* the pages in the cache, mapped by page number  */
    private val pages: MutableMap<Int, SoftReference<*>> = mutableMapOf()

    /* Get a page's parser from the cache
     *
     * @param pageNumber the number of the page to get the parser for
     * @return the parser, or null if it is not in the cache
     */
    fun getPageParser(pageNumber: Int): PdfParser? =
        getPageRecord(pageNumber)?.generator
            ?.let { (it as? PdfParser) }

    /* Add a page to the cache.
     * This method should be used for pages which are still in the process of being rendered.
     *
     * @param pageNumber the page number of this page
     * @param page the page to add
     * @param parser the parser which is parsing this page
     */
    fun addPage(pageNumber: Int, page: PdfPage, parser: PdfParser) {

    }

    /* Get a page from the cache
     *
     * @param pageNumber the number of the page to get
     * @return the page, if it is in the cache, or null if not
     */
    fun getPage(pageNumber: Int): PdfPage? =
        getPageRecord(pageNumber)?.value
            ?.let { (it as? PdfPage) }

    /* Get a page's record from the cache
     *
     * @return the record, or null if it's not in the cache
     */
    private fun getPageRecord(pageNumber: Int): PageRecord? =
        pages[pageNumber]?.get() as? PageRecord


    /* the basic information about a page or image  */
    internal open class Record {

        /* the page or image itself  */
        var value: Any? = null

        /* the thing generating the page, or null if done/not provided  */
        var generator: BaseWatchable? = null
    }

    /* the record stored for each page in the cache  */
    internal class PageRecord : Record() {

        /* any images associated with the page  */
        var images: Map<ImageInfo, SoftReference<Record>> = Collections.synchronizedMap(HashMap())

    }

}
