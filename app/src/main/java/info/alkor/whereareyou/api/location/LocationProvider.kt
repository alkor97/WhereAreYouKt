package info.alkor.whereareyou.api.location

import info.alkor.whereareyou.common.Duration
import info.alkor.whereareyou.model.location.Location
import kotlinx.coroutines.experimental.channels.Channel

interface LocationProvider {
    suspend fun getLocation(timeout: Duration, locationsChannel: Channel<Location>? = null): Location?
}