package info.alkor.whereareyou.impl.action

import android.util.Log
import info.alkor.whereareyou.common.Duration
import info.alkor.whereareyou.common.loggingTagOf
import info.alkor.whereareyou.impl.context.AppContext
import info.alkor.whereareyou.impl.persistence.RegularTicker
import info.alkor.whereareyou.model.action.LocationRequest
import info.alkor.whereareyou.model.action.LocationResponse
import info.alkor.whereareyou.model.action.PhoneNumber
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.consumeEach
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit

class LocationResponder(private val context: AppContext) {

    private val repository by lazy { context.actionsRepository }
    private val settings by lazy { context.settings }
    private val locationProvider by lazy { context.locationProvider }
    private val sender by lazy { context.messageSender }

    private val scope = CoroutineScope(Dispatchers.IO)

    private val loggingTag = loggingTagOf("responder")

    @ExperimentalCoroutinesApi
    suspend fun handleLocationRequest(incomingRequest: LocationRequest) {
        Log.i(loggingTag, "received location request from ${incomingRequest.from}")
        val request = repository.onMyLocationRequested(incomingRequest.from)

        val timeout = settings.getLocationQueryTimeout()
        val unit = TimeUnit.SECONDS

        val ticker = startTicker(timeout.convertTo(unit)) { elapsed ->
            scope.launch {
                repository.updateProgress(request.id!!,
                        elapsed.convertValue(unit).toFloat() / timeout.convertValue(unit))
            }
        }

        val maxAge = settings.getLocationMaxAge()
        locationProvider.getLocationChannel(timeout, maxAge).consumeEach { found ->
            val response = LocationResponse(request.from, found.location, found.final)
            repository.onLocationResponse(response, request.id)

            if (found.final) {
                if (request.from.phone != PhoneNumber.OWN) {
                    sendResponse(request, response)
                }
                ticker.stop()
            }
        }
    }

    private suspend fun sendResponse(request: LocationRequest, response: LocationResponse) {
        val channel = sender.send(response)
        for (status in channel) {
            repository.onCommunicationStatusUpdate(request, status)
            Log.i(loggingTag, "status of location response sent to ${request.from} is $status")
        }
    }

    @ExperimentalCoroutinesApi
    private fun startTicker(timeout: Duration, handler: (Duration) -> Unit): RegularTicker {
        val unit = TimeUnit.SECONDS
        val ticker = RegularTicker(Duration(1, unit), timeout)
        scope.launch {
            ticker.start().consumeEach(handler)
        }
        return ticker
    }
}