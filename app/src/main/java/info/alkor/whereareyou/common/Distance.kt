package info.alkor.whereareyou.common

sealed class DistanceUnit(private val meters: Double) {
    fun toMeters(value: Double) = value * meters
    fun fromMeters(value: Double) = value / meters
    override fun toString(): String {
        return javaClass.simpleName.toLowerCase()
    }
}

data class Distance(val value: Double, val unit: DistanceUnit) {
    private fun toMeters() = unit.toMeters(value)
    override fun equals(other: Any?) = (other as? Distance)?.toMeters() == toMeters()
    override fun hashCode() = toMeters().hashCode() + unit.hashCode()
    override fun toString() = "$value $unit"

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
