package info.alkor.whereareyou.api.action

import info.alkor.whereareyou.model.action.LocationResponse
import info.alkor.whereareyou.model.action.Person

interface LocationResponseParser {
    fun parseLocationResponse(person: Person, text: String): LocationResponse?
    fun formatLocationResponse(response: LocationResponse): String
}