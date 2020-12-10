package info.alkor.whereareyou.impl.persistence

import android.util.Log
import androidx.lifecycle.MutableLiveData
import info.alkor.whereareyou.model.action.*
import info.alkor.whereareyou.model.location.Location

class LocationActionRepository {

    val all = MutableLiveData<List<LocationAction>>()
    private val data = InMemoryActionStorage()

    private val loggingTag = "persistence"

    fun remove(id: MessageId) {
        data.access {
            it.findById(id).forEach { (index, _) ->
                it.removeAt(index)
            }
        }
        postUpdates()
    }

    fun onExternalLocationRequested(target: Person): LocationRequest = onLocationRequested(Direction.OUTGOING, target)

    fun onMyLocationRequested(requester: Person): LocationRequest = onLocationRequested(Direction.INCOMING, requester)

    private fun onLocationRequested(direction: Direction, person: Person): LocationRequest {
        Log.d(loggingTag, "onLocationRequested: $direction $person")
        val action = LocationAction(
                data.nextMessageId(),
                direction,
                person,
                null,
                false,
                SendingStatus.PENDING,
                0.0f)

        data.access { it.addAtFront(action) }
        postUpdates()

        return LocationRequest(person, action.id)
    }

    fun onCommunicationStatusUpdate(request: LocationRequest, status: SendingStatus) {
        Log.d(loggingTag, "onCommunicationStatusUpdate: $request $status")
        if (request.id != null) {
            val found = data.access {
                val found = it.findById(request.id)
                found.forEach { (index, action) ->
                    it.setAt(index, action.updateStatus(status))
                }
                found
            }

            if (found.isEmpty()) {
                Log.w(loggingTag, "no record found for request $request")
            } else {
                postUpdates()
            }
        }
    }

    fun onLocationResponse(response: LocationResponse, id: MessageId? = null) {
        Log.d(loggingTag, "onLocationResponse: $response $id")

        val found = data.access {
            val found = if (id != null)
                it.findById(id)
            else
                it.findMatching(response)
            found.forEach { (index, action) ->
                it.setAt(index, action.updateLocationAndFinal(response.location, response.final))
            }
            found
        }

        val list = found.toList()
        when {
            list.isEmpty() -> Log.w(loggingTag, "no match found for location response $response")
            list.size > 1 -> {
                Log.w(loggingTag, "${list.size} matches found location response $response")
                postUpdates()
            }
            else -> postUpdates()
        }
    }

    fun updateProgress(id: MessageId, progress: Float) {
        data.access {
            it.findById(id).forEach { (index, action) ->
                it.setAt(index, action.updateProgress(progress))
            }
        }
        postUpdates()
    }

    private fun postUpdates() {
        data.postUpdates(all)
    }
}

fun LocationAction.updateStatus(status: SendingStatus) = LocationAction(
        this.id,
        this.direction,
        this.person,
        this.location,
        this.final,
        status,
        this.progress)

fun LocationAction.updateLocationAndFinal(location: Location?, final: Boolean) = LocationAction(
        this.id,
        this.direction,
        this.person,
        location,
        final,
        this.status,
        if (final) null else this.progress)

fun LocationAction.updateProgress(progress: Float) = if (!final) LocationAction(
        this.id,
        this.direction,
        this.person,
        this.location,
        this.final,
        this.status,
        progress) else this
