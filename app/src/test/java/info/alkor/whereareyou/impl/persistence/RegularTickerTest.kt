package info.alkor.whereareyou.impl.persistence

import info.alkor.whereareyou.common.Duration
import info.alkor.whereareyou.common.millis
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.consumeEach
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

@ExperimentalCoroutinesApi
class RegularTickerTest {
    @Test
    fun testTicksEmission() = runBlocking {
        val ticker = RegularTicker(millis(100), millis(500))
        var i = 1
        ticker.start().consumeEach { elapsed ->
            assertEquals(millis(i * 100), elapsed)
            i++
        }
    }

    @Test
    fun testClosingBreaksTicksEmission() = runBlocking {
        val ticker = RegularTicker(millis(100), millis(500))
        val collected = arrayListOf<Duration>()

        val channel = ticker.start()
        val job = launch { channel.consumeEach { collected.add(it) } }

        delay(100)
        // channel closing should stop ticks emission
        channel.close()
        // wait until tick generation coroutine finishes
        job.join()
        assertTrue(collected.size < 2)
    }
}