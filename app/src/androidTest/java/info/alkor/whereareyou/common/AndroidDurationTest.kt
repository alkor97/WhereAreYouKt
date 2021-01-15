package info.alkor.whereareyou.common

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import info.alkor.whereareyou.impl.settings.toElapsedString
import info.alkor.whereareyou.impl.settings.toString
import org.hamcrest.CoreMatchers.containsString
import org.junit.Assert.assertThat
import org.junit.Test
import org.junit.runner.RunWith
import java.util.concurrent.TimeUnit

@RunWith(AndroidJUnit4::class)
class AndroidDurationTest {
    @Test
    fun testToString() {
        val resources = InstrumentationRegistry.getInstrumentation().targetContext.resources

        for (unit in TimeUnit.values()) {
            val value = Duration(3, unit)
            val text = value.toString(resources)

            assertThat(text, containsString("3"))
            assertThat(text, containsString(unit.name.toLowerCase()))
        }
    }

    @Test
    fun testElapsedString() {
        val resources = InstrumentationRegistry.getInstrumentation().targetContext.resources

        for (unit in TimeUnit.values()) {
            val value = Duration(3, unit)
            val text = value.toElapsedString(resources)

            assertThat(text, containsString("Elapsed"))
            assertThat(text, containsString("3"))
            assertThat(text, containsString(unit.name.toLowerCase()))
        }
    }
}