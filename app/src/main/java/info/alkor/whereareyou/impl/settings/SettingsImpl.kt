package info.alkor.whereareyou.impl.settings

import android.content.Context
import android.preference.PreferenceManager
import info.alkor.whereareyou.api.settings.Settings
import info.alkor.whereareyou.common.seconds

class SettingsImpl(context: Context) : Settings {

    private val preferences = PreferenceManager.getDefaultSharedPreferences(context)
    private val resources = context.resources

    override fun getLocationQueryTimeout() = getSecondsValue(SettingsKey.LOCATION_QUERY_TIMEOUT)

    private fun getSecondsValue(key: SettingsKey) = seconds(getLongValue(key))

    private fun getLongValue(key: SettingsKey): Long {
        val defaultValue = resources.getString(key.defaultId)
        return try {
            getStringValue(key, defaultValue).toLong()
        } catch (e: NumberFormatException) {
            defaultValue.toLong()
        }
    }

    private fun getStringValue(key: SettingsKey, defaultValue: String): String {
        return preferences.getString(key.toString(), defaultValue)
    }
}