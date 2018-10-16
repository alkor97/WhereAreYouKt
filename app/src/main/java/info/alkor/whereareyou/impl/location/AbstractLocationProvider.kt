package info.alkor.whereareyou.impl.location

import info.alkor.whereareyou.api.location.LocationProvider
import info.alkor.whereareyou.common.Duration
import info.alkor.whereareyou.common.duration
import info.alkor.whereareyou.model.location.Location
import info.alkor.whereareyou.model.location.Provider
import kotlinx.coroutines.experimental.*
import java.util.*
import kotlin.coroutines.experimental.CoroutineContext

abstract class AbstractLocationProvider(
        private val providers: Array<Provider> = Provider.values(),
        private val coroutineContext: CoroutineContext = DefaultDispatcher,
        private val maxLocationAge: Duration = duration(minutes = 1)
) : LocationProvider {

    override fun getLocation(timeout: Duration, callback: (location: Location?, final: Boolean) -> Unit) {
        val deferred = providers.map {
            async(coroutineContext) {
                val location = requestLocation(it)
                callback(location, false)
                location
            }
        }
        launch {
            withTimeoutOrNull(timeout.value, timeout.unit) {
                deferred.awaitAll()
            }
            val totalTimeout = maxLocationAge + timeout
            val location = deferred.filter { it.isCompleted }
                    .mapNotNull { it.getCompleted() }
                    .filter { it.time.notOlderThan(totalTimeout) }
                    .sortedWith(compareBy<Location, Double?>(nullsLast()) { it.coordinates.accuracy?.value })
                    .firstOrNull()
            callback(location, true)
        }
    }

    protected abstract suspend fun requestLocation(provider: Provider): Location?
}

fun Date.notOlderThan(duration: Duration) = Date().time - this.time <= duration.toMillis()
