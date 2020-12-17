package info.alkor.whereareyou.impl.action

import android.util.Log
import info.alkor.whereareyou.impl.context.AppContext
import info.alkor.whereareyou.model.action.LocationResponse
import info.alkor.whereareyou.model.action.Person
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class LocationRequester(private val context: AppContext) {

    private val scope = CoroutineScope(Dispatchers.IO)
    private val sender by lazy { context.messageSender }
    private val repository by lazy { context.actionsRepository }

    private val loggingTag = "requester"

    fun requestLocationOf(target: Person) {
        scope.launch {
            val request = repository.onExternalLocationRequested(target)
            val channel = sender.send(request)
            for (status in channel) {
                repository.onCommunicationStatusUpdate(request, status)
                Log.i(loggingTag, "status of location request sent to $target is $status")
            }
        }
    }

    fun onLocationResponse(response: LocationResponse) {
        Log.i(loggingTag, "got location response from ${response.person}")
        repository.onLocationResponse(response)
    }
}