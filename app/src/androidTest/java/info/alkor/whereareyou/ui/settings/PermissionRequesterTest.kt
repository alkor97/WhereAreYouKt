package info.alkor.whereareyou.ui.settings

import androidx.test.platform.app.InstrumentationRegistry
import info.alkor.whereareyou.ui.PermissionRequester
import org.junit.Test

class PermissionRequesterTest {
    @Test
    fun testAllPermissionsRationalIsPresent() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        PermissionRequester(context)
    }
}