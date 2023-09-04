package mutnemom.android.kotlindemo.extensions

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.LocationManager
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.core.location.LocationManagerCompat

val Context.isLocationPermissionGranted: Boolean
    get() = hasFineLocationAccess || hasCoarseLocationAccess

val Context.hasFineLocationAccess: Boolean
    get() = ActivityCompat.checkSelfPermission(
        this,
        Manifest.permission.ACCESS_FINE_LOCATION
    ) == PackageManager.PERMISSION_GRANTED

val Context.hasCoarseLocationAccess: Boolean
    get() = ActivityCompat.checkSelfPermission(
        this,
        Manifest.permission.ACCESS_COARSE_LOCATION
    ) == PackageManager.PERMISSION_GRANTED

val Context.isDeviceSettingLocationEnable: Boolean
    get() = try {
        val locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        LocationManagerCompat.isLocationEnabled(locationManager)
    } catch (e: Throwable) {
        e.printStackTrace()
        false
    }

fun Context.showRationaleLocation(callback: (() -> Unit)? = null) {
    try {
        AlertDialog.Builder(this)
            .setTitle("Location")
            .setMessage("จำเป็นต้องเข้าถึงตำแหน่งของอุปกรณ์เพื่อทำรายการ")
            .setCancelable(false)
            .setNegativeButton("ยกเลิก") { dialog, _ -> dialog.dismiss() }
            .setPositiveButton("ตกลง") { _, _ -> callback?.invoke() }
            .show()

    } catch (e: Throwable) {
        e.printStackTrace()
    }
}

fun Context.showPopupEnableLocationSettings(callback: () -> Unit) {
    try {
        AlertDialog.Builder(this)
            .setTitle("Location Setting")
            .setMessage("เปิดการเข้าถึงตำแหน่งของอุปกรณ์")
            .setCancelable(false)
            .setNegativeButton("ยกเลิก") { dialog, _ -> dialog.dismiss() }
            .setPositiveButton("ตกลง") { _, _ -> callback() }
            .show()

    } catch (e: Throwable) {
        e.printStackTrace()
    }
}
