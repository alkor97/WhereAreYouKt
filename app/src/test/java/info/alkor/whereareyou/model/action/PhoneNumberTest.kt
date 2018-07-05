package info.alkor.whereareyou.model.action

import org.junit.Assert
import org.junit.Test
import java.lang.IllegalArgumentException

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

    @Test(expected = IllegalArgumentException::class)
    fun verifyRejectingEmptyPhone() {
        PhoneNumber("")
    }

    @Test(expected = IllegalArgumentException::class)
    fun verifyRejectingNumberWithLetters() {
        PhoneNumber("123abc456")
    }

    @Test(expected = IllegalArgumentException::class)
    fun verifyRejectingPlusOnWrongPosition() {
        PhoneNumber("00+123456789")
    }

    @Test
    fun verifyAcceptingNumberInLocalFormat() {
        Assert.assertEquals("091461546978", PhoneNumber("091461546978").value)
    }
}