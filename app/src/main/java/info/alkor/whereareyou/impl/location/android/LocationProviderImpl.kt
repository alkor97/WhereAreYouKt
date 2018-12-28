package info.alkor.whereareyou.impl.location.android

import android.annotation.SuppressLint
import android.content.Context
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import info.alkor.whereareyou.common.*
import info.alkor.whereareyou.impl.location.AbstractLocationProvider
import info.alkor.whereareyou.model.location.*
import kotlinx.coroutines.experimental.android.UI
import java.util.*
import kotlin.coroutines.experimental.suspendCoroutine

class LocationProviderImpl(
        private val context: Context,
        private val locationManager: LocationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager)
    : AbstractLocationProvider(Provider.values(), UI) {

    @SuppressLint("MissingPermission")
    override suspend fun requestLocation(provider: Provider): info.alkor.whereareyou.model.location.Location? = suspendCoroutine { continuation ->
        try {
            locationManager.requestSingleUpdate(provider.toAndroidProvider(), object : LocationListener {
                override fun onLocationChanged(location: Location?) {
                    continuation.resume(location?.toModelLocation())
                }

                override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) {}
                override fun onProviderEnabled(provider: String?) {}
                override fun onProviderDisabled(provider: String?) {}
            }, null)
        } catch (e: Exception) {
            continuation.resumeWithException(e)
        }
    }
}

fun Provider.toAndroidProvider() = when (this) {
    Provider.GPS -> LocationManager.GPS_PROVIDER
    else -> LocationManager.NETWORK_PROVIDER
}

fun Location.toModelProvider() = when (provider) {
    LocationManager.GPS_PROVIDER -> Provider.GPS
    else -> Provider.NETWORK
}

fun Location.toModelCoordinates() = Coordinates(
        latitudeDegrees(latitude),
        longitudeDegrees(longitude),
        if (hasAccuracy()) meters(accuracy.toDouble()) else null)

fun Location.toModelAltitude() =
        if (hasAltitude())
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && hasVerticalAccuracy())
                Altitude(meters(altitude), meters(verticalAccuracyMeters))
            else
                Altitude(meters(altitude))
        else null

fun Location.toModelSpeed() =
        if (hasSpeed())
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && hasSpeedAccuracy())
                Speed(metersPerSecond(speed.toDouble()), metersPerSecond(speedAccuracyMetersPerSecond.toDouble()))
            else
                Speed(metersPerSecond(speed.toDouble()))
        else null

fun Location.toModelBearing() =
        if (hasBearing())
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && hasBearingAccuracy())
                Bearing(azimuthDegrees(bearing.toDouble()), azimuthDegrees(bearingAccuracyDegrees.toDouble()))
            else
                Bearing(azimuthDegrees(bearing.toDouble()))
        else null

fun Location.toModelLocation() = Location(
        toModelProvider(),
        Date(time),
        toModelCoordinates(),
        toModelAltitude(),
        toModelBearing(),
        toModelSpeed())