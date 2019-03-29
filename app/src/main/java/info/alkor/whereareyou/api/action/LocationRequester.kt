package info.alkor.whereareyou.api.action

import info.alkor.whereareyou.model.action.LocationResponse
import info.alkor.whereareyou.model.action.Person

interface LocationRequester {
    fun requestLocationOf(target: Person)
    fun onLocationResponse(response: LocationResponse)
}