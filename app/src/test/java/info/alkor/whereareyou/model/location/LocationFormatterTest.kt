package info.alkor.whereareyou.model.location

import info.alkor.whereareyou.common.*
import junit.framework.Assert
import org.junit.Test
import java.util.*

class LocationFormatterTest {

    private fun location(
            provider: Provider, date: Date,
            latitude: Double, longitude: Double, accuracy: Double? = null,
            altitude: Double? = null,
            bearing: Double? = null,
            speed: Double? = null) = Location(
            provider,
            date,
            Coordinates(
                    Latitude(latitude),
                    Longitude(longitude),
                    if (accuracy != null) Distance(accuracy) else null
            ),
            if (altitude != null) Altitude(Distance(altitude)) else null,
            if (bearing != null) Bearing(Azimuth(bearing)) else null,
            if (speed != null) Speed(Velocity(speed)) else null)

    private fun verify(expected: Location) {
        val expectedText = LocationFormatter.format(expected)
        val actual = LocationFormatter.parse(expectedText)

        if (actual != null) {
            val actualText = LocationFormatter.format(actual)
            Assert.assertEquals(expectedText, actualText)
        } else {
            Assert.assertNotNull(actual)
        }
    }

    @Test
    fun testCompleteForm() {
        verify(location(
                Provider.NETWORK,
                Date(), 53.1, 14.2, 10.0, 123.0, 12.0, 1.5))
    }

    @Test
    fun testMinimalForm() {
        verify(location(
                Provider.NETWORK,
                Date(), 53.1, 14.2))
    }

    @Test
    fun verifyParsingFailure() {
        Assert.assertNull(LocationFormatter.parse("dummy,network,53.1,14.2,123,10,12,1.5"))
        Assert.assertNull(LocationFormatter.parse("20181125130530,dummy,53.1,14.2,123,10,12,1.5"))
        Assert.assertNull(LocationFormatter.parse("20181125130530,network,dummy,14.2,123,10,12,1.5"))
        Assert.assertNull(LocationFormatter.parse("20181125130530,network,53.1,dummy,123,10,12,1.5"))

        Assert.assertNull(LocationFormatter.parse("20181125130530,network,53.1,14.2,dummy,10,12,1.5"))
        Assert.assertNull(LocationFormatter.parse("20181125130530,network,53.1,14.2,123,dummy,12,1.5"))
        Assert.assertNull(LocationFormatter.parse("20181125130530,network,53.1,14.2,123,10,dummy,1.5"))
        Assert.assertNull(LocationFormatter.parse("20181125130530,network,53.1,14.2,123,10,12,dummy"))
    }
}