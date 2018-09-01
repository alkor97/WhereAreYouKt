package info.alkor.whereareyou.ui

import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.ViewModel
import android.content.Intent
import android.net.Uri
import android.view.View
import info.alkor.whereareyou.common.DateTimeFormatter
import info.alkor.whereareyou.model.location.Location
import info.alkor.whereareyou.model.location.toKilometersPerHour

class SingleLocationViewModel : ViewModel() {

    val provider = MutableLiveData<String>()
    val time = MutableLiveData<String>()
    val coords = MutableLiveData<String>()
    val altitude = MutableLiveData<String>()
    val bearing = MutableLiveData<String>()
    val speed = MutableLiveData<String>()

    val altitudeVisible = MutableLiveData<Int>()
    val bearingVisible = MutableLiveData<Int>()
    val speedVisible = MutableLiveData<Int>()

    val showVisible = MutableLiveData<Int>()

    var link: String? = null

    init {
        update(null, null)
    }

    fun update(location: Location?, link: String?) {
        this.link = link
        if (location != null) {
            provider.postValue(location.provider.name)
            time.postValue(formatter.formatTime(location.time))
            coords.postValue("${location.coordinates}")

            setValue(altitude, altitudeVisible, location.altitude)
            setValue(bearing, bearingVisible, location.bearing)
            setValue(speed, speedVisible, location.speed) {
                "${it.toKilometersPerHour()}"
            }
            showVisible.postValue(View.VISIBLE)
        } else {
            provider.postValue("-")
            time.postValue("-")
            coords.postValue("-")
            setValue(altitude, altitudeVisible, null)
            setValue(bearing, bearingVisible, null)
            setValue(speed, speedVisible, null)
            showVisible.postValue(View.GONE)
        }
    }

    private fun <T> setValue(target: MutableLiveData<String>, targetVisibility: MutableLiveData<Int>, value: T?, toString: (v: T) -> String = fun(v: T) = "$v") {
        if (value != null) {
            target.postValue(toString(value))
            targetVisibility.postValue(View.VISIBLE)
        } else {
            target.postValue("-")
            targetVisibility.postValue(View.GONE)
        }
    }

    companion object {
        val formatter = DateTimeFormatter()
    }
}
