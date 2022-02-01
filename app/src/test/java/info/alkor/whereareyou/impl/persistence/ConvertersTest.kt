package info.alkor.whereareyou.impl.persistence

import info.alkor.whereareyou.common.*
import info.alkor.whereareyou.model.action.*
import info.alkor.whereareyou.model.location.*
import org.junit.Assert.assertEquals
import org.junit.Test
import java.util.*

class ConvertersTest {
    @Test
    fun testPersonConversion() {
        val data = arrayListOf(
                Person(PhoneNumber("+48123456789"), "Mariusz"),
                Person(PhoneNumber("+48987654321")),
                Person(PhoneNumber.OWN))
        data.forEach {
            assertEquals(it, it.toRecord().toModel())
        }
    }

    @Test
    fun testLocationActionConversion() {
        val data = LocationAction(
            id = 13,
            direction = Direction.INCOMING,
            person = Person(PhoneNumber("+48123456789"), "Mariusz"),
            time = Date(),
            location = LocationImpl(
                provider = Provider.GPS,
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
        assertEquals(data, data.toRecord().toModel())
    }

    @Test
    fun testDirectionConversion() {
        Direction.values().forEach {
            assertEquals(it, Converters.stringToDirection(Converters.directionToString(it)))
        }
    }

    @Test
    fun testSendingStatusConversion() {
        SendingStatus.values().forEach {
            assertEquals(it, Converters.stringToSendingStatus(Converters.sendingStatusToString(it)))
        }
    }

    @Test
    fun testProviderConversion() {
        Provider.values().forEach {
            assertEquals(it, Converters.stringToProvider(Converters.providerToString(it)))
        }
    }

    @Test
    fun testDateConversion() {
        val now = Date()
        assertEquals(now, Converters.longToDate(Converters.dateToLong(now)))
    }
}