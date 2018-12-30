package info.alkor.whereareyou.impl.settings

import android.content.res.Resources
import info.alkor.whereareyou.common.duration
import info.alkor.whereareyou.common.minutes
import info.alkor.whereareyou.common.seconds
import info.alkor.whereareyou.ui.toString
import info.alkor.whereareyoukt.R

enum class SettingsKey(val defaultId: Int) {
    // enum names must match keys from settings.xml (lowercase)
    LOCATION_QUERY_TIMEOUT(R.string.location_query_timeout_default) {
        override fun getSummary(resources: Resources, value: String): String {
            val duration = duration(value) ?: seconds(15)
            return duration.toString(resources)
        }
    },
    LOCATION_QUERY_STRING(R.string.location_query_string_default) {
        override fun getSummary(resources: Resources, value: String): String {
            return value
        }
    },
    LOCATION_RESPONSE_MAX_AGE(R.string.location_response_max_age_default) {
        override fun getSummary(resources: Resources, value: String): String {
            val duration = duration(value) ?: minutes(1)
            return duration.toString(resources)
        }
    };

    override fun toString() = name.toLowerCase()
    abstract fun getSummary(resources: Resources, value: String): String

    companion object {
        fun fromString(key: String): SettingsKey? {
            val uppercase = key.toUpperCase()
            for (settingsKey in values()) {
                if (uppercase == settingsKey.name) {
                    return settingsKey
                }
            }
            return null
        }
    }
}