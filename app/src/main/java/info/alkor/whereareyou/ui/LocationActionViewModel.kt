package info.alkor.whereareyou.ui

import android.content.Context
import android.text.format.DateUtils
import android.util.Log
import android.view.View
import info.alkor.whereareyou.model.action.*
import info.alkor.whereareyou.model.location.Bearing
import info.alkor.whereareyou.model.location.Location
import info.alkor.whereareyou.model.location.Speed
import info.alkor.whereareyou.model.location.toKilometersPerHour
import info.alkor.whereareyoukt.R
import java.util.*

class LocationActionViewModel(private val context: Context) {

    var timeStamp = ""
    var query = ""
    var name = ""
    var phone = ""
    var status = ""
    var coordinates = ""
    var altitude = ""
    var speed = ""

    var menuVisible = View.INVISIBLE
    var personVisible = View.GONE

    var inProgressVisible = View.GONE
    var succeededVisible = View.GONE
    var failedVisible = View.GONE

    var coordinatesVisible = View.GONE
    var altitudeVisible = View.GONE
    var speedVisible = View.GONE

    var currentProgress: Int = 0
    var maxProgress: Int = 0
    val indeterminateProgress: Boolean
        get() = maxProgress == 0

    private val resources = context.resources

    private fun postTimeStamp(date: Date) {
        timeStamp = formatTimeStamp(date)
    }

    private fun postPerson(person: Person) {
        name = person.name ?: ""
        phone = formatPhone(person.phone)
        personVisible = View.VISIBLE
    }

    private fun postHeader(action: LocationAction) {
        if (action.person.phone != PhoneNumber.OWN) {
            postPerson(action.person)
            query = formatQuery(action.direction == Direction.INCOMING)
        } else {
            query = resources.getString(R.string.your_location_is)
            personVisible = View.GONE
        }
    }

    fun render(action: LocationAction) {
        postTimeStamp(action.location?.time ?: Date())
        postHeader(action)
        postStatus(action)

        if (action.location != null) {
            menuVisible = View.VISIBLE
            coordinatesVisible = View.VISIBLE
            coordinates = formatCoordinates(action.location)

            if (action.location.altitude != null) {
                altitude = formatAltitude(action.location)
                altitudeVisible = View.VISIBLE
            } else {
                altitudeVisible = View.GONE
            }

            if (action.location.speed != null) {
                if (action.location.speed.value.value > 0.0) {
                    if (action.location.bearing != null) {
                        speed = formatSpeedAndBearing(action.location.speed, action.location.bearing)
                        speedVisible = View.VISIBLE
                    } else {
                        speedVisible = View.GONE
                    }
                } else {
                    speedVisible = View.GONE
                }
            } else {
                speedVisible = View.GONE
            }

            if (!action.final) {
                maxProgress = 100
                currentProgress = ((action.progress ?: 0.0f) * maxProgress).toInt()
            } else {
                currentProgress = 0
                maxProgress = 0
            }
        } else {
            coordinatesVisible = View.VISIBLE
            coordinates = resources.getString(R.string.location_unknown)
            maxProgress = 0
        }
        Log.i("render", "$action")
    }

    private fun postStatus(action: LocationAction) {
        if (action.status == SendingStatus.PENDING) {
            status = resources.getString(R.string.status_locating)
            if (!action.final) {
                postInProgress()
            } else if (action.location != null) {
                postSucceeded()
            } else {
                postFailed()
            }
        } else {
            postStatus(action.status)
        }
    }

    private fun postStatus(eventStatus: SendingStatus) = when (eventStatus) {
        SendingStatus.PENDING -> {
            status = resources.getString(R.string.status_sending)
            postInProgress()
        }
        SendingStatus.SENDING_FAILED -> {
            status = resources.getString(R.string.status_sending)
            postFailed()
        }
        SendingStatus.SENT -> {
            postSucceeded()
            status = resources.getString(R.string.status_delivery)
            postInProgress()
        }
        SendingStatus.DELIVERY_FAILED -> {
            status = resources.getString(R.string.status_delivery)
            postFailed()
        }
        SendingStatus.DELIVERED -> {
            status = resources.getString(R.string.status_delivery)
            postSucceeded()
        }
    }

    private fun formatPhone(phone: PhoneNumber) = "✆ ${phone.toHumanReadable()}"

    private fun formatCoordinates(location: Location) = location.coordinates.toString()

    private fun formatAltitude(location: Location) = resources.getString(
            R.string.above_sea_level,
            location.altitude.toString())

    private fun formatSpeedAndBearing(speed: Speed, bearing: Bearing) = resources.getString(
            R.string.speed_and_bearing,
            speed.toKilometersPerHour(), formatDirection(bearing), formatBearing(bearing))

    private fun formatBearing(bearing: Bearing) = bearing.toString()

    private fun formatDirection(bearing: Bearing): String {
        val normalized = (bearing.value.value - 22.5) / 45
        return DIRECTIONS[Math.ceil(normalized).toInt() % DIRECTIONS.size]
    }

    private fun postInProgress() {
        inProgressVisible = View.VISIBLE
        succeededVisible = View.GONE
        failedVisible = View.GONE
    }

    private fun postSucceeded() {
        inProgressVisible = View.GONE
        succeededVisible = View.VISIBLE
        failedVisible = View.GONE
    }

    private fun postFailed() {
        inProgressVisible = View.GONE
        succeededVisible = View.GONE
        failedVisible = View.VISIBLE
    }

    private fun formatQuery(myLocation: Boolean) = resources.getString(
            if (myLocation)
                R.string.query_my_location
            else
                R.string.query_someones_location)

    private fun formatTimeStamp(time: Date) = DateUtils.formatDateTime(context, time.time,
            DateUtils.FORMAT_SHOW_DATE
                    or DateUtils.FORMAT_SHOW_TIME
                    or DateUtils.FORMAT_SHOW_YEAR
                    or DateUtils.FORMAT_SHOW_WEEKDAY)

    companion object {
        val DIRECTIONS = arrayOf("N↑", "NE↗", "E→", "SE↘", "S↓", "SW↙", "W←", "NW↖")
    }
}