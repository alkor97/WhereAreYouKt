package info.alkor.whereareyou.ui.settings

import android.content.Context
import android.content.res.TypedArray
import android.util.AttributeSet
import androidx.preference.DialogPreference
import info.alkor.whereareyou.common.Duration
import info.alkor.whereareyou.common.duration


class DurationPreference(ctx: Context, attrs: AttributeSet) : DialogPreference(ctx, attrs) {

    private var duration: Duration? = null

    init {
        dialogLayoutResource = info.alkor.whereareyou.R.layout.duration_selector
    }

    override fun onGetDefaultValue(a: TypedArray?, index: Int): Any {
        val value = a?.getString(index) ?: DEFAULT_DURATION.toString()
        duration = duration(value)
        return value
    }

    override fun onSetInitialValue(restorePersistedValue: Boolean, defaultValue: Any?) {
        if (restorePersistedValue) {
            duration = readPersistedDuration()
        } else {
            duration = duration(defaultValue as String)
            if (shouldPersist()) {
                persistDuration()
            }
        }
    }

    fun getDuration() = duration
    fun setDuration(duration: Duration?) {
        this.duration = duration
        persistDuration()
    }

    private fun readPersistedDuration(): Duration {
        val readDuration = getPersistedString("")
        return duration(readDuration) ?: DEFAULT_DURATION
    }

    private fun persistDuration() = persistString(duration.toString())

    companion object {
        val DEFAULT_DURATION = duration(days = 999)
    }
}