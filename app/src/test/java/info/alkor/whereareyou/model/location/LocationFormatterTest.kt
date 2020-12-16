package info.alkor.whereareyou.model.location

import info.alkor.whereareyou.common.*
import org.junit.Assert.*
import org.junit.Test
import java.util.*

class LocationFormatterTest {

    private fun location(
            provider: Provider, date: Date,
            latitude: Double, longitude: Double, accuracy: Double? = null,
            altitude: Double? = null, altitudeAccuracy: Double? = null,
            bearing: Double? = null, bearingAccuracy: Double? = null,
            speed: Double? = null, speedAccuracy: Double? = null) = Location(
            provider,
            date,
            Coordinates(
                    Latitude(latitude),
                    Longitude(longitude),
                    if (accuracy != null) Distance(accuracy) else null
            ),
            if (altitude != null) Altitude(Distance(altitude),
                    if (altitudeAccuracy != null) Distance(altitudeAccuracy) else null)
            else null,
            if (bearing != null) Bearing(Azimuth(bearing),
                    if (bearingAccuracy != null) Azimuth(bearingAccuracy) else null)
            else null,
            if (speed != null) Speed(Velocity(speed),
                    if (speedAccuracy != null) Velocity(speedAccuracy) else null)
            else null)

    private fun verify(expected: Location) {
        val expectedText = LocationFormatter.format(expected)
        val actual = LocationFormatter.parse(expectedText)

        if (actual != null) {
            val actualText = LocationFormatter.format(actual)
            assertEquals(expectedText, actualText)
        } else {
            assertNotNull(actual)
        }
    }

    @Test
    fun testCompleteForm() {
        verify(location(
                Provider.NETWORK,
                Date(), 53.1, 14.2, 10.0,
                123.0, 2.1,
                37.0, 12.5,
                123.4, 10.1))
    }

    @Test
    fun testMinimalForm() {
        verify(location(
                Provider.NETWORK,
                Date(), 53.1, 14.2))
    }

    @Test
    fun testParsing() {
        val text = "20170513213841,gps,53,14,20,13,2,137,15,21,2"
        val parsed = LocationFormatter.parse(text)
        assertNotNull(parsed)
        if (parsed != null) {
            assertEquals(Provider.GPS, parsed.provider)
            assertEquals(Coordinates(Latitude(53.0), Longitude(14.0), Distance(20.0)), parsed.coordinates)
            assertEquals(Altitude(Distance(13.0), Distance(2.0)), parsed.altitude)
            assertEquals(Bearing(Azimuth(137.0), Azimuth(15.0)), parsed.bearing)
            assertEquals(Speed(Velocity(21.0), Velocity(2.0)), parsed.speed)

            assertEquals(text, LocationFormatter.format(parsed))
        }
    }

    @Test
    fun verifyParsingFailure() {
        assertNull(LocationFormatter.parse("dummy,network,53,14,20,13,2,137,15,21,2"))
        assertNull(LocationFormatter.parse("20170513213841,dummy,53,14,20,13,2,137,15,21,2"))
        assertNull(LocationFormatter.parse("20170513213841,network,dummy,14,20,13,2,137,15,21,2"))
        assertNull(LocationFormatter.parse("20170513213841,network,53,dummy,20,13,2,137,15,21,2"))
        assertNull(LocationFormatter.parse("20170513213841,network,53,14,dummy,13,2,137,15,21,2"))
        assertNull(LocationFormatter.parse("20170513213841,network,53,14,20,dummy,2,137,15,21,2"))
        assertNull(LocationFormatter.parse("20170513213841,network,53,14,20,13,dummy,137,15,21,2"))
        assertNull(LocationFormatter.parse("20170513213841,network,53,14,20,13,2,dummy,15,21,2"))
        assertNull(LocationFormatter.parse("20170513213841,gps,53,14,20,13,2,137,dummy,21,2"))
        assertNull(LocationFormatter.parse("20170513213841,gps,53,14,20,13,2,137,15,dummy,2"))
        assertNull(LocationFormatter.parse("20170513213841,gps,53,14,20,13,2,137,15,21,dummy"))
    }
}