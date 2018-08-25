package info.alkor.whereareyou.ui

import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.ViewModel
import info.alkor.whereareyou.model.location.Location
import info.alkor.whereareyou.model.location.toKilometersPerHour

class SingleLocationViewModel : ViewModel() {

    val provider = MutableLiveData<String>()
    val time = MutableLiveData<String>()
    val coords = MutableLiveData<String>()
    val altitude = MutableLiveData<String>()
    val bearing = MutableLiveData<String>()
    val speed = MutableLiveData<String>()

    init {
        update(null)
    }

    fun update(location: Location?) {
        if (location != null) {
            this.provider.postValue(location.provider.name)
            this.time.postValue(location.time.toString())
            this.coords.postValue("${location.coordinates}")
            this.altitude.postValue(if (location.altitude != null) "${location.altitude}" else "-")
            this.bearing.postValue(if (location.bearing != null) "${location.bearing}" else "-")
            this.speed.postValue(if (location.speed != null) "${location.speed.toKilometersPerHour()}" else "-")
        } else {
            this.provider.postValue("-")
            this.time.postValue("-")
            this.coords.postValue("-")
            this.altitude.postValue("-")
            this.bearing.postValue("-")
            this.speed.postValue("-")
        }
    }
}