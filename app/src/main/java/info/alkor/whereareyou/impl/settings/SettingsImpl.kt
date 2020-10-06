package info.alkor.whereareyou.impl.settings

import android.content.Context
import android.preference.PreferenceManager
import info.alkor.whereareyou.R
import info.alkor.whereareyou.api.settings.Settings

class SettingsImpl(context: Context) : Settings {

    private val resources = context.resources
    private val access = SettingsAccess(PreferenceManager.getDefaultSharedPreferences(context), resources)

    override fun getLocationQueryTimeout() = access.getDurationValue(SettingsKey.LOCATION_QUERY_TIMEOUT)
    override fun getLocationRequestString() = access.getStringValue(SettingsKey.LOCATION_QUERY_STRING)
    override fun getLocationResponseString(location: String): String = resources.getString(
            R.string.location_response_format, location)

    override fun getLocationMaxAge() = access.getDurationValue(SettingsKey.LOCATION_RESPONSE_MAX_AGE)
}