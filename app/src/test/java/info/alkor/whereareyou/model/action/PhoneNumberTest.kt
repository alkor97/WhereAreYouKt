package info.alkor.whereareyou.model.action

import org.junit.Assert
import org.junit.Test

class PhoneNumberTest {
    @Test
    fun verifyNormalization() {
        Assert.assertEquals("+48123456789", PhoneNumber("   +4 8 123 456  789 ").value)
        Assert.assertEquals("+48123456789", PhoneNumber("0048123456789").value)
        Assert.assertEquals("+48123456789", PhoneNumber("00048123456789").value)
        Assert.assertEquals("+48123456789", PhoneNumber("+0048123456789").value)
    }

    @Test
    fun verifyHumanReadableForm() {
        Assert.assertEquals("+48 123 456 789", PhoneNumber("+48123456789").toHumanReadable())
        Assert.assertEquals("+480 123 456 789", PhoneNumber("+480123456789").toHumanReadable())
    }

    @Test
    fun verifyExternalForm() {
        Assert.assertEquals("0048123456789", PhoneNumber("+48123456789").toExternalForm())
    }

    @Test
    fun verifyAcceptingNumberInLocalFormat() {
        Assert.assertEquals("091461546978", PhoneNumber("091461546978").value)
    }

    @Test
    fun verifyOwnPhoneNumber() {
        Assert.assertTrue(PhoneNumber.OWN.value.isEmpty())
    }
}