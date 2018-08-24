package info.alkor.whereareyou.api.action

import info.alkor.whereareyou.model.action.LocationRequest
import info.alkor.whereareyou.model.action.Person

interface LocationRequestParser {
    fun parseLocationRequest(person: Person, text: String): LocationRequest?
    fun formatLocationRequest(request: LocationRequest): String
}