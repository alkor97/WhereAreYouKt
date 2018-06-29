package info.alkor.whereareyou.location

import kotlinx.coroutines.experimental.delay
import kotlinx.coroutines.experimental.runBlocking
import org.junit.Assert
import org.junit.Test
import java.util.*

class LocationProviderTest {

    data class Response(val delay: Long, val accuracy: Float?)

    class LocationProviderImpl(private val responses: Map<ProviderType, Response>) : AbstractLocationProvider() {
        override suspend fun requestLocation(provider: ProviderType): Location? {
            val response = responses[provider]
            if (response != null) {
                delay(response.delay)
                return Location(provider, Date(), Coordinates(0.0, 0.0, response.accuracy))
            }
            return null
        }
    }

    private fun getLocationBlocking(timeout: Long, responses: Map<ProviderType, Response>) = runBlocking {
        LocationProviderImpl(responses).getLocation(timeout)
    }

    @Test
    fun testNullResponse() {
        val responses = HashMap<ProviderType, Response>()
        Assert.assertNull(getLocationBlocking(0, responses))
    }

    @Test
    fun testTimeAndAccuracyBasedLocationResponse() {
        val responses = HashMap<ProviderType, Response>()
        responses[ProviderType.GPS] = Response(600, 10.0f)
        responses[ProviderType.NETWORK] = Response(400, 100.0f)
        responses[ProviderType.PASSIVE] = Response(200, 200.0f)

        fun getLocation(type: ProviderType) = getLocationBlocking((responses[type]?.delay
                ?: 0) + 100, responses)

        Assert.assertEquals(ProviderType.PASSIVE, getLocation(ProviderType.PASSIVE)?.provider)
        Assert.assertEquals(ProviderType.NETWORK, getLocation(ProviderType.NETWORK)?.provider)
        Assert.assertEquals(ProviderType.GPS, getLocation(ProviderType.GPS)?.provider)
    }

    @Test
    fun testAccuracyLessLocationResponse() {
        val responses = HashMap<ProviderType, Response>()
        responses[ProviderType.GPS] = Response(100, 10.0f)
        responses[ProviderType.PASSIVE] = Response(100, null)

        fun getLocation(type: ProviderType) = getLocationBlocking((responses[type]?.delay
                ?: 0) + 50, responses)

        Assert.assertEquals(ProviderType.GPS, getLocation(ProviderType.GPS)?.provider)
    }
}