package info.alkor.whereareyou.impl.action

import android.util.Log
import info.alkor.whereareyou.api.action.LocationResponder
import info.alkor.whereareyou.api.context.AppContext
import info.alkor.whereareyou.impl.persistence.RegularTicker
import info.alkor.whereareyou.model.action.LocationRequest
import info.alkor.whereareyou.model.action.LocationResponse
import info.alkor.whereareyou.model.action.PhoneNumber
import info.alkor.whereareyou.model.action.finishesSending
import kotlinx.coroutines.experimental.launch
import java.util.concurrent.TimeUnit
import kotlin.coroutines.experimental.suspendCoroutine

class LocationResponderImpl(private val context: AppContext) : LocationResponder {

    private val persistence by lazy { context.locationRequestPersistence }
    private val settings by lazy { context.settings }
    private val locationProvider by lazy { context.locationProvider }
    private val sender by lazy { context.messageSender }

    private val loggingTag = "responder"

    override fun handleLocationRequest(request: LocationRequest) {
        val person = request.person
        Log.i(loggingTag, "received location request from $person")
        val newRequest = persistence.onLocationRequested(person)

        val ticker = RegularTicker(context, TimeUnit.SECONDS, newRequest)
        ticker.start()

        val timeout = settings.getLocationQueryTimeout()
        val maxAge = settings.getLocationMaxAge()
        locationProvider.getLocation(timeout, maxAge) { location, final ->
            val response = LocationResponse(person, location, final)
            persistence.onLocationResponse(newRequest, response)

            if (final) {
                if (person.phone != PhoneNumber.OWN) {
                    launch {
                        sendResponse(request, response)
                        ticker.stop()
                    }
                } else {
                    ticker.stop()
                }
            }
        }
    }

    private suspend fun sendResponse(request: LocationRequest, response: LocationResponse) = suspendCoroutine<Unit> { cont ->
        val person = request.person
        sender.send(response) {
            persistence.onCommunicationStatusUpdate(request, it)
            Log.i(loggingTag, "status of location response sent to $person is $it")
            if (it.finishesSending()) {
                cont.resume(Unit)
            }
            Log.i(loggingTag, "location response sent to $person")
        }
    }
}