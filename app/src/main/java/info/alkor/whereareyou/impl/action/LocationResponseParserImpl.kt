package info.alkor.whereareyou.impl.action

import info.alkor.whereareyou.api.action.LocationResponseParser
import info.alkor.whereareyou.api.context.AppContext
import info.alkor.whereareyou.model.action.LocationResponse
import info.alkor.whereareyou.model.action.Person
import info.alkor.whereareyou.model.location.LocationFormatter

class LocationResponseParserImpl(private val context: AppContext) : LocationResponseParser {

    override fun parseLocationResponse(person: Person, text: String): LocationResponse? {
        val prefix = getString("")
        val location = LocationFormatter.parse(text.trim().substring(prefix.length))
        if (location != null) {
            return LocationResponse(person, location, false)
        }
        return null
    }

    override fun formatLocationResponse(response: LocationResponse): String {
        if (response.location != null) {
            return getString(LocationFormatter.format(response.location))
        }
        return ""
    }

    private fun getString(location: String) = context.settings.getLocationResponseString(location)
}
