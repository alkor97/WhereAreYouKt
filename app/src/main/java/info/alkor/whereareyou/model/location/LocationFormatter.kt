package info.alkor.whereareyou.model.location

import android.util.Log
import info.alkor.whereareyou.common.*
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*

object LocationFormatter {

    private val DATE_FORMAT = SimpleDateFormat("yyyyMMddHHmmss", Locale.US)
    private val DECIMAL_FORMAT_SYMBOLS = DecimalFormatSymbols(Locale.US)
    private val COORDINATE_FORMAT = DecimalFormat("#.######", DECIMAL_FORMAT_SYMBOLS)
    private val BEARING_FORMAT = NumberFormat.getIntegerInstance(Locale.US)
    private val SPEED_FORMAT = DecimalFormat("#.#", DECIMAL_FORMAT_SYMBOLS)
    private val DISTANCE_FORMAT = NumberFormat.getIntegerInstance(Locale.US)

    private val loggingTag = "parse"

    init {
        DATE_FORMAT.timeZone = TimeZone.getTimeZone("UTC")
    }

    enum class Field {
        TIME, PROVIDER,
        LATITUDE, LONGITUDE, ACCURACY,
        ALTITUDE, ALTITUDE_ACCURACY,
        BEARING, BEARING_ACCURACY,
        SPEED, SPEED_ACCURACY
    }

    private class ParserException(text: String, e: Throwable) : Exception(text, e)

    private abstract class AbstractHandler<E>(private val field: Field) {
        abstract fun format(value: E): String

        fun parse(context: Context): E = try {
            doParse(context[field])
        } catch (e: Exception) {
            throw ParserException("Failed to parse ${field.name.toLowerCase(Locale.US)} from `${context[field]}`", e)
        }

        protected abstract fun doParse(text: String?): E
    }

    private object DateHandler : AbstractHandler<Date>(Field.TIME) {
        override fun format(value: Date): String = DATE_FORMAT.format(value)
        override fun doParse(text: String?): Date = DATE_FORMAT.parse(text)
    }

    private object ProviderHandler : AbstractHandler<Provider>(Field.PROVIDER) {
        override fun format(value: Provider): String = value.name.toLowerCase(Locale.US)
        override fun doParse(text: String?): Provider = Provider.valueOf(text?.toUpperCase(Locale.US)
                ?: "")
    }

    private object LatitudeHandler : AbstractHandler<Latitude>(Field.LATITUDE) {
        override fun format(value: Latitude): String = COORDINATE_FORMAT.format(value.value)
        override fun doParse(text: String?): Latitude = Latitude(COORDINATE_FORMAT.parse(text).toDouble())
    }

    private object LongitudeHandler : AbstractHandler<Longitude>(Field.LONGITUDE) {
        override fun format(value: Longitude): String = COORDINATE_FORMAT.format(value.value)
        override fun doParse(text: String?): Longitude = Longitude(COORDINATE_FORMAT.parse(text).toDouble())
    }

    private class DistanceHandler(field: Field) : AbstractHandler<Distance?>(field) {
        override fun format(value: Distance?): String = if (value != null) DISTANCE_FORMAT.format(value.value) else ""
        override fun doParse(text: String?): Distance? = if (!text.isNullOrEmpty())
            Distance(DISTANCE_FORMAT.parse(text).toDouble())
        else
            null
    }

    private class AzimuthHandler(field: Field) : AbstractHandler<Azimuth?>(field) {
        override fun format(value: Azimuth?): String = if (value != null) BEARING_FORMAT.format(value.value) else ""
        override fun doParse(text: String?): Azimuth? = if (!text.isNullOrEmpty())
            Azimuth(BEARING_FORMAT.parse(text).toDouble())
        else
            null
    }

    private class VelocityHandler(field: Field) : AbstractHandler<Velocity?>(field) {
        override fun format(value: Velocity?): String = if (value != null) SPEED_FORMAT.format(value.value) else ""
        override fun doParse(text: String?): Velocity? = if (!text.isNullOrEmpty())
            Velocity(SPEED_FORMAT.parse(text).toDouble())
        else
            null
    }

    private class Context constructor(text: String) {
        constructor() : this("")

        private val order = Array(Field.values().size) { "" }

        init {
            val split = text.split(",")
            Field.values().forEach {
                order[it.ordinal] = split.elementAtOrElse(it.ordinal) { "" }
            }
        }

        operator fun get(field: Field): String = order[field.ordinal]
        operator fun set(field: Field, value: String) {
            order[field.ordinal] = value
        }

        fun join(separator: String = ",") = order.joinToString(separator)
    }

    fun format(location: Location): String {
        val context = Context()
        context[Field.TIME] = DateHandler.format(location.time)
        context[Field.PROVIDER] = ProviderHandler.format(location.provider)
        context[Field.LATITUDE] = LatitudeHandler.format(location.coordinates.latitude)
        context[Field.LONGITUDE] = LongitudeHandler.format(location.coordinates.longitude)
        context[Field.ACCURACY] = DistanceHandler(Field.ACCURACY).format(location.coordinates.accuracy)
        context[Field.ALTITUDE] = DistanceHandler(Field.ALTITUDE).format(location.altitude?.value)
        context[Field.ALTITUDE_ACCURACY] = DistanceHandler(Field.ALTITUDE_ACCURACY).format(location.altitude?.accuracy)
        context[Field.BEARING] = AzimuthHandler(Field.BEARING).format(location.bearing?.value)
        context[Field.BEARING_ACCURACY] = AzimuthHandler(Field.BEARING_ACCURACY).format(location.bearing?.accuracy)
        context[Field.SPEED] = VelocityHandler(Field.SPEED).format(location.speed?.value)
        context[Field.SPEED_ACCURACY] = VelocityHandler(Field.SPEED_ACCURACY).format(location.speed?.accuracy)
        return context.join()
    }

    fun parse(text: String): Location? {
        val context = Context(text)
        return try {
            Location(
                    provider = ProviderHandler.parse(context),
                    time = DateHandler.parse(context),
                    coordinates = Coordinates(
                            latitude = LatitudeHandler.parse(context),
                            longitude = LongitudeHandler.parse(context),
                            accuracy = DistanceHandler(Field.ACCURACY).parse(context)
                    ),
                    altitude = if (context[Field.ALTITUDE].isNotEmpty())
                        Altitude(DistanceHandler(Field.ALTITUDE).parse(context)!!, DistanceHandler(Field.ALTITUDE_ACCURACY).parse(context))
                    else null,
                    bearing = if (context[Field.BEARING].isNotEmpty())
                        Bearing(AzimuthHandler(Field.BEARING).parse(context)!!, AzimuthHandler(Field.BEARING_ACCURACY).parse(context))
                    else null,
                    speed = if (context[Field.SPEED].isNotEmpty())
                        Speed(VelocityHandler(Field.SPEED).parse(context)!!, VelocityHandler(Field.SPEED_ACCURACY).parse(context))
                    else null
            )
        } catch (e: ParserException) {
            Log.e(loggingTag, e.localizedMessage)
            null
        }
    }
}
