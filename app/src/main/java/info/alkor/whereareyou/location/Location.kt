package info.alkor.whereareyou.location

import java.util.*

data class Coordinates(
        val latitude: Double,
        val longitude: Double,
        val accuracy: Float? = null)

data class Altitude(
        val value: Double,
        val accuracy: Float? = null)

data class Speed(
        val value: Float,
        val accuracy: Float? = null)

data class Bearing(
        val value: Float,
        val accuracy: Float? = null)

data class Location(
        val provider: ProviderType,
        val time: Date,
        val coordinates: Coordinates,
        val altitude: Altitude? = null,
        val bearing: Bearing? = null,
        val speed: Speed? = null)
