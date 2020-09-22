package info.alkor.whereareyou.impl.persistence

import android.arch.lifecycle.MutableLiveData
import info.alkor.whereareyou.api.persistence.PersonRepository
import info.alkor.whereareyou.model.action.Person

class PersonRepositoryImpl : PersonRepository {
    override val all = MutableLiveData<List<Person>>()
    private val data = InMemoryPersonStorage()

    override fun addPerson(person: Person) {
        data.access { session -> session.add(person) }
        postUpdates()
    }

    override fun removePerson(person: Person) {
        data.access { session -> session.remove(person) }
        postUpdates()
    }

    private fun postUpdates() {
        data.postUpdates(all)
    }
}