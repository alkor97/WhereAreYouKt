package info.alkor.whereareyou.impl.persistence

import androidx.lifecycle.MutableLiveData
import info.alkor.whereareyou.model.action.Person

class InMemoryPersonStorage {
    private val data = ArrayList<Person>()

    fun postUpdates(liveData: MutableLiveData<List<Person>>) {
        liveData.postValue(data)
    }

    @Synchronized
    fun <E> access(action: (Session) -> E): E = action(Session(data))

    inner class Session(private val data: ArrayList<Person>) {
        fun add(person: Person) = data.add(person)
        fun remove(person: Person) = data.remove(person)
    }
}