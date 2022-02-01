package info.alkor.whereareyou.impl.action

import android.content.res.Configuration
import android.content.res.Resources
import info.alkor.whereareyou.R
import info.alkor.whereareyou.impl.context.AppContext
import info.alkor.whereareyou.model.action.LocationResponse
import info.alkor.whereareyou.model.action.Person
import info.alkor.whereareyou.model.location.LocationFormatter
import java.util.*

class LocationResponseParser(private val context: AppContext) {

    private val locationCannotBeComputedResponses = Locale.getAvailableLocales()
        .map { getLocalizedResources(it) }
        .map { it.getString(R.string.no_location_response) }

    fun parseLocationResponse(person: Person, text: String): LocationResponse? {
        val prefix = getString("")
        val trimmedText = text.trim()
        if (trimmedText.startsWith(prefix)) {
            val location = LocationFormatter.parse(trimmedText.substring(prefix.length))
            return LocationResponse(
                person = person,
                time = location?.time ?: Date(),
                location = location,
                final = true
            )
        }
        if (locationCannotBeComputedResponses.contains(trimmedText)) {
            return LocationResponse(
                person = person,
                time = Date(),
                location = null,
                final = true
            )
        }
        return null // some irrelevant text arrived from remote person
    }

    fun formatLocationResponse(response: LocationResponse): String {
        if (response.location != null) {
            return getString(LocationFormatter.format(response.location, response.time))
        }
        return context.settings.getNonExistingLocationResponse()
    }

    private fun getString(location: String) = context.settings.getLocationResponseString(location)

    private fun getLocalizedResources(locale: Locale): Resources {
        val localizedConfiguration = Configuration(context.resources.configuration)
        localizedConfiguration.setLocale(locale)
        val localizedContext = context.createConfigurationContext(localizedConfiguration)
        return localizedContext.resources
    }
}
