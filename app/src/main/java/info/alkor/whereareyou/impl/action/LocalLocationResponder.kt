package info.alkor.whereareyou.impl.action

import android.util.Log
import info.alkor.whereareyou.api.action.LocationResponder
import info.alkor.whereareyou.api.context.AppContext
import info.alkor.whereareyou.impl.persistence.RegularTicker
import info.alkor.whereareyou.model.action.LocationRequest
import info.alkor.whereareyou.model.action.LocationResponse
import kotlinx.coroutines.experimental.launch
import java.util.concurrent.TimeUnit

open class LocalLocationResponder(private val context: AppContext) : LocationResponder {

    private val persistence by lazy { context.locationRequestPersistence }
    private val settings by lazy { context.settings }
    private val locationProvider by lazy { context.locationProvider }

    private val loggingTag = "responder"

    override fun handleLocationRequest(request: LocationRequest) {
        val person = request.person
        Log.i(loggingTag, "received location request from $person")
        val newRequest = persistence.onLocationRequested(person)

        val ticker = RegularTicker(context, TimeUnit.SECONDS, newRequest)
        ticker.start()

        val timeout = settings.getLocationQueryTimeout()
        locationProvider.getLocation(timeout) { location, final ->
            val response = LocationResponse(person, location, final)
            persistence.onLocationResponse(newRequest, response)

            if (final) {
                launch {
                    sendResponse(request, response)
                    ticker.stop()
                }
            }
        }
    }

    protected open suspend fun sendResponse(request: LocationRequest, response: LocationResponse) {}
}