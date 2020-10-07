package info.alkor.whereareyou.impl.persistence

import info.alkor.whereareyou.model.action.Person
import info.alkor.whereareyou.model.action.PhoneNumber
import org.junit.Assert.assertEquals
import org.junit.Test

class ConvertersTest {
    @Test
    fun testPersonConversion() {
        val data = arrayListOf(
                Person(PhoneNumber("+48123456789"), "Mariusz"),
                Person(PhoneNumber("+48987654321")))
        data.forEach {
            assertEquals(it, it.toRecord().toModel())
        }
    }
}