package info.alkor.whereareyou.ui

import android.app.Application
import android.arch.lifecycle.AndroidViewModel
import android.arch.lifecycle.MutableLiveData
import android.text.format.DateUtils
import android.view.View
import info.alkor.whereareyou.api.persistence.*
import info.alkor.whereareyou.model.action.PhoneNumber
import info.alkor.whereareyou.model.action.SendingStatus
import info.alkor.whereareyou.model.location.Bearing
import info.alkor.whereareyou.model.location.Location
import info.alkor.whereareyou.model.location.Speed
import info.alkor.whereareyou.model.location.toKilometersPerHour
import info.alkor.whereareyoukt.R
import java.util.*

class RequestViewModel(application: Application) : AndroidViewModel(application) {

    val timeStamp = MutableLiveData<String>()
    val query = MutableLiveData<String>()
    val name = MutableLiveData<String>()
    val phone = MutableLiveData<String>()
    val status = MutableLiveData<String>()
    val coordinates = MutableLiveData<String>()
    val altitude = MutableLiveData<String>()
    val speed = MutableLiveData<String>()

    val menuVisible = MutableLiveData<Int>()
    val personVisible = MutableLiveData<Int>()

    val inProgressVisible = MutableLiveData<Int>()
    val succeededVisible = MutableLiveData<Int>()
    val failedVisible = MutableLiveData<Int>()

    val coordinatesVisible = MutableLiveData<Int>()
    val altitudeVisible = MutableLiveData<Int>()
    val speedVisible = MutableLiveData<Int>()

    private val resources = getApplication<Application>().resources

    init {
        inProgressVisible.postValue(View.GONE)
        succeededVisible.postValue(View.GONE)
        failedVisible.postValue(View.GONE)
        coordinatesVisible.postValue(View.GONE)
        altitudeVisible.postValue(View.GONE)
        speedVisible.postValue(View.GONE)
        menuVisible.postValue(View.GONE)
        personVisible.postValue(View.GONE)
    }

    fun handleEvent(event: LocationRequestEvent) = when (event) {
        is LocationRequested -> {
            timeStamp.postValue(formatTimeStamp(Date())) // TODO: this should depend on event data
            if (event.person.phone != PhoneNumber.OWN) {
                name.postValue(event.person.name)
                phone.postValue(formatPhone(event.person.phone))
                personVisible.postValue(View.VISIBLE)
                query.postValue(formatQuery(true)) // TODO: this should depend on event data
            } else {
                query.postValue(resources.getString(R.string.your_location_is))
                personVisible.postValue(View.GONE)
            }
            status.postValue(resources.getString(R.string.status_locating))
            postInProgress()
        }
        is NoLocation -> {
            postFailed()
            menuVisible.postValue(View.GONE)
            coordinatesVisible.postValue(View.GONE)
            altitudeVisible.postValue(View.GONE)
            speedVisible.postValue(View.GONE)
        }
        is FinalLocation -> {
            postSucceeded()
        }
        is WithLocation -> {
            postInProgress()
            menuVisible.postValue(View.VISIBLE)
            coordinatesVisible.postValue(View.VISIBLE)
            coordinates.postValue(formatCoordinates(event.location))

            event.location.altitude?.let {
                altitude.postValue(formatAltitude(event.location))
                altitudeVisible.postValue(View.VISIBLE)
            } ?: altitudeVisible.postValue(View.GONE)

            event.location.speed?.let { eventSpeed ->
                if (eventSpeed.value.value > 0.0) {
                    event.location.bearing?.let { eventBearing ->
                        speed.postValue(formatSpeedAndBearing(eventSpeed, eventBearing))
                        speedVisible.postValue(View.VISIBLE)
                    } ?: speedVisible.postValue(View.GONE)
                } else speedVisible.postValue(View.GONE)
            } ?: speedVisible.postValue(View.GONE)
        }
        is SendingStatusUpdated -> postStatus(event.status)
        else -> Unit
    }

    private fun postStatus(eventStatus: SendingStatus) = when (eventStatus) {
        SendingStatus.PENDING -> {
            status.postValue(resources.getString(R.string.status_sending))
            postInProgress()
        }
        SendingStatus.SENDING_FAILED -> {
            status.postValue(resources.getString(R.string.status_sending))
            postFailed()
        }
        SendingStatus.SENT -> {
            postSucceeded()
            status.postValue(resources.getString(R.string.status_delivery))
            postInProgress()
        }
        SendingStatus.DELIVERY_FAILED -> {
            status.postValue(resources.getString(R.string.status_delivery))
            postFailed()
        }
        SendingStatus.DELIVERED -> {
            status.postValue(resources.getString(R.string.status_delivery))
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
        inProgressVisible.postValue(View.VISIBLE)
        succeededVisible.postValue(View.GONE)
        failedVisible.postValue(View.GONE)
    }

    private fun postSucceeded() {
        inProgressVisible.postValue(View.GONE)
        succeededVisible.postValue(View.VISIBLE)
        failedVisible.postValue(View.GONE)
    }

    private fun postFailed() {
        inProgressVisible.postValue(View.GONE)
        succeededVisible.postValue(View.GONE)
        failedVisible.postValue(View.VISIBLE)
    }

    private fun formatQuery(myLocation: Boolean) = resources.getString(
            if (myLocation)
                R.string.query_my_location
            else
                R.string.query_someones_location)

    private fun context() = getApplication<Application>().applicationContext

    private fun formatTimeStamp(time: Date) = DateUtils.formatDateTime(context(), time.time,
            DateUtils.FORMAT_SHOW_DATE
                    or DateUtils.FORMAT_SHOW_TIME
                    or DateUtils.FORMAT_SHOW_YEAR
                    or DateUtils.FORMAT_SHOW_WEEKDAY)

    companion object {
        val DIRECTIONS = arrayOf("↑N", "↗NE", "→E", "↘SE", "↓S", "↙SW", "←W", "↖NW")
    }
}
