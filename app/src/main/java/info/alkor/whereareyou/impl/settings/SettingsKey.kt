package info.alkor.whereareyou.impl.settings

import info.alkor.whereareyoukt.R

enum class SettingsKey(val defaultId: Int) {
    // enum names must match keys from settings.xml (lowercase)
    LOCATION_QUERY_TIMEOUT(R.string.location_query_timeout_default) {
        override fun getSummary(access: SettingsAccess) = access.getDurationValueAsString(this)
    },
    LOCATION_QUERY_STRING(R.string.location_query_string_default) {
        override fun getSummary(access: SettingsAccess) = access.getStringValue(this)
    },
    LOCATION_RESPONSE_MAX_AGE(R.string.location_response_max_age_default) {
        override fun getSummary(access: SettingsAccess) = access.getDurationValueAsString(this)
    };

    override fun toString() = name.toLowerCase()
    abstract fun getSummary(access: SettingsAccess): String

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