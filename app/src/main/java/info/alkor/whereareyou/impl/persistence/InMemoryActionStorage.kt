package info.alkor.whereareyou.impl.persistence

import android.arch.lifecycle.MutableLiveData
import info.alkor.whereareyou.model.action.LocationAction
import info.alkor.whereareyou.model.action.LocationResponse
import info.alkor.whereareyou.model.action.MessageId
import java.util.concurrent.atomic.AtomicLong

class InMemoryActionStorage {
    private val data = ArrayList<LocationAction>()
    private val idGenerator = AtomicLong(0)

    @Synchronized
    fun <E> access(action: (Session) -> E): E = action(Session(data))

    fun postUpdates(liveData: MutableLiveData<List<LocationAction>>) {
        liveData.postValue(data)
    }

    fun nextMessageId() = MessageId(idGenerator.incrementAndGet())

    inner class Session(private val data: ArrayList<LocationAction>) {

        private fun indexed() = data.mapIndexed { index, element -> Pair(index, element) }

        fun findById(id: MessageId) = indexed().filter { it.second.id == id }

        fun findMatching(response: LocationResponse) = indexed()
                .filter { (_, action) ->
                    action.person == response.person
                            && !action.final
                            && action.location != response.location
                }.sortedBy { it.second.id }

        fun removeAt(index: Int) = data.removeAt(index)

        fun addAtFront(action: LocationAction) = data.add(0, action)

        fun setAt(index: Int, action: LocationAction) = data.set(index, action)
    }
}
