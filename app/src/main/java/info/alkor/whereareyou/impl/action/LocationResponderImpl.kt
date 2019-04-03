package info.alkor.whereareyou.impl.action

import android.util.Log
import info.alkor.whereareyou.api.action.LocationResponder
import info.alkor.whereareyou.api.context.AppContext
import info.alkor.whereareyou.impl.persistence.RegularTicker
import info.alkor.whereareyou.model.action.LocationRequest
import info.alkor.whereareyou.model.action.LocationResponse
import info.alkor.whereareyou.model.action.PhoneNumber
import info.alkor.whereareyou.model.action.finishesSending
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

class LocationResponderImpl(private val context: AppContext) : LocationResponder {

    private val repository by lazy { context.actionsRepository }
    private val settings by lazy { context.settings }
    private val locationProvider by lazy { context.locationProvider }
    private val sender by lazy { context.messageSender }

    private val loggingTag = "responder"

    override fun handleLocationRequest(incomingRequest: LocationRequest): LocationRequest {
        Log.i(loggingTag, "received location request from ${incomingRequest.from}")
        val request = repository.onMyLocationRequested(incomingRequest.from)

        val timeout = settings.getLocationQueryTimeout()
        val unit = TimeUnit.SECONDS
        val ticker = RegularTicker(context, unit) { elapsed ->
            repository.updateProgress(request.id!!,
                    elapsed.convertValue(unit).toFloat() / timeout.convertValue(unit))
            Unit
        }
        ticker.start()

        val maxAge = settings.getLocationMaxAge()
        locationProvider.getLocation(timeout, maxAge) { location, final ->
            val response = LocationResponse(request.from, location, final)
            repository.onLocationResponse(response, request.id)

            if (final) {
                if (request.from.phone != PhoneNumber.OWN) {
                    GlobalScope.launch {
                        sendResponse(request, response)
                        ticker.stop()
                    }
                } else {
                    ticker.stop()
                }
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
}