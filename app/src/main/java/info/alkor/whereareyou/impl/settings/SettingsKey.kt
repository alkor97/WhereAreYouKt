package info.alkor.whereareyou.impl.settings

import info.alkor.whereareyoukt.R

enum class SettingsKey(val defaultId: Int, val summaryId: Int) {
    LOCATION_QUERY_TIMEOUT(
            R.string.location_query_timeout_default,
            R.string.location_query_timeout_summary);

    override fun toString() = name.toLowerCase()

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