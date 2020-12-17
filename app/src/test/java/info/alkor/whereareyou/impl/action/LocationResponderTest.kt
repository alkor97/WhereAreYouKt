package info.alkor.whereareyou.impl.action

import info.alkor.whereareyou.common.latitudeDegrees
import info.alkor.whereareyou.common.longitudeDegrees
import info.alkor.whereareyou.common.minutes
import info.alkor.whereareyou.common.seconds
import info.alkor.whereareyou.impl.communication.android.SmsSender
import info.alkor.whereareyou.impl.context.AppContext
import info.alkor.whereareyou.impl.location.LocationFound
import info.alkor.whereareyou.impl.location.android.LocationProviderImpl
import info.alkor.whereareyou.impl.persistence.LocationActionRepository
import info.alkor.whereareyou.impl.settings.Settings
import info.alkor.whereareyou.model.action.*
import info.alkor.whereareyou.model.location.Coordinates
import info.alkor.whereareyou.model.location.Location
import info.alkor.whereareyou.model.location.Provider
import io.mockk.*
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import org.junit.Before
import org.junit.Test
import java.util.*

class LocationResponderTest {

    private val repository = mockk<LocationActionRepository>()
    private val settings = mockk<Settings>()
    private val locationProvider = mockk<LocationProviderImpl>()
    private val messageSender = mockk<SmsSender>()
    private val ctx = mockk<AppContext>()

    @Before
    fun before() {
        every { ctx.actionsRepository } returns repository
        every { ctx.settings } returns settings
        every { ctx.locationProvider } returns locationProvider
        every { ctx.messageSender } returns messageSender
    }

    @ExperimentalCoroutinesApi
    @Test
    fun testBasicFlow() {
        runBlocking {
            val impl = LocationResponder(ctx)

            val incoming = LocationRequest(Person(PhoneNumber("+48123456789"), "Mariusz"), null)

            val requestWithId = LocationRequest(incoming.from, 13)
            val maxAge = minutes(1)
            val channel = Channel<LocationFound>()
            val foundLocations = arrayListOf(
                    LocationFound(location(Provider.NETWORK), false),
                    LocationFound(location(Provider.GPS), false),
                    LocationFound(location(Provider.GPS), true)
            )
            val timeout = seconds(1 + foundLocations.size)

            coEvery { repository.onMyLocationRequested(incoming.from) } returns requestWithId
            every { repository.updateProgress(requestWithId.id!!, any()) } answers {}
            every { settings.getLocationQueryTimeout() } returns timeout
            every { settings.getLocationMaxAge() } returns maxAge
            every { locationProvider.getLocationChannel(timeout, maxAge) } returns channel

            val statuses = arrayListOf(SendingStatus.SENT, SendingStatus.DELIVERED)
            val statusChannel = Channel<SendingStatus>()
            coEvery { messageSender.send(LocationResponse(incoming.from, foundLocations[2].location, foundLocations[2].final)) } answers {
                CoroutineScope(Dispatchers.IO).launch {
                    statuses.forEach {
                        statusChannel.send(it)
                    }
                    statusChannel.close()
                }
                statusChannel
            }

            statuses.forEach {
                every { repository.onCommunicationStatusUpdate(requestWithId, it) } answers {}
            }

            foundLocations.forEach {
                every { repository.onLocationResponse(LocationResponse(incoming.from, it.location, it.final), requestWithId.id) } answers {}
            }

            launch {
                foundLocations.forEach {
                    delay(1000)
                    channel.send(it)
                }
                channel.close()
            }
            impl.handleLocationRequest(incoming)

            coVerify { repository.onMyLocationRequested(incoming.from) }
            verify {
                repository.updateProgress(requestWithId.id!!, any())
                foundLocations.forEach {
                    repository.onLocationResponse(LocationResponse(incoming.from, it.location, it.final), requestWithId.id)
                }
                statuses.forEach {
                    repository.onCommunicationStatusUpdate(requestWithId, it)
                }
            }
        }
    }

    private fun location(provider: Provider) = Location(
            provider,
            Date(),
            Coordinates(latitudeDegrees(53.1), longitudeDegrees(14.2))
    )
}