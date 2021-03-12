package info.alkor.whereareyou.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import info.alkor.whereareyou.impl.context.AppContext
import info.alkor.whereareyou.model.action.MessageId

class LocationActionListViewModel(application: Application) : AndroidViewModel(application) {

    private val appContext = application as AppContext
    private val repository = appContext.actionsRepository
    private val filtering = LiveDataFiltering(repository.all) { it.id }
    val actions = filtering.filtered

    fun removeAction(id: MessageId, commit: Boolean = true) {
        if (commit) {
            appContext.actionsRepository.remove(id)
            filtering.dropRemovalMarkSilently(id)
        } else {
            filtering.markForRemoval(id, true)
        }
    }

    fun restoreAction(id: MessageId) {
        filtering.markForRemoval(id, false)
    }
}