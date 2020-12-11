package info.alkor.whereareyou.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import info.alkor.whereareyou.impl.context.AppContext
import info.alkor.whereareyou.model.action.Person

class PersonListViewModel(application: Application) : AndroidViewModel(application) {

    private val appContext = application as AppContext
    private val personsRepository = appContext.personsRepository
    val persons = personsRepository.all

    fun addPerson(person: Person) = personsRepository.addPerson(person)
    fun removePerson(person: Person) = personsRepository.removePerson(person)
}