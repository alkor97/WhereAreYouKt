package info.alkor.whereareyou.impl.action

import info.alkor.whereareyou.api.action.LocationRequestParser
import info.alkor.whereareyou.api.context.AppContext
import info.alkor.whereareyou.model.action.LocationRequest
import info.alkor.whereareyou.model.action.Person

class LocationRequestParserImpl(private val context: AppContext) : LocationRequestParser {

    override fun parseLocationRequest(person: Person, text: String): LocationRequest? {
        if (getRequestString() == text.trim()) {
            return LocationRequest(person)
        }
        return null
    }

    override fun formatLocationRequest(request: LocationRequest) = getRequestString()

    private fun getRequestString() = context.settings.getLocationRequestString()
}