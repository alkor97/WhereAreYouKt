package info.alkor.whereareyou.ui.settings

import android.os.Bundle
import android.text.TextUtils
import android.view.View
import android.widget.EditText
import android.widget.Spinner
import android.widget.TextView
import androidx.preference.PreferenceDialogFragmentCompat
import info.alkor.whereareyou.R
import info.alkor.whereareyou.common.Duration
import info.alkor.whereareyou.common.asString
import info.alkor.whereareyou.common.duration


class DurationPreferenceDialogFragment(key: String) : PreferenceDialogFragmentCompat() {

    private var durationValue: EditText? = null
    private var durationUnit: Spinner? = null
    private var durationMessage: TextView? = null

    init {
        arguments = Bundle().apply {
            putString(ARG_KEY, key)
        }
    }

    private val preference: DurationPreference
        get() = getPreference() as DurationPreference

    override fun onBindDialogView(view: View) {
        super.onBindDialogView(view)
        durationValue = view.findViewById(R.id.durationValue) as EditText
        durationUnit = view.findViewById(R.id.durationUnit) as Spinner
        durationMessage = view.findViewById(R.id.durationMessage) as TextView

        updateControls()
    }

    override fun onDialogClosed(positiveResult: Boolean) {
        if (positiveResult) {
            val duration = readDurationFromControls()
            preference.setDuration(duration)
        }
    }

    private fun updateControls() {
        val duration = preference.getDuration()

        durationValue?.setText(duration?.value.toString())

        val index = requireContext().resources.getStringArray(R.array.time_units_values)
                .indexOf(duration?.unit?.asString())
        if (index > 0) {
            durationUnit?.setSelection(index)
        }
        durationValue?.selectAll()

        if (!TextUtils.isEmpty(preference.dialogMessage)) {
            durationMessage?.text = preference.dialogMessage
        }
    }

    private fun readDurationFromControls(): Duration? {
        val position = durationUnit?.selectedItemPosition ?: -1
        if (position > -1) {
            val unit = requireContext().resources.getStringArray(R.array.time_units_values)[position]
            durationValue.let {
                return duration(it?.text.toString() + unit)
            }
        }
        return null
    }
}