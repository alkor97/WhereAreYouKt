package info.alkor.whereareyou.impl.action

import info.alkor.whereareyou.api.action.LocationRequestParser
import info.alkor.whereareyou.api.context.AppContext
import info.alkor.whereareyou.model.action.LocationRequest
import info.alkor.whereareyou.model.action.Person

class LocationRequestParserImpl(private val context: AppContext) : LocationRequestParser {

    private val requestString by lazy { context.settings.getLocationRequestString() }

    override fun parseLocationRequest(person: Person, text: String): LocationRequest? {
        if (requestString == text.trim()) {
            return LocationRequest(null, person)
        }
        return null
    }

    override fun formatLocationRequest(request: LocationRequest) = requestString
}