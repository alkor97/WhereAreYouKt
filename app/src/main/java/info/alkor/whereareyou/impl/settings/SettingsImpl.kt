package info.alkor.whereareyou.impl.settings

import android.content.Context
import android.preference.PreferenceManager
import info.alkor.whereareyou.api.settings.Settings
import info.alkor.whereareyou.common.duration
import info.alkor.whereareyou.common.minutes
import info.alkor.whereareyou.common.seconds
import info.alkor.whereareyoukt.R

class SettingsImpl(context: Context) : Settings {

    private val preferences = PreferenceManager.getDefaultSharedPreferences(context)
    private val resources = context.resources

    override fun getLocationQueryTimeout() = getDurationValue(SettingsKey.LOCATION_QUERY_TIMEOUT)
            ?: seconds(15)
    override fun getLocationRequestString() = getStringValue(SettingsKey.LOCATION_QUERY_STRING)
    override fun getLocationResponseString(location: String): String = resources.getString(
            R.string.location_response_format, location)

    override fun getLocationMaxAge() = getDurationValue(SettingsKey.LOCATION_RESPONSE_MAX_AGE)
            ?: minutes(1)

    private fun getDurationValue(key: SettingsKey) = duration(getStringValue(key))
    private fun getStringValue(key: SettingsKey) = getStringValue(key, resources.getString(key.defaultId))
    private fun getStringValue(key: SettingsKey, defaultValue: String): String = preferences.getString(key.toString(), defaultValue)
}