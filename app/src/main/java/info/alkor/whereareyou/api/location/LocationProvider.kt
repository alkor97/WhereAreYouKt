package info.alkor.whereareyou.api.location

import info.alkor.whereareyou.common.Duration
import info.alkor.whereareyou.model.location.Location

interface LocationProvider {
    fun getLocation(timeout: Duration, callback: (location: Location?, final: Boolean) -> Unit)
}