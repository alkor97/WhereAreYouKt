package info.alkor.whereareyou.ui

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import info.alkor.whereareyou.impl.context.AppContext
import info.alkor.whereareyou.impl.persistence.PersonRepository
import info.alkor.whereareyou.model.action.Person
import info.alkor.whereareyou.model.action.PhoneNumber
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

class PersonListViewModelTest {

    @get:Rule
    val instantExecutorRule = InstantTaskExecutorRule()

    private val repo = mockk<PersonRepository>()

    private val instance: PersonListViewModel by lazy {
        val storage = LiveDataStorage<Person>()

        every { repo.all } returns storage.all
        every { repo.addPerson(any()) } answers { storage.add(firstArg()) }
        every { repo.removePerson(any()) } answers { storage.remove(firstArg()) }

        val app = mockk<AppContext>()
        every { app.personsRepository } returns repo

        PersonListViewModel(app)
    }

    @Test
    fun testSimulateRemovalAndRestore() {
        val person1 = Person(PhoneNumber("+49123456789"), "Hans")
        val person2 = Person(PhoneNumber("+48123456789"), "Jan")
        val person3 = Person(PhoneNumber("+47123456789"), "Johan")
        val allPersons = arrayListOf(person1, person2, person3)
        allPersons.forEach { instance.addPerson(it) }

        assertEquals("wrong repo entries",
                allPersons, instance.persons.getOrAwaitValue())

        instance.removePerson(person2, commit = false)
        assertEquals("only not marked for removal persons should be visible",
                arrayListOf(person1, person3), instance.persons.getOrAwaitValue())

        instance.restorePerson(person2)
        assertEquals("only not marked for removal persons should be visible",
                allPersons, instance.persons.getOrAwaitValue())
    }

    @Test
    fun testSimulateRemovalAndRemove() {
        val person1 = Person(PhoneNumber("+49123456789"), "Hans")
        val person2 = Person(PhoneNumber("+48123456789"), "Jan")
        val person3 = Person(PhoneNumber("+47123456789"), "Johan")
        val allPersons = arrayListOf(person1, person2, person3)
        allPersons.forEach { instance.addPerson(it) }

        assertEquals("wrong repo entries",
                allPersons, instance.persons.getOrAwaitValue())

        instance.removePerson(person2, commit = false)
        assertEquals("only not marked for removal persons should be visible",
                arrayListOf(person1, person3), instance.persons.getOrAwaitValue())

        instance.removePerson(person2, commit = true)
        assertEquals("only not removed persons should be visible",
                arrayListOf(person1, person3), instance.persons.getOrAwaitValue())
        verify { repo.removePerson(person2) }
    }

    @Test
    fun testSimulateRemoveAndAdd() {
        val person1 = Person(PhoneNumber("+49123456789"), "Hans")

        instance.addPerson(person1)
        assertEquals(arrayListOf(person1), instance.persons.getOrAwaitValue())

        instance.removePerson(person1, commit = false)
        assertEquals(arrayListOf<Person>(), instance.persons.getOrAwaitValue())

        instance.addPerson(person1)
        assertEquals(arrayListOf(person1), instance.persons.getOrAwaitValue())
    }
}