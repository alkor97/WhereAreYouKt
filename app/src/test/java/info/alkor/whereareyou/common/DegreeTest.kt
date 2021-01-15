package info.alkor.whereareyou.common

import org.junit.Assert.assertEquals
import org.junit.Test

class DegreeTest {

    private val delta = 0.1

    @Test
    fun testAzimuth() {
        assertEquals(13.0, azimuthDegrees(13.0).value, delta)
        assertEquals(0.0, azimuthDegrees(360.0).value, delta)
        assertEquals(1.0, azimuthDegrees(721.0).value, delta)
        assertEquals(359.0, azimuthDegrees(-1.0).value, delta)
        assertEquals(359.0, azimuthDegrees(-361.0).value, delta)
    }

    @Test
    fun testLatitude() {
        assertEquals(13.0, latitudeDegrees(13.0).value, delta)
        assertEquals(90.0, latitudeDegrees(90.0).value, delta)
        assertEquals(89.0, latitudeDegrees(91.0).value, delta)
        assertEquals(89.0, latitudeDegrees(91.0 + 180.0).value, delta)
        assertEquals(-90.0, latitudeDegrees(-90.0).value, delta)
        assertEquals(-89.0, latitudeDegrees(-91.0).value, delta)
        assertEquals(-89.0, latitudeDegrees(-91.0 - 180.0).value, delta)
    }

    @Test
    fun testLongitude() {
        assertEquals(13.0, longitudeDegrees(13.0).value, delta)
        assertEquals(-180.0, longitudeDegrees(180.0).value, delta)
        assertEquals(-179.0, longitudeDegrees(181.0).value, delta)
        assertEquals(-179.0, longitudeDegrees(181.0 + 360.0).value, delta)
        assertEquals(-180.0, longitudeDegrees(-180.0).value, delta)
        assertEquals(179.0, longitudeDegrees(-181.0).value, delta)
        assertEquals(179.0, longitudeDegrees(-181.0 - 360.0).value, delta)
    }
}