package info.alkor.whereareyou.api.persistence

import info.alkor.whereareyou.common.Duration
import info.alkor.whereareyou.model.action.LocationRequest
import info.alkor.whereareyou.model.action.LocationResponse
import info.alkor.whereareyou.model.action.Person
import info.alkor.whereareyou.model.action.SendingStatus
import kotlinx.coroutines.experimental.channels.BroadcastChannel

interface LocationRequestState {
    fun onLocationRequested(person: Person): LocationRequest
    fun onCommunicationStatusUpdate(request: LocationRequest, status: SendingStatus)
    fun onLocationResponse(response: LocationResponse)
    fun onLocationResponse(request: LocationRequest, response: LocationResponse)
    fun onProgressUpdated(request: LocationRequest, duration: Duration)
    fun onProgressCompleted(request: LocationRequest, duration: Duration)
    val events: BroadcastChannel<LocationRequestEvent>
}