package info.alkor.whereareyou.common

fun Double.toString(decimal: Int) = String.format("%.${decimal}f", this)

sealed class Degree(protected val min: Double, protected val max: Double) {
    open fun normalize(value: Double): Double {
        if (min <= value && value < max)
            return value
        val span = max - min
        val modifier = if (value < min) span * Math.ceil(-value / span) else 0.0
        return min + (value + modifier - min) % span
    }

    override fun toString() = "°"
}

class LatitudeDegree : Degree(-90.0, 90.0) {
    override fun normalize(value: Double): Double {
        if (value in min..max)
            return value
        val span = max - min
        val modifier = if (value < min) span * Math.ceil(-value / span) else 0.0
        return max - (value + modifier - min) % span
    }
}

data class Latitude private constructor(val value: Double) {
    companion object {
        val unit: LatitudeDegree = LatitudeDegree()
        operator fun invoke(value: Double): Latitude {
            return Latitude(unit.normalize(value))
        }
    }

    override fun toString() = "${value.toString(6)}$unit"
}

fun <T : Number> latitudeDegrees(value: T) = Latitude(value.toDouble())

class LongitudeDegree : Degree(-180.0, 180.0)

data class Longitude private constructor(val value: Double) {
    companion object {
        val unit: LongitudeDegree = LongitudeDegree()
        operator fun invoke(value: Double): Longitude {
            return Longitude(unit.normalize(value))
        }
    }

    override fun toString() = "${value.toString(6)}$unit"
}

fun <T : Number> longitudeDegrees(value: T) = Longitude(value.toDouble())

class AzimuthDegree : Degree(0.0, 360.0)

data class Azimuth private constructor(val value: Double) {
    companion object {
        val unit: AzimuthDegree = AzimuthDegree()
        operator fun invoke(value: Double): Azimuth {
            return Azimuth(unit.normalize(value))
        }
    }

    override fun toString() = "${value.toString(6)}$unit"
}

fun <T : Number> azimuthDegrees(value: T) = Azimuth(value.toDouble())
