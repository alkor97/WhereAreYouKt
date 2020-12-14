package info.alkor.whereareyou.impl.persistence

import info.alkor.whereareyou.common.Duration
import info.alkor.whereareyou.common.millis
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel

class RegularTicker(resolution: Duration, private val max: Duration) {

    private val channel = Channel<Duration>()
    private val resolutionMillis = resolution.toMillis()

    @ExperimentalCoroutinesApi
    fun start(): Channel<Duration> {
        CoroutineScope(Dispatchers.Default).launch {
            for (i in 1..max.toMillis() / resolutionMillis) {
                delay(resolutionMillis)
                if (!channel.isClosedForSend) {
                    channel.send(millis(i * resolutionMillis))
                } else {
                    break
                }
            }
            channel.close()
        }
        return channel
    }

    fun stop() {
        channel.close()
    }
}