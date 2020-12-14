package info.alkor.whereareyou.impl.location

import info.alkor.whereareyou.common.*
import info.alkor.whereareyou.model.location.Coordinates
import info.alkor.whereareyou.model.location.Location
import info.alkor.whereareyou.model.location.Provider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.consumeEach
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.fail
import org.junit.Test
import java.util.*

@ExperimentalCoroutinesApi
class LocationProviderTest {

    data class Response(val delay: Long, val accuracy: Double?)

    class LocationProviderImpl(private val responses: Map<Provider, Response>)
        : AbstractLocationProvider(locationCoroutineContext = Dispatchers.Default) {
        override suspend fun requestLocation(provider: Provider): Location? {
            val response = responses[provider]
            if (response != null) {
                delay(response.delay)
                return Location(provider,
                        Date(),
                        Coordinates(
                                latitudeDegrees(0.0),
                                longitudeDegrees(0.0),
                                if (response.accuracy != null) meters(response.accuracy) else null))
            }
            return null
        }
    }

    @Test
    fun testNoLocationResponse() = runBlocking {
        val locationProvider = LocationProviderImpl(EnumMap(Provider::class.java))
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
    fun testTimeAndAccuracyBasedLocationResponse() = runBlocking {
        val responses = hashMapOf(
                Provider.GPS to Response(600, 10.0),
                Provider.NETWORK to Response(400, 100.0)
        )
        val locationProvider = LocationProviderImpl(responses)
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

    private fun expect(provider: Provider, final: Boolean, locationFound: LocationFound) {
        assertEquals(provider, locationFound.location?.provider)
        assertEquals(final, locationFound.final)
    }
}