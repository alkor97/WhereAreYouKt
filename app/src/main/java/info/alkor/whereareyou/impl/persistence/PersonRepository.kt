package info.alkor.whereareyou.impl.persistence

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.Transformations
import info.alkor.whereareyou.model.action.Person
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class PersonRepository(context: Context) {
    private val db: AppDatabase by lazy { AppDatabase.getInstance(context) }

    private val persons: PersonDao by lazy { db.personRecords() }

    val all: LiveData<List<Person>> by lazy {
        Transformations.map(persons.all()) {
            it.map { it.toModel() }
        }
    }

    fun addPerson(person: Person) {
        GlobalScope.launch {
            persons.insert(person.toRecord())
        }
    }

    fun removePerson(person: Person) {
        GlobalScope.launch {
            persons.delete(person.toRecord())
        }
    }
}