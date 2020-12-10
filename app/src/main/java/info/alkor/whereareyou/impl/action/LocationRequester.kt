package info.alkor.whereareyou.impl.action

import android.util.Log
import info.alkor.whereareyou.impl.context.AppContext
import info.alkor.whereareyou.model.action.LocationResponse
import info.alkor.whereareyou.model.action.Person

class LocationRequester(private val context: AppContext) {

    private val sender by lazy { context.messageSender }
    private val repository by lazy { context.actionsRepository }

    private val loggingTag = "requester"

    fun requestLocationOf(target: Person) {
        val request = repository.onExternalLocationRequested(target)

        sender.send(request) {
            repository.onCommunicationStatusUpdate(request, it)
            Log.i(loggingTag, "status of location request sent to $target is $it")
        }
        Log.i(loggingTag, "location request sent to $target")
    }

    fun onLocationResponse(response: LocationResponse) {
        Log.i(loggingTag, "got location response from ${response.person}")
        repository.onLocationResponse(response)
    }
}