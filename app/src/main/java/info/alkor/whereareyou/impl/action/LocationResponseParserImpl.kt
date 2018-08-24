package info.alkor.whereareyou.impl.action

import info.alkor.whereareyou.api.action.LocationResponseParser
import info.alkor.whereareyou.model.action.LocationResponse
import info.alkor.whereareyou.model.action.Person

class LocationResponseParserImpl : LocationResponseParser {
    override fun parseLocationResponse(person: Person, text: String): LocationResponse? {
        return null
    }

    override fun formatLocationResponse(response: LocationResponse): String {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }
}
