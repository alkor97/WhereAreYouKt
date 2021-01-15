package info.alkor.whereareyou.common

import org.junit.Assert.assertEquals
import org.junit.Test

class DistanceTest {
    @Test
    fun testConversions() {
        assertEquals(meters(1000), kilometers(1))
        val converted = meters(1000).convertTo(Kilometer)
        assertEquals(1.0, converted.value, 0.1)
    }
}