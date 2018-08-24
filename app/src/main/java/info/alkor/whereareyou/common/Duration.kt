package info.alkor.whereareyou.common

import java.util.concurrent.TimeUnit

fun TimeUnit.asString(): String = when {
    this == TimeUnit.DAYS -> "d"
    this == TimeUnit.HOURS -> "h"
    this == TimeUnit.MINUTES -> "min"
    this == TimeUnit.SECONDS -> "sec"
    this == TimeUnit.MILLISECONDS -> "msec"
    this == TimeUnit.MICROSECONDS -> "usec"
    this == TimeUnit.NANOSECONDS -> "nsec"
    else -> javaClass.simpleName.toLowerCase()
}

data class Duration(val value: Long, val unit: TimeUnit) {
    private fun toSmallest() = unit.toNanos(value)
    override fun equals(other: Any?) = (other as? Duration)?.toSmallest() == toSmallest()
    override fun hashCode() = toSmallest().hashCode() + unit.hashCode()
    override fun toString(): String {
        var milliseconds = toSmallest()
        return arrayOf(TimeUnit.DAYS, TimeUnit.HOURS, TimeUnit.MINUTES, TimeUnit.SECONDS, TimeUnit.MILLISECONDS, TimeUnit.MICROSECONDS, TimeUnit.NANOSECONDS)
                .filter { it >= unit }
                .mapNotNull {
                    val unitInNanos = it.toNanos(1)
                    val units = milliseconds / unitInNanos
                    milliseconds %= unitInNanos
                    if (units != 0L) "$units${it.asString()}" else null
                }
                .joinToString(" ")
    }

    fun <T : TimeUnit> convertTo(unit: T) = Duration(convertValue(unit), unit)

    fun <T : TimeUnit> convertValue(unit: T) = unit.convert(this.value, this.unit)

    operator fun plus(other: Duration): Duration {
        val resultUnit = if (other.unit < unit) other.unit else unit
        return Duration(convertValue(resultUnit) + other.convertValue(resultUnit), resultUnit)
    }

    fun toNanos() = unit.toNanos(value)
    fun toMicros() = unit.toMicros(value)
    fun toMillis() = unit.toMillis(value)
    fun toSeconds() = unit.toSeconds(value)
    fun toMinutes() = unit.toMinutes(value)
    fun toHours() = unit.toHours(value)
    fun toDays() = unit.toDays(value)
}

fun <T : Number> nanos(value: T) = Duration(value.toLong(), TimeUnit.NANOSECONDS)

fun <T : Number> micros(value: T) = Duration(value.toLong(), TimeUnit.MICROSECONDS)

fun <T : Number> millis(value: T) = Duration(value.toLong(), TimeUnit.MILLISECONDS)

fun <T : Number> seconds(value: T) = Duration(value.toLong(), TimeUnit.SECONDS)

fun <T : Number> minutes(value: T) = Duration(value.toLong(), TimeUnit.MINUTES)

fun <T : Number> hours(value: T) = Duration(value.toLong(), TimeUnit.HOURS)

fun <T : Number> days(value: T) = Duration(value.toLong(), TimeUnit.DAYS)

fun <T : Number> duration(days: T? = null, hours: T? = null, minutes: T? = null, seconds: T? = null, millis: T? = null, micros: T? = null, nanos: T? = null): Duration {
    return nanos(nanos ?: 0) + micros(micros ?: 0) + millis(millis ?: 0) + seconds(seconds
            ?: 0) + minutes(minutes ?: 0) + hours(hours ?: 0) + days(days ?: 0)
}
