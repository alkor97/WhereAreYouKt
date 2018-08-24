package info.alkor.whereareyou.api.action

import info.alkor.whereareyou.model.action.LocationResponse
import info.alkor.whereareyou.model.action.Person

interface LocationRequester {
    fun requestLocationOf(person: Person)
    fun onLocationResponse(response: LocationResponse)
}