package info.alkor.whereareyou.impl.persistence

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.Transformations
import info.alkor.whereareyou.api.persistence.PersonRepository
import info.alkor.whereareyou.model.action.Person
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class PersonRepositoryImpl(context: Context) : PersonRepository {
    private val db: AppDatabase by lazy { AppDatabase.getInstance(context) }

    private val persons: PersonDao by lazy { db.personRecords() }

    override val all: LiveData<List<Person>> by lazy {
        Transformations.map(persons.all()) {
            it.map { it.toModel() }
        }
    }

    override fun addPerson(person: Person) {
        GlobalScope.launch {
            persons.insert(person.toRecord())
        }
    }

    override fun removePerson(person: Person) {
        GlobalScope.launch {
            persons.delete(person.toRecord())
        }
    }
}