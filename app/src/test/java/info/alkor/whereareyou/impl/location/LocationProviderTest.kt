package info.alkor.whereareyou.impl.location

import info.alkor.whereareyou.common.*
import info.alkor.whereareyou.model.location.Coordinates
import info.alkor.whereareyou.model.location.Location
import info.alkor.whereareyou.model.location.Provider
import kotlinx.coroutines.experimental.delay
import kotlinx.coroutines.experimental.runBlocking
import org.junit.Assert
import org.junit.Test
import java.util.*
import kotlin.coroutines.experimental.suspendCoroutine

class LocationProviderTest {

    data class Response(val delay: Long, val accuracy: Double?)

    class LocationProviderImpl(private val responses: Map<Provider, Response>) : AbstractLocationProvider() {
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

    private fun getLocationBlocking(timeout: Duration, responses: Map<Provider, Response>): Location? = runBlocking {
        suspendCoroutine<Location?> { cont ->
            LocationProviderImpl(responses).getLocation(timeout, minutes(1)) { location, final -> if (final) cont.resume(location) }
        }
    }

    @Test
    fun testNullResponse() {
        val responses = HashMap<Provider, Response>()
        Assert.assertNull(getLocationBlocking(millis(0), responses))
    }

    @Test
    fun testTimeAndAccuracyBasedLocationResponse() {
        val responses = HashMap<Provider, Response>()
        responses[Provider.GPS] = Response(600, 10.0)
        responses[Provider.NETWORK] = Response(400, 100.0)

        fun getLocation(type: Provider) = getLocationBlocking(millis((responses[type]?.delay
                ?: 0) + 100), responses)

        Assert.assertEquals(Provider.NETWORK, getLocation(Provider.NETWORK)?.provider)
        Assert.assertEquals(Provider.GPS, getLocation(Provider.GPS)?.provider)
    }

    @Test
    fun testAccuracyLessLocationResponse() {
        val responses = HashMap<Provider, Response>()
        responses[Provider.GPS] = Response(100, 10.0)

        fun getLocation(type: Provider) = getLocationBlocking(millis((responses[type]?.delay
                ?: 0) + 50), responses)

        Assert.assertEquals(Provider.GPS, getLocation(Provider.GPS)?.provider)
    }
}