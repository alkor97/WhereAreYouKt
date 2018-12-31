package info.alkor.whereareyou.ui.settings

import android.content.Context
import android.content.res.TypedArray
import android.preference.DialogPreference
import android.text.TextUtils
import android.util.AttributeSet
import android.view.View
import android.widget.EditText
import android.widget.Spinner
import android.widget.TextView
import info.alkor.whereareyou.common.Duration
import info.alkor.whereareyou.common.asString
import info.alkor.whereareyou.common.duration
import info.alkor.whereareyoukt.R

class DurationPreference(ctx: Context, attrs: AttributeSet) : DialogPreference(ctx, attrs) {

    private var durationMessage: TextView? = null
    private var durationValue: EditText? = null
    private var durationUnit: Spinner? = null

    private val defaultDuration = duration(days = 999)
    private var duration: Duration? = null

    init {
        dialogLayoutResource = R.layout.duration_selector
    }

    private fun setDuration(other: Duration) {
        duration = other
        duration?.let {
            durationValue?.setText(it.value.toString())

            val index = context.resources.getStringArray(R.array.time_units_values)
                    .indexOf(it.unit.asString())
            if (index > 0) {
                durationUnit?.setSelection(index)
            }
        }
    }

    private fun getDuration(): Duration? {
        durationUnit?.let { spinner ->
            val position = spinner.selectedItemPosition
            if (position > -1) {
                val unit = context.resources.getStringArray(R.array.time_units_values)[position]
                durationValue?.let {
                    return duration(it.text.toString() + unit)
                }
            }
        }
        return null
    }

    override fun onBindDialogView(view: View?) {
        super.onBindDialogView(view)
        bindControls(view)
        setDuration(getCurrentDuration())
        durationValue?.selectAll()
    }

    override fun onDialogClosed(positiveResult: Boolean) {
        if (positiveResult) {
            duration = getDuration()
            duration?.let {
                persistDuration(it)
            }
        }
        unbindControls()
        super.onDialogClosed(positiveResult)
    }

    private fun bindControls(view: View?) {
        durationValue = view?.findViewById(R.id.durationValue)
        durationUnit = view?.findViewById(R.id.durationUnit)
        durationMessage = view?.findViewById(R.id.durationMessage)

        if (!TextUtils.isEmpty(dialogMessage)) {
            durationMessage?.let {
                it.text = dialogMessage
            }
        }
    }

    private fun unbindControls() {
        durationValue = null
        durationUnit = null
        durationMessage = null
    }

    private fun getCurrentDuration() = duration ?: defaultDuration

    override fun onGetDefaultValue(a: TypedArray?, index: Int): Any {
        val value = a?.getString(index) ?: getCurrentDuration().toString()
        duration = duration(value)
        return value
    }

    override fun onSetInitialValue(restorePersistedValue: Boolean, defaultValue: Any?) {
        if (restorePersistedValue) {
            setDuration(getPersistedDuration())
        } else {
            val duration = duration(defaultValue as String) ?: getCurrentDuration()
            setDuration(duration)
            if (shouldPersist()) {
                persistDuration(duration)
            }
        }
    }

    private fun getPersistedDuration(): Duration {
        val read = getPersistedString("")
        return duration(read) ?: getCurrentDuration()
    }

    private fun persistDuration(duration: Duration) = persistString(duration.toString())
}
