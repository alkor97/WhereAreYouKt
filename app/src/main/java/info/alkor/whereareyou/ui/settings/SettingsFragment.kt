package info.alkor.whereareyou.ui.settings


import android.content.SharedPreferences
import android.content.SharedPreferences.OnSharedPreferenceChangeListener
import android.os.Bundle
import android.preference.PreferenceFragment
import info.alkor.whereareyou.impl.settings.SettingsKey
import info.alkor.whereareyoukt.R

class SettingsFragment : PreferenceFragment(), OnSharedPreferenceChangeListener {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        addPreferencesFromResource(R.xml.settings)
    }

    override fun onResume() {
        super.onResume()
        preferenceManager.sharedPreferences.let { prefs ->
            prefs.registerOnSharedPreferenceChangeListener(this)
            prefs.all.keys.onEach { key ->
                onSharedPreferenceChanged(prefs, key)
            }
        }
    }

    override fun onPause() {
        preferenceManager.sharedPreferences.unregisterOnSharedPreferenceChangeListener(this)
        super.onPause()
    }

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences?, key: String?) {
        if (sharedPreferences != null && key != null) {
            SettingsKey.fromString(key)?.let { typedKey ->
                preferenceScreen.findPreference(key)?.let { preference ->
                    val defaultValue = resources.getString(typedKey.defaultId)
                    val value = sharedPreferences.getString(key, defaultValue)
                    val summary = resources.getString(typedKey.summaryId, value)
                    preference.summary = summary
                }
            }
        }
    }
}