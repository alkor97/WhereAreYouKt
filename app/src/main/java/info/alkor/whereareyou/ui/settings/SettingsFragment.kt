package info.alkor.whereareyou.ui.settings


import android.content.SharedPreferences
import android.content.SharedPreferences.OnSharedPreferenceChangeListener
import android.os.Bundle
import androidx.preference.DialogPreference
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import info.alkor.whereareyou.R
import info.alkor.whereareyou.impl.settings.SettingsAccess
import info.alkor.whereareyou.impl.settings.SettingsKey

class SettingsFragment : PreferenceFragmentCompat(), OnSharedPreferenceChangeListener {

    private val DIALOG_FRAGMENT_TAG = DurationPreference::class.simpleName!!

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        addPreferencesFromResource(R.xml.settings)
    }

    override fun onResume() {
        super.onResume()
        preferenceManager.sharedPreferences.let { prefs ->
            prefs?.registerOnSharedPreferenceChangeListener(this)
            prefs?.all?.keys?.onEach { key ->
                onSharedPreferenceChanged(prefs, key)
            }
        }
    }

    override fun onPause() {
        preferenceManager.sharedPreferences?.unregisterOnSharedPreferenceChangeListener(this)
        super.onPause()
    }

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences?, key: String?) {
        if (sharedPreferences != null && key != null) {
            val typedKey = SettingsKey.fromString(key)
            if (typedKey != null) {
                val preference: DialogPreference? = preferenceScreen.findPreference(key)
                if (preference != null) {
                    val access = SettingsAccess(sharedPreferences, resources)
                    val summary = typedKey.getSummary(access)
                    preference.summary = summary
                }
            }
        }
    }

    override fun onDisplayPreferenceDialog(preference: Preference) {
        if (parentFragmentManager.findFragmentByTag(DIALOG_FRAGMENT_TAG) != null) {
            return
        }
        if (preference is DurationPreference) {
            val dialogFragment = DurationPreferenceDialogFragment(preference.key)
            dialogFragment.setTargetFragment(this, 0)
            dialogFragment.show(parentFragmentManager, DIALOG_FRAGMENT_TAG)
        } else {
            super.onDisplayPreferenceDialog(preference)
        }
    }
}