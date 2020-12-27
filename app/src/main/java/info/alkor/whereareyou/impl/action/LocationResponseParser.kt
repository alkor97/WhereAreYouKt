package info.alkor.whereareyou.impl.action

import info.alkor.whereareyou.impl.context.AppContext
import info.alkor.whereareyou.model.action.LocationResponse
import info.alkor.whereareyou.model.action.Person
import info.alkor.whereareyou.model.location.LocationFormatter

class LocationResponseParser(private val context: AppContext) {

    fun parseLocationResponse(person: Person, text: String): LocationResponse {
        val prefix = getString("")
        val trimmedText = text.trim()
        if (trimmedText.startsWith(prefix)) {
            val location = LocationFormatter.parse(trimmedText.substring(prefix.length))
            return LocationResponse(person, location, true)
        }
        return LocationResponse(person, null, true)
    }

    fun formatLocationResponse(response: LocationResponse): String {
        if (response.location != null) {
            return getString(LocationFormatter.format(response.location))
        }
        return context.settings.getNonExistingLocationResponse()
    }

    private fun getString(location: String) = context.settings.getLocationResponseString(location)
}
