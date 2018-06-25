package info.alkor.whereareyou.location

import info.alkor.whereareyou.common.AccurateValue
import java.util.*

data class Coordinates(
        val latitude: Double,
        val longitude: Double,
        val accuracy: Float? = null)

typealias Altitude = AccurateValue<Double>
typealias Speed = AccurateValue<Float>
typealias Bearing = AccurateValue<Float>

data class Location(
        val provider: ProviderType,
        val time: Date,
        val coordinates: Coordinates,
        val altitude: Altitude? = null,
        val bearing: Bearing? = null,
        val speed: Speed? = null)
