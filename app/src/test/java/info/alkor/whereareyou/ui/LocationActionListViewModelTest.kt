package info.alkor.whereareyou.ui

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import info.alkor.whereareyou.common.*
import info.alkor.whereareyou.impl.context.AppContext
import info.alkor.whereareyou.impl.persistence.LocationActionRepository
import info.alkor.whereareyou.model.action.*
import info.alkor.whereareyou.model.location.*
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import java.util.*
import kotlin.collections.ArrayList

class LocationActionListViewModelTest {

    @get:Rule
    val instantExecutorRule = InstantTaskExecutorRule()

    private val repo = mockk<LocationActionRepository>()

    private val storage: LiveDataStorage<LocationAction> by lazy {
        val storage = LiveDataStorage<LocationAction>()
        every { repo.all } returns storage.all
        every { repo.remove(any()) } answers { storage.remove(firstArg<MessageId>()) }
        storage
    }

    private val instance: LocationActionListViewModel by lazy {
        val app = mockk<AppContext>()
        every { app.actionsRepository } returns repo

        LocationActionListViewModel(app)
    }

    @Test
    fun testSimulateRemovalAndRestore() {
        val action1 = action(13)
        val action2 = action(15)
        val action3 = action(17)
        val allActions = arrayListOf(action1, action2, action3)
        allActions.forEach { storage.add(it) }

        assertEquals("wrong repo values", allActions, repo.all.getOrAwaitValue())

        instance.removeAction(action2.id!!, commit = false)
        assertEquals("only not marked for removal actions should be visible",
                arrayListOf(action1, action3), instance.actions.getOrAwaitValue())

        instance.restoreAction(action2.id!!)
        assertEquals("only not marked for removal actions should be visible",
                allActions, instance.actions.getOrAwaitValue())
    }

    @Test
    fun testSimulateRemovalAndRemove() {
        val action1 = action(13)
        val action2 = action(15)
        val action3 = action(17)
        val allActions = arrayListOf(action1, action2, action3)
        allActions.forEach { storage.add(it) }

        assertEquals("wrong repo values", allActions, repo.all.getOrAwaitValue())

        instance.removeAction(action2.id!!, commit = false)
        assertEquals("only not marked for removal actions should be visible",
                arrayListOf(action1, action3), instance.actions.getOrAwaitValue())

        instance.removeAction(action2.id!!, commit = true)
        assertEquals("only not removed actions should be visible",
                arrayListOf(action1, action3), instance.actions.getOrAwaitValue())
        verify { repo.remove(action2.id!!) }
    }
}

fun LiveDataStorage<LocationAction>.remove(id: MessageId) {
    val data = all.getOrAwaitValue()
    all.postValue(ArrayList(data).apply {
        removeIf { id == it.id }
    })
}

fun action(id: MessageId) = LocationAction(
    id = id,
    direction = Direction.INCOMING,
    person = Person(PhoneNumber("+48123456789"), "Mariusz"),
    time = Date(),
    location = ComputedLocation(
        provider = Provider.GPS,
        time = Date(),
        coordinates = Coordinates(
            latitude = latitudeDegrees(53.1),
            longitude = longitudeDegrees(14.2),
            accuracy = meters(13.1)
        ),
        altitude = Altitude(meters(14.2), meters(1.2)),
        bearing = Bearing(azimuthDegrees(13.3), azimuthDegrees(1.1)),
        speed = Speed(metersPerSecond(31.4), metersPerSecond(2.1))
    ),
    final = true,
    status = SendingStatus.DELIVERED,
    progress = 13.5f
)