package info.alkor.whereareyou.ui

import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData

class LiveDataFiltering<K, V>(private val stored: LiveData<List<V>>, private val keyOf: (V) -> K) {

    private val markedForRemoval = HashSet<K>()
    private val version = MutableLiveData<Int>()

    val filtered: LiveData<List<V>> by lazy {
        val mediator = MediatorLiveData<List<V>>()

        mediator.addSource(stored) { stored ->
            markedForRemoval.retainAll(stored.map { keyOf(it) })
            mediator.value = stored.filter { value ->
                shouldKeep(keyOf(value), markedForRemoval)
            }
        }

        mediator.addSource(version) {
            mediator.value = stored.value?.filter { value ->
                shouldKeep(keyOf(value), markedForRemoval)
            }
        }

        mediator
    }

    fun dropRemovalMarkSilently(key: K) = markedForRemoval.remove(key)

    fun markForRemoval(key: K, marked: Boolean) {
        val updated = if (marked) markedForRemoval.add(key) else dropRemovalMarkSilently(key)
        if (updated) {
            version.postValue(1 + (version.value ?: 0))
        }
    }

    private fun shouldKeep(key: K, markedForRemoval: Set<K>?) = if (markedForRemoval != null) !markedForRemoval.contains(key) else true
}