package info.alkor.whereareyou.common

import java.util.*

sealed class DistanceUnit(private val meters: Double) {
    fun toMeters(value: Double) = value * meters
    fun fromMeters(value: Double) = value / meters
    override fun toString(): String {
        return javaClass.simpleName.toLowerCase(Locale.getDefault())
    }
}

data class Distance(override val value: Double, val unit: DistanceUnit = Meter) : WithDouble {
    private fun toMeters() = unit.toMeters(value)
    override fun equals(other: Any?) = (other as? Distance)?.toMeters() == toMeters()
    override fun hashCode() = toMeters().hashCode() + unit.hashCode()
    override fun toString() = "${value.toString(0)} $unit"

    fun <T : DistanceUnit> convertTo(unit: T) = Distance(unit.fromMeters(toMeters()), unit)
}

object Meter : DistanceUnit(1.0) {
    override fun toString() = "m"
}

fun <T : Number> meters(value: T) = Distance(value.toDouble(), Meter)

object Kilometer : DistanceUnit(1000.0) {
    override fun toString() = "km"
}

fun <T : Number> kilometers(value: T) = Distance(value.toDouble(), Kilometer)
