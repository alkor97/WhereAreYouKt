package info.alkor.whereareyou.model.location

import info.alkor.whereareyou.common.*

import java.util.*

data class Coordinates(
    val latitude: Latitude,
    val longitude: Longitude,
    val accuracy: Distance? = null
) {
    override fun toString() = "$latitude $longitude" + if (accuracy != null) " ±$accuracy" else ""
}

typealias Altitude = AccurateValue<Distance>
typealias Bearing = AccurateValue<Azimuth>
typealias Speed = AccurateValue<Velocity>

interface Location {
    val provider: Provider
    val coordinates: Coordinates
    val altitude: Altitude?
    val bearing: Bearing?
    val speed: Speed?
}

data class ComputedLocation(
    override val provider: Provider,
    val time: Date,
    override val coordinates: Coordinates,
    override val altitude: Altitude? = null,
    override val bearing: Bearing? = null,
    override val speed: Speed? = null
) : Location

enum class Provider {
    GPS, NETWORK
}

fun Speed.toKilometersPerHour() =
    AccurateValue(this.value.toKilometersPerHour(), this.accuracy?.toKilometersPerHour())
