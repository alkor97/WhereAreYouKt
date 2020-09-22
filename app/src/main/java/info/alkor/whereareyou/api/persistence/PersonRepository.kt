package info.alkor.whereareyou.api.persistence

import android.arch.lifecycle.LiveData
import info.alkor.whereareyou.model.action.Person

interface PersonRepository {
    val all: LiveData<List<Person>>
    fun addPerson(person: Person)
    fun removePerson(person: Person)
}