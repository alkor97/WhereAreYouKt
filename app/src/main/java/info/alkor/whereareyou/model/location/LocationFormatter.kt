package info.alkor.whereareyou.model.location

import android.util.Log
import info.alkor.whereareyou.common.*
import java.text.*
import java.util.*

object LocationFormatter {

    private val DATE_FORMAT = SimpleDateFormat("yyyyMMddHHmmss", Locale.US)
    private val DECIMAL_FORMAT_SYMBOLS = DecimalFormatSymbols(Locale.US)
    private val COORDINATE_FORMAT = DecimalFormat("#.######", DECIMAL_FORMAT_SYMBOLS)
    private val ACCURACY_FORMAT = NumberFormat.getIntegerInstance(Locale.US)
    private val ALTITUDE_FORMAT = NumberFormat.getIntegerInstance(Locale.US)
    private val BEARING_FORMAT = NumberFormat.getIntegerInstance(Locale.US)
    private val SPEED_FORMAT = DecimalFormat("#.#", DECIMAL_FORMAT_SYMBOLS)

    private val loggingTag = "parse"

    init {
        DATE_FORMAT.timeZone = TimeZone.getTimeZone("UTC")
    }

    private fun formatTime(location: Location): String = DATE_FORMAT.format(location.time)
    private fun parseTime(text: String) = DATE_FORMAT.parse(text)
    private fun formatProvider(provider: Provider) = provider.name
    private fun parseProvider(text: String) = Provider.valueOf(text)
    private fun formatProvider(location: Location): String = formatProvider(location.provider)
    private fun formatLatitude(location: Location): String = COORDINATE_FORMAT.format(location.coordinates.latitude.value)
    private fun parseLatitude(text: String) = Latitude(COORDINATE_FORMAT.parse(text).toDouble())
    private fun formatLongitude(location: Location): String = COORDINATE_FORMAT.format(location.coordinates.longitude.value)
    private fun parseLongitude(text: String) = Longitude(COORDINATE_FORMAT.parse(text).toDouble())
    private fun formatDistance(distance: Distance, format: NumberFormat) = format.format(distance.value)
    private fun parseDistance(text: String, format: NumberFormat) = Distance(format.parse(text).toDouble())
    private fun formatAltitude(altitude: Altitude): String = formatDistance(altitude.value, ALTITUDE_FORMAT)
    private fun parseAltitude(text: String) = if (text.isEmpty()) null else Altitude(parseDistance(text, ALTITUDE_FORMAT))
    private fun formatAltitude(location: Location) = location.altitude?.let {
        formatAltitude(location.altitude)
    } ?: ""

    private fun formatAccuracy(location: Location) = location.coordinates.accuracy?.let {
        formatDistance(it, ACCURACY_FORMAT)
    } ?: ""

    private fun parseAccuracy(text: String) = if (text.isEmpty()) null else parseDistance(text, ACCURACY_FORMAT)
    private fun formatAzimuth(bearing: Azimuth, format: NumberFormat): String = format.format(bearing.value)
    private fun parseAzimuth(text: String, format: NumberFormat) = Azimuth(format.parse(text).toDouble())
    private fun formatBearing(bearing: Bearing) = formatAzimuth(bearing.value, BEARING_FORMAT)
    private fun parseBearing(text: String) = if (text.isEmpty()) null else Bearing(parseAzimuth(text, BEARING_FORMAT))
    private fun formatBearing(location: Location) = location.bearing?.let {
        formatBearing(it)
    } ?: ""

    private fun formatVelocity(speed: Velocity): String = SPEED_FORMAT.format(speed.value)
    private fun parseVelocity(text: String) = Velocity(SPEED_FORMAT.parse(text).toDouble())
    private fun formatSpeed(speed: Speed) = formatVelocity(speed.value)
    private fun parseSpeed(text: String) = if (text.isEmpty()) null else Speed(parseVelocity(text))
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

    fun parse(text: String): Location? {
        val split = text.split(",")
        if (split.size >= 4) {
            val time = try {
                parseTime(split[0])
            } catch (e: ParseException) {
                Log.e(loggingTag, "Error while parsing date ${split[0]}")
                return null
            }

            val provider = try {
                parseProvider(split[1])
            } catch (e: IllegalArgumentException) {
                Log.e(loggingTag, "Error while parsing provider ${split[1]}")
                return null
            }

            val latitude = try {
                parseLatitude(split[2])
            } catch (e: ParseException) {
                Log.e(loggingTag, "Error while parsing latitude ${split[2]}")
                return null
            }

            val longitude = try {
                parseLongitude(split[3])
            } catch (e: ParseException) {
                Log.e(loggingTag, "Error while parsing longitude ${split[3]}")
                return null
            }

            val altitude = if (split.size >= 5) try {
                parseAltitude(split[4])
            } catch (e: ParseException) {
                Log.e(loggingTag, "Error while parsing altitude ${split[4]}")
                return null
            } else null

            val accuracy = if (split.size >= 6) try {
                parseAccuracy(split[5])
            } catch (e: ParseException) {
                Log.e(loggingTag, "Error while parsing accuracy ${split[5]}")
                return null
            } else null

            val bearing = if (split.size >= 7) try {
                parseBearing(split[6])
            } catch (e: ParseException) {
                Log.e(loggingTag, "Error while parsing bearing ${split[6]}")
                return null
            } else null

            val speed = if (split.size >= 8) try {
                parseSpeed(split[7])
            } catch (e: ParseException) {
                Log.e(loggingTag, "Error while parsing speed ${split[7]}")
                return null
            } else null

            return Location(provider, time, Coordinates(latitude, longitude, accuracy), altitude, bearing, speed)
        }
        Log.e(loggingTag, "Could not parse $text to location")
        return null
    }
}
