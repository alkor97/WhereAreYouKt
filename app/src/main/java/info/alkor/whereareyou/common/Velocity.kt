package info.alkor.whereareyou.common

sealed class VelocityUnit(private val metersPerSecond: Double) {
    fun toMetersPerSecond(value: Double) = value / metersPerSecond
    fun fromMetersPerSecond(value: Double) = value * metersPerSecond
}

data class Velocity(val value: Double, val unit: VelocityUnit = MetersPerSecond) {
    private fun toBaseValue() = unit.toMetersPerSecond(value)
    override fun equals(other: Any?) = (other as? Velocity)?.toBaseValue() == toBaseValue()
    override fun hashCode() = toBaseValue().hashCode() + unit.hashCode()
    override fun toString() = "${value.toString(0)} $unit"

    fun <T : VelocityUnit> convertTo(unit: T) = Velocity(unit.fromMetersPerSecond(toBaseValue()), unit)
    fun toMetersPerSecond() = convertTo(MetersPerSecond)
    fun toKilometersPerHour() = convertTo(KilometersPerHour)
}

object MetersPerSecond : VelocityUnit(1.0) {
    override fun toString() = "m/s"
}

fun <T : Number> metersPerSecond(value: T) = Velocity(value.toDouble(), MetersPerSecond)

object KilometersPerHour : VelocityUnit(36.0 / 10.0) {
    override fun toString() = "km/h"
}

fun <T : Number> kilometersPerHour(value: T) = Velocity(value.toDouble(), KilometersPerHour)
