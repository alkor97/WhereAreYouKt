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

    private val defaultDuration = duration(days = 1)
    private var durationMessage: TextView? = null
    private var durationValue: EditText? = null
    private var durationUnit: Spinner? = null

    init {
        dialogLayoutResource = R.layout.duration_selector
    }

    private fun setDuration(duration: Duration) {
        durationValue?.setText(duration.value.toString())

        val index = context.resources.getStringArray(R.array.time_units_values).indexOf(duration.unit.asString())
        if (index > 0) {
            durationUnit?.setSelection(index)
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

        durationValue = view?.findViewById(R.id.durationValue)
        durationUnit = view?.findViewById(R.id.durationUnit)
        durationMessage = view?.findViewById(R.id.durationMessage)

        val duration = getPersistedDuration(defaultDuration)
        setDuration(duration)

        if (!TextUtils.isEmpty(dialogMessage)) {
            durationMessage?.let {
                it.text = dialogMessage
            }
        }
    }

    override fun onDialogClosed(positiveResult: Boolean) {
        super.onDialogClosed(positiveResult)

        if (positiveResult) {
            val duration = getDuration()
            if (duration != null) {
                persistDuration(duration)
            }
        }
    }

    override fun onGetDefaultValue(a: TypedArray?, index: Int): Any {
        return a?.getString(index) ?: defaultDuration.toString()
    }

    override fun onSetInitialValue(restorePersistedValue: Boolean, defaultValue: Any?) {
        if (restorePersistedValue) {
            setDuration(getPersistedDuration(defaultDuration))
        } else {
            setDuration(duration(defaultValue as String) ?: defaultDuration)
        }
    }

    private fun getPersistedDuration(defaultDuration: Duration): Duration {
        val read = getPersistedString(defaultDuration.toString())
        return duration(read) ?: defaultDuration
    }

    private fun persistDuration(duration: Duration) = persistString(duration.toString())
}
