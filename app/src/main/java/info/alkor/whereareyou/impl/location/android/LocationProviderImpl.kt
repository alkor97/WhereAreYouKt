package info.alkor.whereareyou.impl.location.android

import android.annotation.SuppressLint
import android.content.Context
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Build
import android.os.HandlerThread
import android.util.Log
import info.alkor.whereareyou.common.*
import info.alkor.whereareyou.impl.location.AbstractLocationProvider
import info.alkor.whereareyou.model.location.*
import kotlinx.coroutines.suspendCancellableCoroutine
import java.util.*
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

class LocationProviderImpl(context: Context) : AbstractLocationProvider(Provider.values()) {

    private val locationManager: LocationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager

    @SuppressLint("MissingPermission")
    override suspend fun requestLocation(provider: Provider): info.alkor.whereareyou.model.location.Location? = suspendCancellableCoroutine { continuation ->
        val thread = HandlerThread("$provider-thread")
        thread.start()
        try {
            val listener = object : LocationListener {
                override fun onLocationChanged(location: Location) {
                    locationManager.removeUpdates(this)
                    thread.quitSafely()
                    continuation.resume(location.toModelLocation())
                }
            }
            continuation.invokeOnCancellation {
                locationManager.removeUpdates(listener)
                thread.quitSafely()
                Log.d(loggingTag, "cancelled location request from $provider")
            }
            locationManager.requestSingleUpdate(provider.toAndroidProvider(), listener, thread.looper)
        } catch (e: Exception) {
            thread.quitSafely()
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