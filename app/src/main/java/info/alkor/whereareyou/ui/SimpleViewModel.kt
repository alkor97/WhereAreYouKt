package info.alkor.whereareyou.ui

import android.databinding.BaseObservable
import android.databinding.Bindable
import com.android.databinding.library.baseAdapters.BR
import info.alkor.whereareyou.model.location.Location
import info.alkor.whereareyou.model.location.toKilometersPerHour

class SimpleViewModel(
        provider: String = "-",
        time: String = "-",
        coords: String = "-",
        altitude: String = "-",
        bearing: String = "-",
        speed: String = "-"
) : BaseObservable() {

    fun update(location: Location?) {
        if (location != null) {
            this.provider = location.provider.name
            this.time = location.time.toString()
            this.coords = "${location.coordinates}"
            this.altitude = if (location.altitude != null) "${location.altitude}" else "-"
            this.bearing = if (location.bearing != null) "${location.bearing}" else "-"
            this.speed = if (location.speed != null) "${location.speed.toKilometersPerHour()}" else "-"
        } else {
            this.provider = "-"
            this.time = "-"
            this.coords = "-"
            this.altitude = "-"
            this.bearing = "-"
            this.speed = "-"
        }
    }

    @Bindable
    var provider: String = provider
        set(value) {
            if (field != value) {
                field = value
                notifyPropertyChanged(BR.provider)
            }
        }

    @Bindable
    var time: String = time
        set(value) {
            if (field != value) {
                field = value
                notifyPropertyChanged(BR.time)
            }
        }

    @Bindable
    var coords: String = coords
        set(value) {
            if (field != value) {
                field = value
                notifyPropertyChanged(BR.coords)
            }
        }

    @Bindable
    var altitude: String = altitude
        set(value) {
            if (field != value) {
                field = value
                notifyPropertyChanged(BR.altitude)
            }
        }

    @Bindable
    var bearing: String = bearing
        set(value) {
            if (field != value) {
                field = value
                notifyPropertyChanged(BR.bearing)
            }
        }

    @Bindable
    var speed: String = speed
        set(value) {
            if (field != value) {
                field = value
                notifyPropertyChanged(BR.speed)
            }
        }
}