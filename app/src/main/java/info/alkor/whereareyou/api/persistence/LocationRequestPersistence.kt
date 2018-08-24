package info.alkor.whereareyou.api.persistence

import info.alkor.whereareyou.model.action.LocationRequest
import info.alkor.whereareyou.model.action.LocationResponse
import info.alkor.whereareyou.model.action.Person
import info.alkor.whereareyou.model.action.SendingStatus

interface LocationRequestPersistence {
    fun onLocationRequested(person: Person): LocationRequest
    fun onCommunicationStatusUpdate(request: LocationRequest, status: SendingStatus)
    fun onLocationResponse(response: LocationResponse)
}