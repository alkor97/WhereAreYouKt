package info.alkor.whereareyou.api.persistence

import info.alkor.whereareyou.model.action.LocationResponse
import info.alkor.whereareyou.model.action.Person
import info.alkor.whereareyou.model.location.Location

interface LocationResponsePersistence {
    fun onLocationRequested(from: Person): LocationResponse
    fun onLocationProvided(response: LocationResponse, location: Location?)
    fun onLocationResponseSent(response: LocationResponse)
    fun onLocationResponseDelivered(response: LocationResponse)
}