package info.alkor.whereareyou.location.android

import android.annotation.SuppressLint
import android.content.Context
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Build
import android.os.Bundle
import info.alkor.whereareyou.location.*
import java.util.*
import kotlin.coroutines.experimental.suspendCoroutine

class LocationProvider(
        private val context: Context,
        private val locationManager: LocationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager)
    : AbstractLocationProvider(ProviderType.values()) {

    @SuppressLint("MissingPermission")
    override suspend fun requestLocation(provider: ProviderType): info.alkor.whereareyou.location.Location? = suspendCoroutine { continuation ->
        locationManager.requestSingleUpdate(provider.toAndroidProvider(), object : LocationListener {
            override fun onLocationChanged(location: Location?) {
                continuation.resume(location?.toModelLocation())
            }

            override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) {}
            override fun onProviderEnabled(provider: String?) {}
            override fun onProviderDisabled(provider: String?) {}
        }, null)
    }
}

fun ProviderType.toAndroidProvider() = when (this) {
    ProviderType.GPS -> LocationManager.GPS_PROVIDER
    ProviderType.NETWORK -> LocationManager.NETWORK_PROVIDER
    ProviderType.PASSIVE -> LocationManager.PASSIVE_PROVIDER
}

fun Location.toModelProvider() = when (provider) {
    LocationManager.GPS_PROVIDER -> ProviderType.GPS
    LocationManager.NETWORK_PROVIDER -> ProviderType.NETWORK
    else -> ProviderType.PASSIVE
}

fun Location.toModelCoordinates() = Coordinates(latitude, longitude, if (hasAccuracy()) accuracy else null)

fun Location.toModelAltitude() =
        if (hasAltitude())
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && hasVerticalAccuracy())
                Altitude(altitude, verticalAccuracyMeters)
            else
                Altitude(altitude)
        else null

fun Location.toModelSpeed() =
        if (hasSpeed())
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && hasSpeedAccuracy())
                Speed(speed, speedAccuracyMetersPerSecond)
            else
                Speed(speed)
        else null

fun Location.toModelBearing() =
        if (hasBearing())
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && hasBearingAccuracy())
                Bearing(bearing, bearingAccuracyDegrees)
            else
                Bearing(bearing)
        else null

fun Location.toModelLocation() = Location(
        toModelProvider(),
        Date(time),
        toModelCoordinates(),
        toModelAltitude(),
        toModelBearing(),
        toModelSpeed())