package mutnemom.android.kotlindemo.location

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationAvailability
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.tasks.Task
import mutnemom.android.kotlindemo.databinding.ActivityLocationDemoBinding
import mutnemom.android.kotlindemo.extensions.getLocation
import mutnemom.android.kotlindemo.extensions.hasGooglePlayServices
import mutnemom.android.kotlindemo.extensions.isDeviceSettingLocationEnable
import mutnemom.android.kotlindemo.extensions.isLocationPermissionGranted
import mutnemom.android.kotlindemo.extensions.requestLocationUpdate
import mutnemom.android.kotlindemo.extensions.requestPermission
import mutnemom.android.kotlindemo.extensions.shouldShowRationale
import mutnemom.android.kotlindemo.extensions.showPopupEnableLocationSettings
import mutnemom.android.kotlindemo.extensions.showRationaleLocation
import mutnemom.android.kotlindemo.extensions.toast

class LocationDemoActivity : AppCompatActivity(), OnCompleteListener<Location> {

    private lateinit var binding: ActivityLocationDemoBinding

    private lateinit var providerClient: FusedLocationProviderClient
    private var currentLocation: Location? = null
    private var isGpsAvailable = true

    private val locationPermissionCode = 444
    private val locationPermission by lazy { Manifest.permission.ACCESS_FINE_LOCATION }

    private val locationListener = object : LocationListener {
        override fun onLocationChanged(locations: MutableList<Location>) {
        }

        override fun onFlushComplete(requestCode: Int) {
        }

        @Suppress("OVERRIDE_DEPRECATION")
        override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) {
        }

        override fun onProviderEnabled(provider: String) {
        }

        override fun onProviderDisabled(provider: String) {
        }

        override fun onLocationChanged(p0: Location) {
            toast("-> update location")
            onLocationUpdated(p0)
        }
    }

    private val locationCallback = object : LocationCallback() {
        override fun onLocationResult(locationResult: LocationResult) {
            super.onLocationResult(locationResult)

            isGpsAvailable = true
            locationResult.lastLocation?.let { onLocationUpdated(it) }
        }

        override fun onLocationAvailability(p0: LocationAvailability) {
            super.onLocationAvailability(p0)
            isGpsAvailable = p0.isLocationAvailable || currentLocation != null
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityLocationDemoBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupGoogleApiClient()
        setEvent()
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        when (requestCode) {
            locationPermissionCode -> handleLocationPermissionResult(permissions, grantResults)
            else -> super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        }
    }

    override fun onComplete(p0: Task<Location>) {
        if (p0.isSuccessful) {
            p0.result?.also { onLocationUpdated(it) }
        }
    }

    private fun handleLocationPermissionResult(perms: Array<out String>, grantResults: IntArray) {
        if (grantResults.isEmpty()) return
        if (perms[0] == locationPermission) {
            when (PackageManager.PERMISSION_GRANTED) {
                grantResults[0] -> checkDeviceLocationSetting()
                else -> toast("ไม่ได้รับสิทธิ์การเข้าถึงตำแหน่ง")
            }
        }
    }

    private fun checkDeviceSettingLocationEnable(callback: () -> Unit) {
        if (isDeviceSettingLocationEnable) {
            callback()
        } else {
            showPopupEnableLocationSettings {
                startActivity(Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS))
            }
        }
    }

    private fun setEvent() {
        binding.btnGetLocation.setOnClickListener { checkPermission() }
    }

    private fun checkDeviceLocationSetting() {
        checkDeviceSettingLocationEnable { prepareShowCurrentLocation() }
    }

    private fun checkPermission() {
        when {
            isLocationPermissionGranted -> checkDeviceLocationSetting()
            else -> checkShouldShowRationale {
                requestPermission(locationPermission, locationPermissionCode)
            }
        }
    }

    private fun checkShouldShowRationale(callback: () -> Unit) {
        if (shouldShowRationale(locationPermission)) {
            showRationaleLocation {
                callback.invoke()
            }
        } else {
            callback.invoke()
        }
    }

    private fun prepareShowCurrentLocation() {
        if (isDeviceSettingLocationEnable) {
            currentLocation
                ?.also { updateLocationTextView() }
                ?: run { getCurrentLocation() }
        }
    }

    private fun getCurrentLocation() {
        showLoading()

        if (!this::providerClient.isInitialized) {
            setupGoogleApiClient()
        }

        when {
            !hasGooglePlayServices -> toast("-> by location manager")
            else -> toast("-> by Play Services")
        }

        when {
            !hasGooglePlayServices -> getLocation(locationListener)
            else -> requestLocationUpdate(providerClient, locationCallback)
        }
    }

    @Suppress("MissingPermission")
    private fun setupGoogleApiClient() {
        if (isLocationPermissionGranted) {
            providerClient = LocationServices.getFusedLocationProviderClient(this)
            providerClient.lastLocation.addOnCompleteListener(this, this)
        }
    }

    private fun updateLocationTextView() {
        binding.apply {
            txtLongitude.text = currentLocation?.longitude.toString()
            txtLatitude.text = currentLocation?.latitude.toString()
        }
    }

    private fun onLocationUpdated(location: Location) {
        currentLocation = location
        updateLocationTextView()
        hideLoading()
    }

    private fun hideLoading() {
        binding.progressLoading.visibility = View.GONE
    }

    private fun showLoading() {
        binding.progressLoading.visibility = View.VISIBLE
    }

}
