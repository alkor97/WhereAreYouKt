package info.alkor.whereareyou.impl.location

import android.content.Context
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.HandlerThread
import android.os.Looper
import info.alkor.whereareyou.common.minutes
import info.alkor.whereareyou.common.seconds
import info.alkor.whereareyou.impl.location.android.LocationProviderImpl
import info.alkor.whereareyou.model.location.Provider
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkConstructor
import io.mockk.verify
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.consumeEach
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.fail
import org.junit.Before
import org.junit.Test

@ExperimentalCoroutinesApi
class LocationProviderTest {

    private val scope = CoroutineScope(Dispatchers.Default)
    private val context = mockk<Context>()
    private val locationManager = mockk<LocationManager>()
    private lateinit var locationProvider: LocationProviderImpl
    private lateinit var gpsListener: LocationListener
    private lateinit var netListener: LocationListener
    private var gpsHandler: ((LocationListener) -> Unit)? = null
    private var netHandler: ((LocationListener) -> Unit)? = null
    private val looper = mockk<Looper>()

    @Before
    fun beforeTest() {
        mockkConstructor(HandlerThread::class)
        every { anyConstructed<HandlerThread>().quitSafely() } returns true
        every { anyConstructed<HandlerThread>().looper } returns looper
        every { anyConstructed<HandlerThread>().run() } answers {}

        every { context.getSystemService(Context.LOCATION_SERVICE) } returns locationManager
        locationProvider = LocationProviderImpl(context)

        every { locationManager.requestSingleUpdate("gps", any(), looper) } answers {
            gpsListener = arg(1)
            gpsHandler?.invoke(gpsListener)
        }
        every { locationManager.requestSingleUpdate("network", any(), looper) } answers {
            netListener = arg(1)
            netHandler?.invoke(netListener)
        }
        every { locationManager.removeUpdates(any<LocationListener>()) } answers {}
    }

    @After
    fun afterTest() {
        if (this::gpsListener.isInitialized || this::netListener.isInitialized) {
            verify {
                if (this@LocationProviderTest::gpsListener.isInitialized) {
                    locationManager.requestSingleUpdate("gps", gpsListener, looper)
                    locationManager.removeUpdates(gpsListener)
                }
                if (this@LocationProviderTest::netListener.isInitialized) {
                    locationManager.requestSingleUpdate("network", netListener, looper)
                    locationManager.removeUpdates(netListener)
                }
            }
        }
    }

    @Test
    fun `test no location response generated`() = runBlocking {
        var index = 0
        locationProvider.getLocationChannel(seconds(0), minutes(1)).consumeEach { msg ->
            when (index) {
                0 -> assertEquals(LocationFound(null, true), msg)
                else -> fail("too many messages")
            }
            index++
        }
    }

    @Test
    fun `test select location with better accuracy`() = runBlocking {
        gpsHandler = setupHandler(600, Provider.GPS, 10.0f)
        netHandler = setupHandler(400, Provider.NETWORK, 100.0f)

        var index = 0
        locationProvider.getLocationChannel(seconds(1), minutes(1)).consumeEach { msg ->
            when (index) {
                0 -> expect(Provider.NETWORK, false, msg)
                1 -> expect(Provider.GPS, false, msg)
                2 -> expect(Provider.GPS, true, msg)
                else -> fail("too many messages")
            }
            index++
        }
    }

    @Test
    fun `test GPS location is timed out`() = runBlocking {
        netHandler = setupHandler(200, Provider.NETWORK, 100.0f)

        var index = 0
        locationProvider.getLocationChannel(seconds(1), minutes(1)).consumeEach { msg ->
            when (index) {
                0 -> expect(Provider.NETWORK, false, msg)
                1 -> expect(Provider.NETWORK, true, msg)
                else -> fail("too many messages")
            }
            index++
        }
    }

    private fun setupHandler(delay: Long, provider: Provider, accuracy: Float) = fun(listener: LocationListener) {
        scope.launch {
            delay(delay)
            listener.onLocationChanged(mockLocation(provider, accuracy))
        }
    }

    private fun expect(provider: Provider, final: Boolean, locationFound: LocationFound) {
        assertEquals(provider, locationFound.location?.provider)
        assertEquals(final, locationFound.final)
    }

    private fun mockLocation(provider: Provider, accuracy: Float): Location {
        val location = mockk<Location>()
        every { location.provider } returns provider.name.toLowerCase()
        every { location.hasAccuracy() } returns true
        every { location.accuracy } returns accuracy
        every { location.time } returns System.currentTimeMillis()
        every { location.latitude } returns 53.0
        every { location.longitude } returns 14.0
        every { location.hasAltitude() } returns true
        every { location.altitude } returns 99.0
        every { location.hasBearing() } returns false
        every { location.hasSpeed() } returns false
        return location
    }
}
