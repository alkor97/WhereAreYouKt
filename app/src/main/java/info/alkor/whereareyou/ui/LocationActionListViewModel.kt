package info.alkor.whereareyou.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import info.alkor.whereareyou.impl.context.AppContext
import info.alkor.whereareyou.model.action.MessageId

class LocationActionListViewModel(application: Application) : AndroidViewModel(application) {

    private val appContext = application as AppContext
    private val repository = appContext.actionsRepository
    val actions = repository.all

    fun removeAction(id: MessageId) {
        appContext.actionsRepository.remove(id)
    }
}