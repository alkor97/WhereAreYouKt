package info.alkor.whereareyou.impl.persistence

import android.content.Context
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.Transformations
import info.alkor.whereareyou.model.action.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class LocationActionRepository(context: Context) {

    private val scope = CoroutineScope(Dispatchers.IO)
    private val appDatabase by lazy { AppDatabase.getInstance(context) }
    private val actions: LocationActionDao by lazy { appDatabase.locationActionRecords() }
    private val persons: PersonDao by lazy { appDatabase.personRecords() }

    val all: LiveData<List<LocationAction>> by lazy {
        Transformations.map(actions.all()) {
            it.map { it.toModel() }
        }
    }

    private val loggingTag = "persistence"

    fun remove(id: MessageId) {
        scope.launch {
            actions.deleteAction(id)
        }
    }

    suspend fun onExternalLocationRequested(target: Person): LocationRequest = onLocationRequested(Direction.OUTGOING, target)

    suspend fun onMyLocationRequested(requester: Person): LocationRequest = onLocationRequested(Direction.INCOMING, requester)

    private suspend fun onLocationRequested(direction: Direction, person: Person): LocationRequest {
        Log.d(loggingTag, "onLocationRequested: $direction $person")
        val action = LocationAction(
                null,
                direction,
                person,
                null,
                false,
                SendingStatus.PENDING,
                0.0f)

        val id = actions.addAction(action.toRecord())
        return LocationRequest(person, id)
    }

    fun onCommunicationStatusUpdate(request: LocationRequest, status: SendingStatus) {
        Log.d(loggingTag, "onCommunicationStatusUpdate: $request $status")
        if (request.id != null) {
            scope.launch {
                actions.updateSendingStatus(request.id, status)
            }
        }
    }

    fun onLocationResponse(response: LocationResponse, id: MessageId? = null) {
        Log.d(loggingTag, "onLocationResponse: $response $id")

        scope.launch {
            val found = if (id != null) actions.findById(id) else actions.findMatching(response.person.phone.toExternalForm())
            if (found != null) {
                if (response.location != null) {
                    found.location = response.location.toRecord()
                }
                found.isFinal = response.final
                actions.updateAction(found)
            } else {
                Log.w(loggingTag, "no match found for location response $response")
            }
        }
    }

    fun updateProgress(id: MessageId, progress: Float) {
        scope.launch {
            actions.updateProgress(id, progress)
        }
    }
}
