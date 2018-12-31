package info.alkor.whereareyou.impl.settings

import android.content.SharedPreferences
import android.content.res.Resources
import info.alkor.whereareyou.common.Duration
import info.alkor.whereareyou.common.duration
import info.alkor.whereareyou.ui.toString

class SettingsAccess(private val preferences: SharedPreferences, private val resources: Resources) {

    fun getStringValue(key: SettingsKey) = fromPreferences(key, fromResources(key))
    fun getDurationValue(key: SettingsKey): Duration {
        val defaultValue = fromResources(key)
        val readValue = fromPreferences(key, defaultValue)
        return duration(readValue)
                ?: duration(defaultValue)
                ?: throw IllegalArgumentException("Unable to parse duration value of ${key.name}: " +
                        "neither $defaultValue nor $readValue match duration syntax.")
    }

    fun getDurationValueAsString(key: SettingsKey) = getDurationValue(key).toString(resources)

    private fun fromResources(key: SettingsKey): String = resources.getString(key.defaultId)
    private fun fromPreferences(key: SettingsKey, defaultValue: String): String = preferences.getString(key.toString(), defaultValue)
}
