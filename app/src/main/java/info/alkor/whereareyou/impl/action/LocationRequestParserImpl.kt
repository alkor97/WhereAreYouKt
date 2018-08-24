package info.alkor.whereareyou.impl.action

import info.alkor.whereareyou.api.action.LocationRequestParser
import info.alkor.whereareyou.model.action.LocationRequest
import info.alkor.whereareyou.model.action.Person

class LocationRequestParserImpl : LocationRequestParser {
    override fun parseLocationRequest(person: Person, text: String): LocationRequest? {
        if (REQUEST == text.trim()) {
            return LocationRequest(null, person)
        }
        return null
    }

    override fun formatLocationRequest(request: LocationRequest): String {
        return REQUEST
    }

    companion object {
        private val REQUEST = "Hey, where are you?"
    }
}