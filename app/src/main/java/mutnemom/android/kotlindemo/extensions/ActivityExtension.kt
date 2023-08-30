package mutnemom.android.kotlindemo.extensions

import android.content.Context.LOCATION_SERVICE
import android.content.pm.PackageManager
import android.location.LocationListener
import android.location.LocationManager
import android.os.Looper
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailability
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import java.util.concurrent.TimeUnit

fun AppCompatActivity.toast(text: String): Toast {
    val toast = Toast.makeText(this, text, Toast.LENGTH_SHORT)
    toast.show()
    return toast
}

fun AppCompatActivity.requestPermission(p: String, requestCode: Int) {
    if (ActivityCompat.checkSelfPermission(this, p) != PackageManager.PERMISSION_GRANTED) {
        ActivityCompat.requestPermissions(this, arrayOf(p), requestCode)
    }
}

@Suppress("MissingPermission")
fun AppCompatActivity.getLocation(listener: LocationListener) {
    val locationManager =
        (applicationContext.getSystemService(LOCATION_SERVICE) as? LocationManager)
            ?: return

    when {
        !isLocationPermissionGranted -> return
        locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) -> {
            locationManager.requestLocationUpdates(
                LocationManager.GPS_PROVIDER,
                (0) /* time between updates in milliseconds */,
                90f /* distance in meters */,
                listener
            )
        }
    }
}

@Suppress("MissingPermission", "DEPRECATION")
fun AppCompatActivity.requestLocationUpdate(
    providerClient: FusedLocationProviderClient,
    locationCallback: LocationCallback
) {
    val request = LocationRequest().apply {
        smallestDisplacement = 10.0f /* request again in 10 meters */
        fastestInterval = TimeUnit.SECONDS.toMillis(1)
        numUpdates = 5
        priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        interval = TimeUnit.SECONDS.toMillis(5)
    }

    Looper.myLooper()
        ?.also { providerClient.requestLocationUpdates(request, locationCallback, it) }
}

fun AppCompatActivity.shouldShowRationale(permission: String): Boolean =
    ActivityCompat.shouldShowRequestPermissionRationale(this, permission)

val AppCompatActivity.hasGooglePlayServices: Boolean
    get() = GoogleApiAvailability
        .getInstance()
        .isGooglePlayServicesAvailable(this) == ConnectionResult.SUCCESS
