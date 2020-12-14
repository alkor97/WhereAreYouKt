package info.alkor.whereareyou.impl.action

import android.util.Log
import info.alkor.whereareyou.common.Duration
import info.alkor.whereareyou.impl.context.AppContext
import info.alkor.whereareyou.impl.persistence.RegularTicker
import info.alkor.whereareyou.model.action.LocationRequest
import info.alkor.whereareyou.model.action.LocationResponse
import info.alkor.whereareyou.model.action.PhoneNumber
import info.alkor.whereareyou.model.action.finishesSending
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.consumeEach
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

class LocationResponder(private val context: AppContext) {

    private val repository by lazy { context.actionsRepository }
    private val settings by lazy { context.settings }
    private val locationProvider by lazy { context.locationProvider }
    private val sender by lazy { context.messageSender }

    private val loggingTag = "responder"

    @ExperimentalCoroutinesApi
    suspend fun handleLocationRequest(incomingRequest: LocationRequest): LocationRequest {
        Log.i(loggingTag, "received location request from ${incomingRequest.from}")
        val request = repository.onMyLocationRequested(incomingRequest.from)

        val timeout = settings.getLocationQueryTimeout()
        val unit = TimeUnit.SECONDS

        val ticker = startTicker(timeout.convertTo(unit)) { elapsed ->
            repository.updateProgress(request.id!!,
                    elapsed.convertValue(unit).toFloat() / timeout.convertValue(unit))
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
        return request
    }

    private suspend fun sendResponse(request: LocationRequest, response: LocationResponse) = suspendCoroutine<Unit> { cont ->
        val person = request.from
        sender.send(response) {
            repository.onCommunicationStatusUpdate(request, it)
            Log.i(loggingTag, "status of location response sent to $person is $it")
            if (it.finishesSending()) {
                cont.resume(Unit)
            }
            Log.i(loggingTag, "location response sent to $person")
        }
    }

    @ExperimentalCoroutinesApi
    private fun startTicker(timeout: Duration, handler: (Duration) -> Unit): RegularTicker {
        val unit = TimeUnit.SECONDS
        val ticker = RegularTicker(Duration(1, unit), timeout)
        CoroutineScope(Dispatchers.Default).launch {
            ticker.start().consumeEach(handler)
        }
        return ticker
    }
}