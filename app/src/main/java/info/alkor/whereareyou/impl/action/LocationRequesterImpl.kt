package info.alkor.whereareyou.impl.action

import android.util.Log
import info.alkor.whereareyou.api.action.LocationRequester
import info.alkor.whereareyou.api.context.AppContext
import info.alkor.whereareyou.model.action.LocationResponse
import info.alkor.whereareyou.model.action.Person

class LocationRequesterImpl(private val context: AppContext) : LocationRequester {

    private val persistence by lazy { context.locationRequestPersistence }
    private val sender by lazy { context.messageSender }

    private val loggingTag = "requester"

    override fun requestLocationOf(person: Person) {
        val request = persistence.onLocationRequested(person)

        sender.send(request) {
            persistence.onCommunicationStatusUpdate(request, it)
            Log.i(loggingTag, "status of location request sent to $person is $it")
        }
        Log.i(loggingTag, "location request sent to $person")
    }

    override fun onLocationResponse(response: LocationResponse) {
        Log.i(loggingTag, "got location response from ${response.person}")
        persistence.onLocationResponse(response)
    }
}