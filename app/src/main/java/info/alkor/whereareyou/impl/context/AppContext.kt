package info.alkor.whereareyou.impl.context

import android.app.Application
import info.alkor.whereareyou.impl.action.LocationRequestParser
import info.alkor.whereareyou.impl.action.LocationRequester
import info.alkor.whereareyou.impl.action.LocationResponder
import info.alkor.whereareyou.impl.action.LocationResponseParser
import info.alkor.whereareyou.impl.communication.MessageReceiver
import info.alkor.whereareyou.impl.communication.android.SmsSender
import info.alkor.whereareyou.impl.contact.android.ContactProvider
import info.alkor.whereareyou.impl.location.android.LocationProviderImpl
import info.alkor.whereareyou.impl.persistence.LocationActionRepository
import info.alkor.whereareyou.impl.persistence.PersonRepository
import info.alkor.whereareyou.impl.settings.Settings
import info.alkor.whereareyou.model.action.LocationRequest
import info.alkor.whereareyou.model.action.Person
import info.alkor.whereareyou.model.action.PhoneNumber
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class AppContext : Application() {
    val contactProvider by lazy { ContactProvider(this) }
    val locationRequestParser by lazy { LocationRequestParser(this) }
    val locationResponseParser by lazy { LocationResponseParser(this) }
    val locationRequester by lazy { LocationRequester(this) }
    val locationResponder by lazy { LocationResponder(this) }
    val messageReceiver by lazy { MessageReceiver(this) }
    val locationProvider by lazy { LocationProviderImpl(this) }
    val settings by lazy { Settings(this) }
    val messageSender by lazy { SmsSender(this) }
    val permissionAccessor by lazy { PermissionAccessor(this) }
    val actionsRepository by lazy { LocationActionRepository(this) }
    val personsRepository by lazy { PersonRepository(this) }
    private val scope = CoroutineScope(Dispatchers.IO)

    fun requestMyLocation() = scope.launch {
        locationResponder.handleLocationRequest(LocationRequest(Person(PhoneNumber.OWN)))
    }

    fun requestLocationOf(person: Person) = locationRequester.requestLocationOf(person)
}