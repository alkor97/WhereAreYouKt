package info.alkor.whereareyou.impl.action

import info.alkor.whereareyou.impl.context.AppContext
import info.alkor.whereareyou.model.action.LocationRequest
import info.alkor.whereareyou.model.action.Person

class LocationRequestParser(private val context: AppContext) {

    fun parseLocationRequest(person: Person, text: String): LocationRequest? {
        if (getRequestString() == text.trim()) {
            return LocationRequest(person)
        }
        return null
    }

    fun formatLocationRequest(request: LocationRequest) = getRequestString()

    private fun getRequestString() = context.settings.getLocationRequestString()
}