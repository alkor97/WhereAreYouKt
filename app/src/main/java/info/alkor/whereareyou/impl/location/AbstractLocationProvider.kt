package info.alkor.whereareyou.impl.location

import android.util.Log
import info.alkor.whereareyou.common.Duration
import info.alkor.whereareyou.common.loggingTagOf
import info.alkor.whereareyou.model.location.ComputedLocation
import info.alkor.whereareyou.model.location.Provider
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import java.util.*

data class LocationFound(val location: ComputedLocation?, val final: Boolean)

abstract class AbstractLocationProvider(
        private val providers: Array<Provider> = Provider.values()
) {
    private val scope = CoroutineScope(Dispatchers.IO)
    protected val loggingTag = loggingTagOf("locating")

    @ExperimentalCoroutinesApi
    fun getLocationChannel(timeout: Duration, maxAge: Duration): Channel<LocationFound> {
        val channel = Channel<LocationFound>()
        val totalTimeout = maxAge + timeout

        // request location for all providers in parallel
        val deferred = providers.map { provider ->
            scope.async {
                val location = requestLocation(provider, timeout)
                if (location != null) {
                    // report any location found as non-final one
                    channel.send(LocationFound(location, false))
                }
                Log.d(loggingTag, "completed with $location")
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
        }.invokeOnCompletion {
            Log.d(loggingTag, "location awaiting completed with: " + (it?.allReasons()
                    ?: "success"))
            channel.close()
        }
        return channel
    }

    protected abstract suspend fun requestLocation(provider: Provider, timeout: Duration): ComputedLocation?
}

fun Date.notOlderThan(duration: Duration) = Date().time - this.time <= duration.toMillis()

fun Throwable.allReasons(): String {
    if (localizedMessage != null) {
        return "\n- $localizedMessage" + (cause?.allReasons() ?: "")
    }
    return ""
}