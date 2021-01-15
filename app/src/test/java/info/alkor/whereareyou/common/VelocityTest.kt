package info.alkor.whereareyou.common

import org.junit.Assert.assertEquals
import org.junit.Test

class VelocityTest {
    @Test
    fun testConversions() {
        assertEquals(kilometersPerHour(3.6), metersPerSecond(1))
        val converted = kilometersPerHour(3.6).convertTo(MetersPerSecond)
        assertEquals(1.0, converted.value, 0.1)
    }
}