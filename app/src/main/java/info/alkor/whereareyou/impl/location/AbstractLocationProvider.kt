package info.alkor.whereareyou.impl.location

import android.util.Log
import info.alkor.whereareyou.common.Duration
import info.alkor.whereareyou.model.location.Location
import info.alkor.whereareyou.model.location.Provider
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import java.util.*
import kotlin.coroutines.CoroutineContext

data class LocationFound(val location: Location?, val final: Boolean)

abstract class AbstractLocationProvider(
        private val providers: Array<Provider> = Provider.values(),
        protected val locationCoroutineContext: CoroutineContext = Dispatchers.Main
) {
    private val scope = CoroutineScope(Dispatchers.IO)
    protected val loggingTag = "locating"

    @ExperimentalCoroutinesApi
    fun getLocationChannel(timeout: Duration, maxAge: Duration): Channel<LocationFound> {
        val channel = Channel<LocationFound>()
        val totalTimeout = maxAge + timeout

        // request location for all providers in parallel
        val deferred = providers.map { provider ->
            scope.async(locationCoroutineContext) {
                val location = requestLocation(provider)
                if (location != null && location.time.notOlderThan(totalTimeout)) {
                    // report any location found as non-final one
                    channel.send(LocationFound(location, false))
                }
                location
            }
        }

        scope.launch {
            // await no more than requested
            withTimeoutOrNull(timeout.toMillis()) {
                deferred.awaitAll()
            }
            Log.d(loggingTag, "$timeout timeout is completed")

            // find first reasonable location
            val location = deferred.asSequence()
                    .onEach { if (it.isActive) it.cancel() } // ensure all tasks are completed
                    .filter { it.isCompleted } // filter out non-completed ones
                    .filter { !it.isCancelled } // filter out cancelled ones
                    .mapNotNull { it.getCompleted() } // get non-null location
                    .filter { it.time.notOlderThan(totalTimeout) } // filter out too old locations
                    .sortedWith(compareBy(nullsLast()) { it.coordinates.accuracy?.value }) // sort by accuracy
                    .firstOrNull()

            // send reasonable location as final one
            channel.send(LocationFound(location, true))
            channel.close()
        }
        return channel
    }

    protected abstract suspend fun requestLocation(provider: Provider): Location?
}

fun Date.notOlderThan(duration: Duration) = Date().time - this.time <= duration.toMillis()
