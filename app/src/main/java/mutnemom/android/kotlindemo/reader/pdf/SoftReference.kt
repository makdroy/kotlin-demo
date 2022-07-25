package mutnemom.android.kotlindemo.reader.pdf

import java.lang.ref.SoftReference

class SoftReference<T>(o: T) {

    var softRef: SoftReference<T>? = null
    var hardRef: HardReference<T>? = null

    fun get(): T? =
        if (HardReference.sKeepCaches) hardRef?.get() else softRef?.get()

    init {
        if (HardReference.sKeepCaches) {
            hardRef = HardReference(o)
        } else {
            softRef = SoftReference(o)
        }
    }

}
