package info.alkor.whereareyou.location

import kotlinx.coroutines.experimental.async
import kotlinx.coroutines.experimental.awaitAll
import kotlinx.coroutines.experimental.withTimeoutOrNull
import java.util.*
import java.util.concurrent.TimeUnit

abstract class AbstractLocationProvider(private val providers: Array<ProviderType> = ProviderType.values()) {

    suspend fun getLocation(timeout: Long, timeUnit: TimeUnit = TimeUnit.MILLISECONDS): Location? {
        val deferred = providers.map {
            async {
                requestLocation(it)
            }
        }
        withTimeoutOrNull(timeout, timeUnit) {
            deferred.awaitAll()
        }
        return deferred.filter { it.isCompleted }
                .mapNotNull { it.getCompleted() }
                .filter { it.time.notOlderThan(1, TimeUnit.MINUTES) }
                .sortedWith(compareBy<Location, Float?>(nullsLast(), { it.coordinates.accuracy }))
                .firstOrNull()
    }

    protected abstract suspend fun requestLocation(provider: ProviderType): Location?
}

fun Date.notOlderThan(duration: Long, timeUnit: TimeUnit = TimeUnit.MILLISECONDS) =
        Date().time - this.time <= TimeUnit.MILLISECONDS.convert(duration, timeUnit)
