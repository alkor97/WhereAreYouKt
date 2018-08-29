package info.alkor.whereareyou.common

import org.junit.Assert
import org.junit.Test
import java.util.*

class DateTimeFormatterTest {

    private val now = now()
    private val instance = DateTimeFormatter(Locale.GERMANY)

    @Test
    fun testSameDay() {
        val time = now()
        time.roll(Calendar.HOUR_OF_DAY, -5)
        Assert.assertEquals("08:47:15", instance.formatTime(time.time, now))
    }

    @Test
    fun testSameWeek() {
        val time = now()
        time.roll(Calendar.DAY_OF_MONTH, -1)
        Assert.assertEquals("Dienstag, 13:47:15", instance.formatTime(time.time, now))
    }

    @Test
    fun testSameYear() {
        val time = now()
        time.roll(Calendar.MONTH, -1)
        Assert.assertEquals("29 Juli, 13:47:15", instance.formatTime(time.time, now))
    }

    @Test
    fun testGenericDate() {
        val time = now()
        time.roll(Calendar.YEAR, -1)
        Assert.assertEquals("29. August 2017 13:47:15", instance.formatTime(time.time, now))
    }

    private fun now() = calendar(2018, 8, 29, 13, 47, 15)

    private fun calendar(year: Int, month: Int, dayOfMonth: Int, hourOfDay: Int, minute: Int, second: Int = 0): Calendar {
        val c = Calendar.getInstance()
        c.set(Calendar.YEAR, year)
        c.set(Calendar.MONTH, month - 1)
        c.set(Calendar.DAY_OF_MONTH, dayOfMonth)
        c.set(Calendar.HOUR_OF_DAY, hourOfDay)
        c.set(Calendar.MINUTE, minute)
        c.set(Calendar.SECOND, second)
        c.set(Calendar.MILLISECOND, 0)
        return c
    }
}