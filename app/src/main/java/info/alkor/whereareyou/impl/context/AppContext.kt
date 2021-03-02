package info.alkor.whereareyou.impl.context

import android.app.Application
import android.util.Log
import info.alkor.whereareyou.impl.action.LocationRequestParser
import info.alkor.whereareyou.impl.action.LocationRequester
import info.alkor.whereareyou.impl.action.LocationResponder
import info.alkor.whereareyou.impl.action.LocationResponseParser
import info.alkor.whereareyou.impl.communication.android.SmsSender
import info.alkor.whereareyou.impl.location.android.LocationProviderImpl
import info.alkor.whereareyou.impl.persistence.LocationActionRepository
import info.alkor.whereareyou.impl.persistence.PersonRepository
import info.alkor.whereareyou.impl.settings.Settings
import info.alkor.whereareyou.model.action.LocationRequest
import info.alkor.whereareyou.model.action.MessageId
import info.alkor.whereareyou.model.action.Person
import info.alkor.whereareyou.model.action.PhoneNumber
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

class AppContext : Application() {
    val locationRequestParser by lazy { LocationRequestParser(this) }
    val locationResponseParser by lazy { LocationResponseParser(this) }
    private val locationRequester by lazy { LocationRequester(this) }
    private val locationResponder by lazy { LocationResponder(this) }
    val locationProvider by lazy { LocationProviderImpl(this) }
    val settings by lazy { Settings(this) }
    val messageSender by lazy { SmsSender(this) }
    val permissionAccessor by lazy { PermissionAccessor(this) }
    val actionsRepository by lazy { LocationActionRepository(this) }
    val personsRepository by lazy { PersonRepository(this) }
    private val scope = CoroutineScope(Dispatchers.IO)
    private val awaiting = HashMap<MessageId, Mutex>()
    private val loggingTag = "ctx"

    @ExperimentalCoroutinesApi
    fun handleOwnLocation() = scope.launch {
        val startTime = now()
        handleLocationRequest(LocationRequest(Person(PhoneNumber.OWN)))
        val duration = now() - startTime
        Log.i(loggingTag, "own location request handled in $duration seconds")
    }

    fun requestLocationOf(person: Person) = scope.launch {
        val startTime = now()
        val requestId = locationRequester.requestLocationOf(person)
        blockUntilReleased(requestId)
        val duration = now() - startTime
        Log.i(loggingTag, "locally-initiated location request handled in $duration seconds")
    }

    private suspend fun blockUntilReleased(requestId: MessageId) {
        awaiting[requestId] = Mutex(locked = true)
        awaiting[requestId]?.withLock { }
        awaiting.remove(requestId)
    }

    @ExperimentalCoroutinesApi
    fun handleMessage(from: Person, message: String) = scope.launch(Dispatchers.IO) {
        if (!tryHandleLocationRequest(from, message)) {
            tryHandleLocationResponse(from, message)
        }
    }

    @ExperimentalCoroutinesApi
    private suspend fun tryHandleLocationRequest(from: Person, message: String): Boolean {
        val startTime = now()
        locationRequestParser.parseLocationRequest(from, message)?.let {
            if (personsRepository.isPersonRegistered(it.from)) {
                handleLocationRequest(it)
                val duration = now() - startTime
                Log.i(loggingTag, "remotely-initiated location request handled in $duration seconds")
            }
            return true
        }
        return false
    }

    @ExperimentalCoroutinesApi
    private suspend fun handleLocationRequest(request: LocationRequest) = locationResponder.handleLocationRequest(request)

    private suspend fun tryHandleLocationResponse(from: Person, message: String) {
        val response = locationResponseParser.parseLocationResponse(from, message)
        val requestId = locationRequester.onLocationResponse(response)
        if (requestId != null) {
            awaiting[requestId]?.unlock()
        }
    }

    private fun now() = System.currentTimeMillis() / 1000
}