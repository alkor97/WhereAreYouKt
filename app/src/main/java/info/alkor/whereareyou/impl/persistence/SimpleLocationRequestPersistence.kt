package info.alkor.whereareyou.impl.persistence

import android.util.Log
import info.alkor.whereareyou.api.persistence.*
import info.alkor.whereareyou.common.Duration
import info.alkor.whereareyou.model.action.*
import info.alkor.whereareyou.model.location.Location
import kotlinx.coroutines.experimental.channels.BroadcastChannel
import kotlinx.coroutines.experimental.launch

class SimpleLocationRequestPersistence : LocationRequestState {

    private data class PersistentLocationRequest(
            val id: MessageId,
            val person: Person,
            var sendingStatus: SendingStatus = SendingStatus.PENDING,
            var location: Location? = null,
            var finalLocation: Boolean = false)

    private val repository = ArrayList<PersistentLocationRequest>()

    private val loggingTag = "persistence"

    override val events = BroadcastChannel<LocationRequestEvent>(5)

    override fun onLocationRequested(person: Person): LocationRequest {
        val id = nextMessageId()
        repository.add(PersistentLocationRequest(id, person))
        sendUpdate(LocationRequested(person))
        return LocationRequest(person, id)
    }

    override fun onCommunicationStatusUpdate(request: LocationRequest, status: SendingStatus) {
        if (request.id != null) {
            val found = findById(request.id)
            found.forEach {
                it.sendingStatus = status
                sendUpdate(SendingStatusUpdated(status))
            }

            if (found.isEmpty()) {
                Log.w(loggingTag, "no record found for request $request")
            }
        }
    }

    override fun onLocationResponse(response: LocationResponse) {
        val found = findMatching(response)
        found.forEach {
            processFound(it, response)
        }

        val list = found.toList()
        if (list.isEmpty()) {
            Log.w(loggingTag, "no match found for location response $response")
        } else if (list.size > 1) {
            Log.w(loggingTag, "${list.size} matches found location response $response")
        }
    }

    override fun onLocationResponse(request: LocationRequest, response: LocationResponse) {
        if (request.id != null) {
            val found = findById(request.id)
            found.forEach {
                processFound(it, response)
            }
            if (found.isEmpty()) {
                Log.w(loggingTag, "no record found for response $request to request $request")
            }
        }
    }

    private fun processFound(found: PersistentLocationRequest, response: LocationResponse) {
        found.location = response.location
        found.finalLocation = response.final
        if (response.final) {
            if (response.location != null) {
                sendUpdate(FinalLocation(response.location))
            } else {
                sendUpdate(NoLocation)
            }
        } else if (response.location != null) {
            sendUpdate(IntermediateLocation(response.location))
        }
    }

    override fun onProgressUpdated(request: LocationRequest, duration: Duration) {
        sendUpdate(ExecutionProgress(duration))
    }

    override fun onProgressCompleted(request: LocationRequest, duration: Duration) {
        sendUpdate(ExecutionCompleted(duration))
    }

    private fun findById(id: MessageId) = repository.filter { it.id == id }

    private fun findMatching(response: LocationResponse) = repository.asSequence().filter {
        it.person == response.person
                && !it.finalLocation
                && it.location == null
                && it.sendingStatus == SendingStatus.DELIVERED
    }.sortedBy { it.id }

    private fun nextMessageId() = MessageId(System.currentTimeMillis())

    private fun sendUpdate(event: LocationRequestEvent) = launch {
        events.send(event)
    }
}