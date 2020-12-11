package info.alkor.whereareyou.impl.persistence

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.Transformations
import info.alkor.whereareyou.model.action.Person
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class PersonRepository(context: Context) {

    private val scope = CoroutineScope(Dispatchers.IO)
    private val persons: PersonDao by lazy { AppDatabase.getInstance(context).personRecords() }

    val all: LiveData<List<Person>> by lazy {
        Transformations.map(persons.all()) {
            it.map { it.toModel() }
        }
    }

    fun addPerson(person: Person) {
        scope.launch {
            persons.insert(person.toRecord())
        }
    }

    fun removePerson(person: Person) {
        scope.launch {
            persons.delete(person.toRecord())
        }
    }

    suspend fun isPersonRegistered(person: Person): Boolean = persons.getPersonByPhone(person.toRecord().phone) != null
}