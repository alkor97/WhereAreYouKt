package info.alkor.whereareyou.model.location

import info.alkor.whereareyou.common.*

import java.util.*

data class Coordinates(
        val latitude: Latitude,
        val longitude: Longitude,
        val accuracy: Distance? = null) {
    override fun toString() = "$latitude,$longitude" + if (accuracy != null) " ±$accuracy" else ""
}

typealias Altitude = AccurateValue<Distance>
typealias Bearing = AccurateValue<Azimuth>
typealias Speed = AccurateValue<Velocity>

data class Location(
        val provider: Provider,
        val time: Date,
        val coordinates: Coordinates,
        val altitude: Altitude? = null,
        val bearing: Bearing? = null,
        val speed: Speed? = null)

enum class Provider {
    GPS, NETWORK
}

fun Speed.toKilometersPerHour() = AccurateValue(this.value.toKilometersPerHour(), this.accuracy?.toKilometersPerHour())
