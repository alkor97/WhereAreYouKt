package info.alkor.whereareyou.ui

import android.content.Context
import android.text.format.DateUtils
import android.util.Log
import android.view.View
import info.alkor.whereareyou.model.action.*
import info.alkor.whereareyou.model.location.*
import info.alkor.whereareyoukt.R
import java.util.*

class LocationActionViewModel {

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

    class Builder(private val context: Context) {
        private val resources = context.resources

        fun build(inputModel: LocationActionViewModel?, action: LocationAction): LocationActionViewModel {
            val model = inputModel ?: LocationActionViewModel()
            model.timeStamp = formatTimeStamp(action.location?.time ?: Date())
            postHeader(model, action)
            postStatus(model, action)

            if (action.location != null) {
                model.menuVisible = View.VISIBLE
                model.coordinatesVisible = View.VISIBLE
                model.coordinates = formatCoordinates(action.location)

                if (action.location.altitude != null) {
                    model.altitude = formatAltitude(action.location)
                    model.altitudeVisible = View.VISIBLE
                } else {
                    model.altitudeVisible = View.GONE
                }

                if (action.location.speed != null) {
                    if (action.location.speed.value.value > 0.0) {
                        if (action.location.bearing != null) {
                            model.speed = formatSpeedAndBearing(action.location.speed, action.location.bearing)
                            model.speedVisible = View.VISIBLE
                        } else {
                            model.speedVisible = View.GONE
                        }
                    } else {
                        model.speedVisible = View.GONE
                    }
                } else {
                    model.speedVisible = View.GONE
                }

                if (!action.final) {
                    model.maxProgress = 100
                    model.currentProgress = ((action.progress ?: 0.0f) * model.maxProgress).toInt()
                } else {
                    model.currentProgress = 0
                    model.maxProgress = 0
                }
            } else {
                model.coordinatesVisible = View.VISIBLE
                model.coordinates = resources.getString(R.string.location_unknown)
                model.maxProgress = 0
            }
            Log.i("render", "$action")
            return model
        }

        private fun postStatus(model: LocationActionViewModel, action: LocationAction) {
            if (action.status == SendingStatus.PENDING) {
                model.status = resources.getString(R.string.status_locating)
                if (!action.final) {
                    postInProgress(model)
                } else if (action.location != null) {
                    postSucceeded(model)
                } else {
                    postFailed(model)
                }
            } else {
                postStatus(model, action.status)
            }
        }

        private fun postStatus(model: LocationActionViewModel, eventStatus: SendingStatus) = when (eventStatus) {
            SendingStatus.PENDING -> {
                model.status = resources.getString(R.string.status_sending)
                postInProgress(model)
            }
            SendingStatus.SENDING_FAILED -> {
                model.status = resources.getString(R.string.status_sending)
                postFailed(model)
            }
            SendingStatus.SENT -> {
                postSucceeded(model)
                model.status = resources.getString(R.string.status_delivery)
                postInProgress(model)
            }
            SendingStatus.DELIVERY_FAILED -> {
                model.status = resources.getString(R.string.status_delivery)
                postFailed(model)
            }
            SendingStatus.DELIVERED -> {
                model.status = resources.getString(R.string.status_delivery)
                postSucceeded(model)
            }
        }

        private fun postInProgress(model: LocationActionViewModel) {
            model.inProgressVisible = View.VISIBLE
            model.succeededVisible = View.GONE
            model.failedVisible = View.GONE
        }

        private fun postSucceeded(model: LocationActionViewModel) {
            model.inProgressVisible = View.GONE
            model.succeededVisible = View.VISIBLE
            model.failedVisible = View.GONE
        }

        private fun postFailed(model: LocationActionViewModel) {
            model.inProgressVisible = View.GONE
            model.succeededVisible = View.GONE
            model.failedVisible = View.VISIBLE
        }

        private fun postPerson(model: LocationActionViewModel, person: Person) {
            model.name = person.name ?: ""
            model.phone = formatPhone(person.phone)
            model.personVisible = View.VISIBLE
        }

        private fun postHeader(model: LocationActionViewModel, action: LocationAction) {
            if (action.person.phone != PhoneNumber.OWN) {
                postPerson(model, action.person)
                model.query = formatQuery(action.direction == Direction.INCOMING)
            } else {
                model.query = resources.getString(R.string.your_location_is)
                model.personVisible = View.GONE
            }
        }

        private fun formatPhone(phone: PhoneNumber) = "✆ ${phone.toHumanReadable()}"

        private fun formatCoordinates(location: Location) = location.coordinates.toString() + " (" + formatProvider(location) + ")"

        private fun formatProvider(location: Location) = when (location.provider) {
            Provider.GPS -> context.getString(R.string.provider_gps)
            Provider.NETWORK -> context.getString(R.string.provider_network)
        }

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
}