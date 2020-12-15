package info.alkor.whereareyou

import android.preference.PreferenceManager
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import info.alkor.whereareyou.impl.settings.SettingsAccess
import info.alkor.whereareyou.impl.settings.SettingsKey
import info.alkor.whereareyou.ui.PermissionRequester
import org.junit.Assert
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class StaticsTest {
    @Test
    fun testSettings() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val access = SettingsAccess(PreferenceManager.getDefaultSharedPreferences(context), context.resources)

        for (key in SettingsKey.values()) {
            key.getSummary(access)
        }
    }

    @Test
    fun testPermissions() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        PermissionRequester(context)
    }

    @Test
    fun testPresenterUrlConstruction() {
        val resources = InstrumentationRegistry.getInstrumentation().targetContext.resources
        Assert.assertNotNull(resources.getString(R.string.host))
        Assert.assertNotNull(resources.getString(R.string.pathPrefix))
        Assert.assertNotNull(resources.getString(R.string.scheme))
    }
}
