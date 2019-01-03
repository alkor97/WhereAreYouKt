package info.alkor.whereareyou

import android.preference.PreferenceManager
import android.support.test.InstrumentationRegistry
import android.support.test.runner.AndroidJUnit4
import info.alkor.whereareyou.impl.settings.SettingsAccess
import info.alkor.whereareyou.impl.settings.SettingsKey
import info.alkor.whereareyou.ui.PermissionRequester
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class StaticsTest {
    @Test
    fun testSettings() {
        val context = InstrumentationRegistry.getTargetContext()
        val access = SettingsAccess(PreferenceManager.getDefaultSharedPreferences(context), context.resources)

        for (key in SettingsKey.values()) {
            key.getSummary(access)
        }
    }

    @Test
    fun testPermissions() {
        val context = InstrumentationRegistry.getTargetContext()
        PermissionRequester(context)
    }
}