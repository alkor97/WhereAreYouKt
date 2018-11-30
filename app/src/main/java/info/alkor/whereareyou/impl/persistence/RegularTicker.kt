package info.alkor.whereareyou.impl.persistence

import info.alkor.whereareyou.api.context.AppContext
import info.alkor.whereareyou.common.millis
import info.alkor.whereareyou.model.action.LocationRequest
import java.util.*
import java.util.concurrent.TimeUnit

class RegularTicker(context: AppContext, private val resolution: TimeUnit, private val request: LocationRequest) {
    private val persistence = context.locationRequestPersistence
    private val timer = Timer()
    private val startTime by lazy { now() }

    private val task = object : TimerTask() {
        override fun run() {
            persistence.onProgressUpdated(request, elapsedTime())
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
        persistence.onProgressCompleted(request, elapsedTime())
    }
}
