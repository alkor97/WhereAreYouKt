package info.alkor.whereareyou.api.settings

import info.alkor.whereareyou.common.Duration

interface Settings {
    fun getLocationQueryTimeout(): Duration
}