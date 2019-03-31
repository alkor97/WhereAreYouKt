package info.alkor.whereareyou.impl.location

import android.util.Log
import info.alkor.whereareyou.api.location.LocationProvider
import info.alkor.whereareyou.common.Duration
import info.alkor.whereareyou.model.location.Location
import info.alkor.whereareyou.model.location.Provider
import kotlinx.coroutines.experimental.*
import java.util.*
import kotlin.coroutines.experimental.CoroutineContext

abstract class AbstractLocationProvider(
        private val providers: Array<Provider> = Provider.values(),
        private val coroutineContext: CoroutineContext = DefaultDispatcher
) : LocationProvider {

    private val loggingTag = "locating"

    override fun getLocation(timeout: Duration, maxAge: Duration, callback: (location: Location?, final: Boolean) -> Unit) {
        val totalTimeout = maxAge + timeout
        val deferred = providers.map {
            async(coroutineContext) {
                Log.i(loggingTag, "$it queried")
                val location = requestLocation(it)
                if (location != null && location.time.notOlderThan(totalTimeout)) {
                    Log.i(loggingTag, "$it responded with $location")
                    callback(location, false)
                }
                location
            }
        }
        launch {
            withTimeoutOrNull(timeout.value, timeout.unit) {
                Log.i(loggingTag, "awaiting location...")
                deferred.awaitAll()
            }
            val location = deferred.asSequence().filter { it.isCompleted }
                    .mapNotNull { it.getCompleted() }
                    .filter { it.time.notOlderThan(totalTimeout) }
                    .sortedWith(compareBy<Location, Double?>(nullsLast()) { it.coordinates.accuracy?.value })
                    .firstOrNull()
            Log.i(loggingTag, "got final location $location")
            callback(location, true)
        }
    }

    protected abstract suspend fun requestLocation(provider: Provider): Location?
}

fun Date.notOlderThan(duration: Duration) = Date().time - this.time <= duration.toMillis()
