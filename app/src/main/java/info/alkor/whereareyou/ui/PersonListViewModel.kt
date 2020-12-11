package info.alkor.whereareyou.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import info.alkor.whereareyou.impl.context.AppContext
import info.alkor.whereareyou.model.action.Person

class PersonListViewModel(application: Application) : AndroidViewModel(application) {

    private val appContext = application as AppContext
    private val repository = appContext.personsRepository
    val persons = repository.all

    fun addPerson(person: Person) = repository.addPerson(person)
    fun removePerson(person: Person) = repository.removePerson(person)
}