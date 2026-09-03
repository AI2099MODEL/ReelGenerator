package com.example.util

import android.annotation.SuppressLint
import android.content.Context
import android.location.Geocoder
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Build
import android.os.Bundle
import android.os.Looper
import android.util.Log
import androidx.core.content.ContextCompat
import java.util.Locale

data class GpsLocationResult(
    val locationName: String,
    val latitude: Double,
    val longitude: Double,
    val accuracyMeters: Float,
    val provider: String,
    val isGpsActive: Boolean = true
)

object GpsLocationTracker {
    private const val TAG = "GpsLocationTracker"

    fun hasLocationPermission(context: Context): Boolean {
        val fineGranted = ContextCompat.checkSelfPermission(
            context,
            android.Manifest.permission.ACCESS_FINE_LOCATION
        ) == android.content.pm.PackageManager.PERMISSION_GRANTED
        val coarseGranted = ContextCompat.checkSelfPermission(
            context,
            android.Manifest.permission.ACCESS_COARSE_LOCATION
        ) == android.content.pm.PackageManager.PERMISSION_GRANTED
        return fineGranted || coarseGranted
    }

    @SuppressLint("MissingPermission")
    fun getCurrentGpsLocation(
        context: Context,
        onResult: (GpsLocationResult) -> Unit
    ) {
        if (!hasLocationPermission(context)) {
            Log.w(TAG, "Location permission not granted")
            onResult(
                GpsLocationResult(
                    locationName = "Location Permission Required",
                    latitude = 37.7749,
                    longitude = -122.4194,
                    accuracyMeters = 0f,
                    provider = "None",
                    isGpsActive = false
                )
            )
            return
        }

        val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as? LocationManager
        if (locationManager == null) {
            onResult(
                GpsLocationResult(
                    locationName = "GPS Service Unavailable",
                    latitude = 37.7749,
                    longitude = -122.4194,
                    accuracyMeters = 0f,
                    provider = "Error",
                    isGpsActive = false
                )
            )
            return
        }

        val isGpsEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
        val isNetworkEnabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)

        if (!isGpsEnabled && !isNetworkEnabled) {
            onResult(
                GpsLocationResult(
                    locationName = "GPS Hardware Disabled on Device",
                    latitude = 37.7749,
                    longitude = -122.4194,
                    accuracyMeters = 0f,
                    provider = "Disabled",
                    isGpsActive = false
                )
            )
            return
        }

        // Try getting last known location first for immediate UI response
        var lastLocation: Location? = null
        if (isGpsEnabled) {
            lastLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER)
        }
        if (lastLocation == null && isNetworkEnabled) {
            lastLocation = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER)
        }

        if (lastLocation != null) {
            val result = buildGpsResult(context, lastLocation)
            onResult(result)
        }

        // Request a fresh GPS update listener
        val providerToUse = when {
            isGpsEnabled -> LocationManager.GPS_PROVIDER
            isNetworkEnabled -> LocationManager.NETWORK_PROVIDER
            else -> LocationManager.PASSIVE_PROVIDER
        }

        val locationListener = object : LocationListener {
            override fun onLocationChanged(location: Location) {
                val freshResult = buildGpsResult(context, location)
                onResult(freshResult)
                try {
                    locationManager.removeUpdates(this)
                } catch (e: Exception) {
                    Log.e(TAG, "Error removing location updates", e)
                }
            }

            @Deprecated("Deprecated in Java")
            override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) {}
            override fun onProviderEnabled(provider: String) {}
            override fun onProviderDisabled(provider: String) {}
        }

        try {
            locationManager.requestSingleUpdate(
                providerToUse,
                locationListener,
                Looper.getMainLooper()
            )
        } catch (e: Exception) {
            Log.e(TAG, "Failed requesting single location update", e)
        }
    }

    private fun buildGpsResult(context: Context, location: Location): GpsLocationResult {
        val lat = location.latitude
        val lng = location.longitude
        val accuracy = location.accuracy
        val providerName = location.provider?.capitalize(Locale.ROOT) ?: "GPS"

        var placeName = "GPS: %.4f, %.4f".format(lat, lng)

        try {
            val geocoder = Geocoder(context, Locale.getDefault())
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                geocoder.getFromLocation(lat, lng, 1) { addresses ->
                    val addr = addresses.firstOrNull()
                    if (addr != null) {
                        val locality = addr.locality ?: addr.subAdminArea ?: addr.adminArea
                        val country = addr.countryName
                        if (locality != null && country != null) {
                            placeName = "$locality, $country (Local GPS)"
                        } else if (locality != null) {
                            placeName = "$locality (Local GPS)"
                        }
                    }
                }
            } else {
                @Suppress("DEPRECATION")
                val addresses = geocoder.getFromLocation(lat, lng, 1)
                val addr = addresses?.firstOrNull()
                if (addr != null) {
                    val locality = addr.locality ?: addr.subAdminArea ?: addr.adminArea
                    val country = addr.countryName
                    if (locality != null && country != null) {
                        placeName = "$locality, $country (Local GPS)"
                    } else if (locality != null) {
                        placeName = "$locality (Local GPS)"
                    }
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Geocoding error or offline", e)
        }

        return GpsLocationResult(
            locationName = placeName,
            latitude = lat,
            longitude = lng,
            accuracyMeters = accuracy,
            provider = "$providerName Sensor",
            isGpsActive = true
        )
    }
}
