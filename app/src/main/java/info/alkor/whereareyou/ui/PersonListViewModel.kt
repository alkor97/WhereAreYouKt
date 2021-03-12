package info.alkor.whereareyou.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import info.alkor.whereareyou.impl.context.AppContext
import info.alkor.whereareyou.model.action.Person

class PersonListViewModel(application: Application) : AndroidViewModel(application) {

    private val appContext = application as AppContext
    private val repository = appContext.personsRepository
    private val filtering = LiveDataFiltering(repository.all) { it }
    val persons = filtering.filtered

    fun addPerson(person: Person) {
        repository.addPerson(person)
        filtering.markForRemoval(person, false)
    }

    fun removePerson(person: Person, commit: Boolean = true) {
        if (commit) {
            repository.removePerson(person)
        }
        filtering.markForRemoval(person, !commit)
    }

    fun restorePerson(person: Person) {
        filtering.markForRemoval(person, false)
    }
}