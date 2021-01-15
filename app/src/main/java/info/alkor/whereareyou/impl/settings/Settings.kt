package info.alkor.whereareyou.impl.settings

import android.content.Context
import androidx.preference.PreferenceManager
import info.alkor.whereareyou.R

class Settings(context: Context) {

    private val resources = context.resources
    private val access = SettingsAccess(PreferenceManager.getDefaultSharedPreferences(context), resources)

    fun getLocationQueryTimeout() = access.getDurationValue(SettingsKey.LOCATION_QUERY_TIMEOUT)
    fun getLocationRequestString() = access.getStringValue(SettingsKey.LOCATION_QUERY_STRING)
    fun getLocationResponseString(location: String): String = resources.getString(
            R.string.location_response_format, location)

    fun getNonExistingLocationResponse() = resources.getString(R.string.no_location_response)

    fun getLocationMaxAge() = access.getDurationValue(SettingsKey.LOCATION_RESPONSE_MAX_AGE)
}