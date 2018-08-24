package info.alkor.whereareyou.common

import junit.framework.Assert
import org.junit.Test

class DegreeTest {
    @Test
    fun testAzimuth() {
        Assert.assertEquals(13.0, azimuthDegrees(13.0).value)
        Assert.assertEquals(0.0, azimuthDegrees(360.0).value)
        Assert.assertEquals(1.0, azimuthDegrees(721.0).value)
        Assert.assertEquals(359.0, azimuthDegrees(-1.0).value)
        Assert.assertEquals(359.0, azimuthDegrees(-361.0).value)
    }

    @Test
    fun testLatitude() {
        Assert.assertEquals(13.0, latitudeDegrees(13.0).value)
        Assert.assertEquals(90.0, latitudeDegrees(90.0).value)
        Assert.assertEquals(89.0, latitudeDegrees(91.0).value)
        Assert.assertEquals(89.0, latitudeDegrees(91.0 + 180.0).value)
        Assert.assertEquals(-90.0, latitudeDegrees(-90.0).value)
        Assert.assertEquals(-89.0, latitudeDegrees(-91.0).value)
        Assert.assertEquals(-89.0, latitudeDegrees(-91.0 - 180.0).value)
    }

    @Test
    fun testLongitude() {
        Assert.assertEquals(13.0, longitudeDegrees(13.0).value)
        Assert.assertEquals(-180.0, longitudeDegrees(180.0).value)
        Assert.assertEquals(-179.0, longitudeDegrees(181.0).value)
        Assert.assertEquals(-179.0, longitudeDegrees(181.0 + 360.0).value)
        Assert.assertEquals(-180.0, longitudeDegrees(-180.0).value)
        Assert.assertEquals(179.0, longitudeDegrees(-181.0).value)
        Assert.assertEquals(179.0, longitudeDegrees(-181.0 - 360.0).value)
    }
}