package info.alkor.whereareyou.impl.persistence

import android.content.Context
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.Transformations
import info.alkor.whereareyou.common.loggingTagOf
import info.alkor.whereareyou.model.action.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.*

class LocationActionRepository(context: Context) {

    private val scope = CoroutineScope(Dispatchers.IO)
    private val appDatabase by lazy { AppDatabase.getInstance(context) }
    private val actions: LocationActionDao by lazy { appDatabase.locationActionRecords() }

    val all: LiveData<List<LocationAction>> by lazy {
        Transformations.map(actions.all()) {
            it.map { it.toModel() }
        }
    }

    private val loggingTag = loggingTagOf("persistence")

    fun remove(id: MessageId) {
        scope.launch {
            actions.deleteAction(id)
        }
    }

    fun onExternalLocationRequested(target: Person): LocationRequest =
        onLocationRequested(Direction.OUTGOING, target)

    fun onMyLocationRequested(requester: Person): LocationRequest =
        onLocationRequested(Direction.INCOMING, requester)

    private fun onLocationRequested(direction: Direction, person: Person): LocationRequest {
        Log.d(loggingTag, "onLocationRequested: $direction $person")
        val action = LocationAction(
            null,
            direction,
            person,
            Date(),
            null,
            false,
            SendingStatus.PENDING,
            0.0f
        )

        val id = actions.addAction(action.toRecord())
        return LocationRequest(person, id)
    }

    fun onCommunicationStatusUpdate(request: LocationRequest, status: SendingStatus) {
        Log.d(loggingTag, "onCommunicationStatusUpdate: $request $status")
        if (request.id != null) {
            actions.updateSendingStatus(request.id, status)
        }
    }

    fun onLocationResponse(response: LocationResponse, id: MessageId? = null): MessageId? {
        Log.d(loggingTag, "onLocationResponse: $response $id")

        val found =
            if (id != null) actions.findById(id) else actions.findMatching(response.person.phone.toExternalForm())
        if (found != null) {
            if (response.location != null) {
                found.location = response.location.toRecord()
            }
            found.isFinal = response.final
            actions.updateAction(found)
            return found.id
        } else {
            Log.w(loggingTag, "no match found for location response $response")
            return null
        }
    }

    fun updateProgress(id: MessageId, progress: Float) {
        actions.updateProgress(id, progress)
    }
}
