package mutnemom.android.kotlindemo.reader.pdf

class HardReference<T>(o: T) {

    companion object {
        private var cleanupList = ArrayList<HardReference<*>>()
        var sKeepCaches = false

        fun cleanup() {
            val oldList = cleanupList
            cleanupList = ArrayList()
            for (hr in oldList) {
                hr.clean()
            }
            oldList.clear()
        }
    }

    private var ref: T?

    init {
        ref = o
        cleanupList.add(this)
    }

    fun clean() {
        ref = null
    }

    fun get(): T? = ref

}
