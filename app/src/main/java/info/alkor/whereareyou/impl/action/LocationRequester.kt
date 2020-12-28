package info.alkor.whereareyou.impl.action

import android.util.Log
import info.alkor.whereareyou.impl.context.AppContext
import info.alkor.whereareyou.model.action.LocationResponse
import info.alkor.whereareyou.model.action.MessageId
import info.alkor.whereareyou.model.action.Person

class LocationRequester(private val context: AppContext) {

    private val sender by lazy { context.messageSender }
    private val repository by lazy { context.actionsRepository }

    private val loggingTag = "requester"

    suspend fun requestLocationOf(target: Person): MessageId {
        val request = repository.onExternalLocationRequested(target)
        val channel = sender.send(request)
        for (status in channel) {
            repository.onCommunicationStatusUpdate(request, status)
            Log.i(loggingTag, "status of location request sent to $target is $status")
        }
        return request.id!!
    }

    suspend fun onLocationResponse(response: LocationResponse): MessageId? {
        val type = if (response.location != null) "non-null" else "null"
        Log.i(loggingTag, "got $type location response from ${response.person}")
        return repository.onLocationResponse(response)
    }
}