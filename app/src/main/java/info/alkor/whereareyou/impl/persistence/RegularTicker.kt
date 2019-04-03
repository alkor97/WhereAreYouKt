package info.alkor.whereareyou.impl.persistence

import info.alkor.whereareyou.common.Duration
import info.alkor.whereareyou.common.millis
import java.util.*
import java.util.concurrent.TimeUnit

class RegularTicker(private val resolution: TimeUnit, private val callback: (Duration) -> Unit) {
    private val timer = Timer()
    private val startTime by lazy { now() }

    private val task = object : TimerTask() {
        override fun run() {
            callback(elapsedTime())
        }
    }

    private fun now() = millis(System.currentTimeMillis())
    private fun elapsedTime() = (now() - startTime).convertTo(resolution)

    fun start() {
        val period = TimeUnit.MILLISECONDS.convert(1, resolution)
        timer.schedule(task, period, period)
        startTime // initialize start time
    }

    fun stop() {
        task.cancel()
        timer.cancel()
    }
}
