package info.alkor.whereareyou.service.android

import androidx.test.platform.app.InstrumentationRegistry
import info.alkor.whereareyou.impl.service.android.LocationService
import info.alkor.whereareyou.model.action.Person
import info.alkor.whereareyou.model.action.PhoneNumber
import org.junit.Assert.assertEquals
import org.junit.Test

class LocationIntentConversionTest {
    @Test
    fun testConversionWorksCorrectly() {
        val person = Person(PhoneNumber("+48123456789"), "Mietek")

        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val intent = LocationService.toIntent(context, person)
        val actualPerson = LocationService.fromIntent(intent)

        assertEquals(person, actualPerson)
    }
}