package info.alkor.whereareyou.impl.persistence

import androidx.room.TypeConverter
import info.alkor.whereareyou.common.*
import info.alkor.whereareyou.model.action.*
import info.alkor.whereareyou.model.location.*
import java.util.*

fun Person.toRecord() = PersonRecord(
        phone = phone.value,
        name = name
)

fun PersonRecord.toModel() = Person(
        phone = PhoneNumber(phone),
        name = name
)

fun Coordinates.toRecord() = CoordinatesRecord(
        latitude = latitude.value,
        longitude = longitude.value,
        accuracy = accuracy?.value
)

fun CoordinatesRecord.toModel() = Coordinates(
        latitude = Latitude(latitude),
        longitude = Longitude(longitude),
        accuracy = if (accuracy != null) meters(accuracy) else null
)

fun <E : WithDouble> AccurateValue<E>.toRecord() = AccurateValueRecord(
        value = value.value,
        accuracy = accuracy?.value
)

fun AccurateValueRecord.toAltitude() = Altitude(
        value = Distance(value),
        accuracy = if (accuracy != null) meters(accuracy) else null
)

fun AccurateValueRecord.toBearing() = Bearing(
        value = Azimuth(value),
        accuracy = if (accuracy != null) azimuthDegrees(accuracy) else null
)

fun AccurateValueRecord.toSpeed() = Speed(
        value = Velocity(value),
        accuracy = if (accuracy != null) metersPerSecond(accuracy) else null
)

fun Location.toRecord() = LocationRecord(
        provider = provider,
        time = time,
        coordinates = coordinates.toRecord(),
        altitude = altitude?.toRecord(),
        bearing = bearing?.toRecord(),
        speed = speed?.toRecord()
)

fun LocationRecord.toModel() = Location(
        provider = provider,
        time = time,
        coordinates = coordinates.toModel(),
        altitude = altitude?.toAltitude(),
        bearing = bearing?.toBearing(),
        speed = speed?.toSpeed()
)

fun LocationAction.toRecord() = LocationActionRecord(
        id = null,
        direction = direction,
        phone = person.phone.toExternalForm(),
        name = person.name,
        location = location?.toRecord(),
        isFinal = final,
        status = status,
        progress = progress
)

fun LocationActionRecord.toModel() = LocationAction(
        id = id!!,
        direction = direction,
        person = Person(PhoneNumber(phone), name),
        location = location?.toModel(),
        final = isFinal,
        status = status,
        progress = progress
)

class Converters {
    companion object {
        @TypeConverter
        @JvmStatic
        fun directionToString(value: Direction) = value.name

        @TypeConverter
        @JvmStatic
        fun stringToDirection(value: String) = Direction.valueOf(value)

        @TypeConverter
        @JvmStatic
        fun sendingStatusToString(value: SendingStatus) = value.name

        @TypeConverter
        @JvmStatic
        fun stringToSendingStatus(value: String) = SendingStatus.valueOf(value)

        @TypeConverter
        @JvmStatic
        fun providerToString(value: Provider) = value.name

        @TypeConverter
        @JvmStatic
        fun stringToProvider(value: String) = Provider.valueOf(value)

        @TypeConverter
        @JvmStatic
        fun dateToLong(value: Date) = value.time

        @TypeConverter
        @JvmStatic
        fun longToDate(value: Long) = Date(value)
    }
}
