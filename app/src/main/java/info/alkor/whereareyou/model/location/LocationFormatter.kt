package info.alkor.whereareyou.model.location

import info.alkor.whereareyou.common.Azimuth
import info.alkor.whereareyou.common.Distance
import info.alkor.whereareyou.common.Velocity
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*

object LocationFormatter {

    private val DATE_FORMAT = SimpleDateFormat("yyyyMMddHHmmss", Locale.US)
    private val DECIMAL_FORMAT_SYMBOLS = DecimalFormatSymbols(Locale.US)
    private val COORDINATE_FORMAT = DecimalFormat("#.######", DECIMAL_FORMAT_SYMBOLS)
    private val ACCURACY_FORMAT = NumberFormat.getIntegerInstance(Locale.US)
    private val ALTITUDE_FORMAT = NumberFormat.getIntegerInstance(Locale.US)
    private val BEARING_FORMAT = NumberFormat.getIntegerInstance(Locale.US)
    private val SPEED_FORMAT = DecimalFormat("#.#", DECIMAL_FORMAT_SYMBOLS)

    init {
        DATE_FORMAT.timeZone = TimeZone.getTimeZone("UTC")
    }

    private fun formatTime(location: Location): String = DATE_FORMAT.format(location.time)
    private fun formatProvider(location: Location) = location.provider.name
    private fun formatLatitude(location: Location): String = COORDINATE_FORMAT.format(location.coordinates.latitude.value)
    private fun formatLongitude(location: Location): String = COORDINATE_FORMAT.format(location.coordinates.longitude.value)
    private fun formatAltitude(altitude: Distance): String = ALTITUDE_FORMAT.format(altitude.value)
    private fun formatAltitude(altitude: Altitude): String = formatAltitude(altitude.value)
    private fun formatAltitude(location: Location) = location.altitude?.let {
        formatAltitude(location.altitude)
    } ?: ""
    private fun formatAccuracy(accuracy: Distance): String = ACCURACY_FORMAT.format(accuracy.value)
    private fun formatAccuracy(location: Location) = location.coordinates.accuracy?.let {
        formatAccuracy(it)
    } ?: ""
    private fun formatBearing(bearing: Azimuth): String = BEARING_FORMAT.format(bearing.value)
    private fun formatBearing(bearing: Bearing) = formatBearing(bearing.value)
    private fun formatBearing(location: Location) = location.bearing?.let {
        formatBearing(it)
    } ?: ""
    private fun formatSpeed(speed: Velocity): String = SPEED_FORMAT.format(speed.value)
    private fun formatSpeed(speed: Speed) = formatSpeed(speed.value)
    private fun formatSpeed(location: Location) = location.speed?.let {
        formatSpeed(it)
    } ?: ""
    fun format(location: Location): String {
        val time = formatTime(location)
        val provider = formatProvider(location)
        val latitude = formatLatitude(location)
        val longitude = formatLongitude(location)
        val altitude = formatAltitude(location)
        val accuracy = formatAccuracy(location)
        val bearing = formatBearing(location)
        val speed = formatSpeed(location)
        return "$time,$provider,$latitude,$longitude,$altitude,$accuracy,$bearing,$speed"
    }
}

fun Location.toMinimalText() = LocationFormatter.format(this)
