package info.alkor.whereareyou.impl.persistence

import android.arch.lifecycle.MutableLiveData
import android.util.Log
import info.alkor.whereareyou.api.persistence.LocationActionRepository
import info.alkor.whereareyou.api.persistence.LocationRequestEvent
import info.alkor.whereareyou.model.action.*
import info.alkor.whereareyou.model.location.Location
import kotlinx.coroutines.channels.BroadcastChannel

class LocationActionRepositoryImpl : LocationActionRepository {

    override val all = MutableLiveData<List<LocationAction>>()
    override val events = BroadcastChannel<LocationRequestEvent>(5)
    private val data = ArrayList<LocationAction>()

    private val loggingTag = "persistence"

    @Synchronized
    override fun remove(id: MessageId) {
        val found = findById(id)
        found.forEach { (index, _) ->
            data.removeAt(index)
        }
        postUpdates()
    }

    override fun onExternalLocationRequested(target: Person): LocationRequest = onLocationRequested(Direction.OUTGOING, target)

    override fun onMyLocationRequested(requester: Person): LocationRequest = onLocationRequested(Direction.INCOMING, requester)

    @Synchronized
    private fun onLocationRequested(direction: Direction, person: Person): LocationRequest {
        Log.d(loggingTag, "onLocationRequested: $direction $person")
        val action = LocationAction(
                nextMessageId(),
                direction,
                person,
                null,
                false,
                SendingStatus.PENDING,
                0.0f)

        data.add(action)
        postUpdates()

        return LocationRequest(person, action.id)
    }

    @Synchronized
    override fun onCommunicationStatusUpdate(request: LocationRequest, status: SendingStatus) {
        Log.d(loggingTag, "onCommunicationStatusUpdate: $request $status")
        if (request.id != null) {
            val found = findById(request.id)
            found.forEach { (index, action) ->
                data[index] = action.updateStatus(status)
            }

            if (found.isEmpty()) {
                Log.w(loggingTag, "no record found for request $request")
            } else {
                postUpdates()
            }
        }
    }

    @Synchronized
    override fun onLocationResponse(response: LocationResponse, id: MessageId?) {
        Log.d(loggingTag, "onLocationResponse: $response $id")
        val found = if (id != null) findById(id) else findMatching(response)
        found.forEach { (index, action) ->
            data[index] = action.updateLocationAndFinal(response.location, response.final)
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

    @Synchronized
    override fun updateProgress(id: MessageId, progress: Float) {
        findById(id).forEach { (index, action) ->
            data[index] = action.updateProgress(progress)
        }
        postUpdates()
    }

    private fun indexed() = data.mapIndexed { index, element -> Pair(index, element) }

    private fun findMatching(response: LocationResponse) = indexed()
            .filter { (_, action) ->
                action.person == response.person
                        && !action.final
                        && action.location != response.location
                        && action.status == SendingStatus.PENDING
            }.sortedBy { it.second.id }

    private fun findById(id: MessageId) = indexed()
            .filter { it.second.id == id }

    private fun nextMessageId() = MessageId(System.currentTimeMillis())

    private fun postUpdates() {
        all.postValue(data.reversed())
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
