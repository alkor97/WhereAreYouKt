package info.alkor.whereareyou.impl.action

import android.util.Log
import info.alkor.whereareyou.api.action.LocationRequester
import info.alkor.whereareyou.api.context.AppContext
import info.alkor.whereareyou.model.action.LocationResponse
import info.alkor.whereareyou.model.action.Person

class LocationRequesterImpl(private val context: AppContext) : LocationRequester {

    private val sender by lazy { context.messageSender }
    private val repository by lazy { context.actionsRepository }

    private val loggingTag = "requester"

    override fun requestLocationOf(target: Person) {
        val request = repository.onExternalLocationRequested(target)

        sender.send(request) {
            repository.onCommunicationStatusUpdate(request, it)
            Log.i(loggingTag, "status of location request sent to $target is $it")
        }
        Log.i(loggingTag, "location request sent to $target")
    }

    override fun onLocationResponse(response: LocationResponse) {
        Log.i(loggingTag, "got location response from ${response.person}")
        repository.onLocationResponse(response)
    }
}