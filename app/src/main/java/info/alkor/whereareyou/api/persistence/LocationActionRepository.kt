package info.alkor.whereareyou.api.persistence

import android.arch.lifecycle.LiveData
import info.alkor.whereareyou.model.action.*
import kotlinx.coroutines.channels.BroadcastChannel

interface LocationActionRepository {
    val all: LiveData<List<LocationAction>>
    val events: BroadcastChannel<LocationRequestEvent>
    fun onExternalLocationRequested(target: Person): LocationRequest
    fun onMyLocationRequested(requester: Person): LocationRequest
    fun onCommunicationStatusUpdate(request: LocationRequest, status: SendingStatus)
    fun onLocationResponse(response: LocationResponse, id: MessageId? = null)
    fun updateProgress(id: MessageId, progress: Float)
    fun remove(id: MessageId)
}
