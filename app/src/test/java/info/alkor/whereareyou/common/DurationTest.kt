package info.alkor.whereareyou.common

import org.junit.Assert.assertEquals
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
}