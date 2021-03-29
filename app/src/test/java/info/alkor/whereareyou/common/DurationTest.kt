package info.alkor.whereareyou.common

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class DurationTest {
    @Test
    fun testToString() {
        assertEquals("2h 13msec", duration(hours = 2, millis = 13).toString())
    }

    @Test
    fun testAdding() {
        assertEquals(duration(days = 3, minutes = 4), duration(days = 3) + duration(minutes = 4))
    }

    @Test
    fun testComparison() {
        assertTrue(hours(3) < days(1))
        assertTrue(millis(5) > nanos(123))
        assertTrue(micros(43) == micros(43))
    }
}