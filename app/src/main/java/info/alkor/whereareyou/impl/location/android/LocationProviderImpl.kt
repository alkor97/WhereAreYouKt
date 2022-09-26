package info.alkor.whereareyou.impl.location.android

import android.annotation.SuppressLint
import android.content.Context
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.location.LocationRequest
import android.os.Build
import android.os.CancellationSignal
import android.os.HandlerThread
import android.util.Log
import androidx.annotation.RequiresApi
import info.alkor.whereareyou.common.*
import info.alkor.whereareyou.impl.location.AbstractLocationProvider
import info.alkor.whereareyou.model.location.*
import kotlinx.coroutines.suspendCancellableCoroutine
import java.util.*
import java.util.concurrent.Executor
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

class LocationProviderImpl(private val context: Context) : AbstractLocationProvider(Provider.values()) {

    private val locationManager: LocationManager =
        context.getSystemService(Context.LOCATION_SERVICE) as LocationManager

    @SuppressLint("MissingPermission")
    override suspend fun requestLocation(provider: Provider, timeout: Duration): ComputedLocation? =
        suspendCancellableCoroutine { continuation ->
            val requester = getRequester(provider)
            Log.i(loggingTag, "requesting location from $provider with ${requester.javaClass.simpleName}")
            try {
                requester.start()
                continuation.invokeOnCancellation {
                    requester.stop()
                    Log.d(loggingTag, "cancelled location request from $provider")
                }
                requester.request(timeout) { location ->
                    requester.stop()
                    continuation.resume(location?.toModelLocation())
                    Log.d(loggingTag, "location from $provider is $location")
                }
            } catch (e: Exception) {
                requester.stop()
                continuation.resumeWithException(e)
            }
        }

    private interface Requester {
        fun start()
        fun stop()
        fun request(timeout: Duration, consumer: (Location?) -> Unit)
    }

    private class DefaultRequester(private val locationManager: LocationManager,
                                   private val provider: Provider): Requester {

        private val thread = HandlerThread("$provider-thread")
        private var listener: LocationListener? = null

        override fun start() {
            thread.start()
        }

        override fun stop() {
            val listener = this.listener
            if (listener != null) {
                locationManager.removeUpdates(listener)
                this.listener = null
            }
            thread.quitSafely()
        }

        @SuppressLint("MissingPermission")
        override fun request(timeout: Duration, consumer: (Location?) -> Unit) {
            val listener = LocationListener { location -> consumer(location) }
            this.listener = listener
            locationManager.requestSingleUpdate(
                provider.toAndroidProvider(),
                listener,
                thread.looper
            )
        }
    }

    @RequiresApi(Build.VERSION_CODES.R)
    @SuppressLint("MissingPermission")
    private open class AndroidRRequester(protected val locationManager: LocationManager,
                                         protected val provider: Provider,
                                         protected val executor: Executor): Requester {

        protected val cancellationSignal = CancellationSignal()

        override fun start() { /* do nothing */ }

        override fun stop() {
            cancellationSignal.cancel()
        }

        override fun request(timeout: Duration, consumer: (Location?) -> Unit) {
            locationManager.getCurrentLocation(
                provider.toAndroidProvider(),
                cancellationSignal,
                executor,
                consumer)
        }
    }

    @RequiresApi(Build.VERSION_CODES.S)
    @SuppressLint("MissingPermission")
    private class AndroidSRequester(locationManager: LocationManager,
                                    provider: Provider,
                                    executor: Executor): AndroidRRequester(locationManager, provider, executor) {

        override fun request(timeout: Duration, consumer: (Location?) -> Unit) {
            val request = LocationRequest.Builder(0)
                .setMaxUpdates(1)
                .setDurationMillis(timeout.toMillis())
                .build()
            locationManager.getCurrentLocation(
                provider.toAndroidProvider(),
                request,
                cancellationSignal,
                executor,
                consumer)
        }
    }

    private fun getRequester(provider: Provider): Requester {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            return AndroidSRequester(locationManager, provider, context.mainExecutor)
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            return AndroidRRequester(locationManager, provider, context.mainExecutor)
        }
        return DefaultRequester(locationManager, provider)
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

fun Location.toModelLocation() = ComputedLocation(
    toModelProvider(),
    Date(time),
    toModelCoordinates(),
    toModelAltitude(),
    toModelBearing(),
    toModelSpeed()
)