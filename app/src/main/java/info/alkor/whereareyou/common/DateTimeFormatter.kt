package info.alkor.whereareyou.common

import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.*

class DateTimeFormatter(locale: Locale = Locale.getDefault()) {

    private val sameDayFormat: DateFormat = DateFormat.getTimeInstance(DateFormat.MEDIUM, locale)
    private val sameWeekFormat = SimpleDateFormat("E, HH:mm:ss", locale)
    private val sameYearFormat = SimpleDateFormat("d M, HH:mm:ss", locale)
    private val genericFormat: DateFormat = DateFormat.getDateTimeInstance(DateFormat.LONG, DateFormat.MEDIUM, locale)

    fun formatTime(time: Date, now: Calendar = Calendar.getInstance()): String {
        val c = Calendar.getInstance()
        c.time = time

        if (c.isSameDay(now)) {
            return sameDayFormat.format(time)
        }
        if (c.isSameWeek(now)) {
            return sameWeekFormat.format(time)
        }
        if (c.isSameYear(now)) {
            return sameYearFormat.format(time)
        }
        return genericFormat.format(time)
    }
}

fun Calendar.year() = get(Calendar.YEAR)
fun Calendar.dayOfYear() = get(Calendar.DAY_OF_YEAR)
fun Calendar.weekOfYear() = get(Calendar.WEEK_OF_YEAR)

fun Calendar.isSameDay(other: Calendar) = year() == other.year() && dayOfYear() == other.dayOfYear()
fun Calendar.isSameWeek(other: Calendar) = year() == other.year() && weekOfYear() == other.weekOfYear()
fun Calendar.isSameYear(other: Calendar) = year() == other.year()
