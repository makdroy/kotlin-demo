package mutnemom.android.kotlindemo.model

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class DownloadModel(
    var progress: Int = 0,
    var currentFileSize: Int = 0,
    var totalFileSize: Int = 0
) : Parcelable
