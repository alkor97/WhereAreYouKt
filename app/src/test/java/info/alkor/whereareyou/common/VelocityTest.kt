package info.alkor.whereareyou.common

import junit.framework.Assert
import org.junit.Test

class VelocityTest {
    @Test
    fun testConversions() {
        Assert.assertEquals(kilometersPerHour(3.6), metersPerSecond(1))
        val converted = kilometersPerHour(3.6).convertTo(MetersPerSecond)
        Assert.assertEquals(1.0, converted.value)
    }
}