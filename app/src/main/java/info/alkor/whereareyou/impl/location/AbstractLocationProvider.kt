package info.alkor.whereareyou.impl.location

import info.alkor.whereareyou.api.location.LocationProvider
import info.alkor.whereareyou.common.Duration
import info.alkor.whereareyou.model.location.Location
import info.alkor.whereareyou.model.location.Provider
import kotlinx.coroutines.*
import java.util.*
import kotlin.coroutines.CoroutineContext

abstract class AbstractLocationProvider(
        private val providers: Array<Provider> = Provider.values(),
        private val coroutineContext: CoroutineContext = Dispatchers.Default
) : LocationProvider {

    override fun getLocation(timeout: Duration, maxAge: Duration, callback: (location: Location?, final: Boolean) -> Unit) {
        val totalTimeout = maxAge + timeout
        val deferred = providers.map {
            GlobalScope.async(coroutineContext) {
                val location = requestLocation(it)
                if (location != null && location.time.notOlderThan(totalTimeout)) {
                    callback(location, false)
                }
                location
            }
        }
        GlobalScope.launch {
            withTimeoutOrNull(timeout.toMillis()) {
                deferred.awaitAll()
            }
            val location = deferred.asSequence().filter { it.isCompleted }
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
