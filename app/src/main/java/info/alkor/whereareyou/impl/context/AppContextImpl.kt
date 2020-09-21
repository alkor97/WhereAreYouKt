package info.alkor.whereareyou.impl.context

import android.app.Application
import info.alkor.whereareyou.api.context.AppContext
import info.alkor.whereareyou.impl.action.LocationRequestParserImpl
import info.alkor.whereareyou.impl.action.LocationRequesterImpl
import info.alkor.whereareyou.impl.action.LocationResponderImpl
import info.alkor.whereareyou.impl.action.LocationResponseParserImpl
import info.alkor.whereareyou.impl.communication.MessageReceiverImpl
import info.alkor.whereareyou.impl.communication.android.SmsSender
import info.alkor.whereareyou.impl.contact.android.ContactProviderImpl
import info.alkor.whereareyou.impl.location.android.LocationProviderImpl
import info.alkor.whereareyou.impl.persistence.LocationActionRepositoryImpl
import info.alkor.whereareyou.impl.settings.SettingsImpl
import info.alkor.whereareyou.model.action.LocationRequest
import info.alkor.whereareyou.model.action.Person
import info.alkor.whereareyou.model.action.PhoneNumber

class AppContextImpl : Application(), AppContext {
    override val contactProvider by lazy { ContactProviderImpl(this) }
    override val locationRequestParser by lazy { LocationRequestParserImpl(this) }
    override val locationResponseParser by lazy { LocationResponseParserImpl(this) }
    override val locationRequester by lazy { LocationRequesterImpl(this) }
    override val locationResponder by lazy { LocationResponderImpl(this) }
    override val messageReceiver by lazy { MessageReceiverImpl(this) }
    override val locationProvider by lazy { LocationProviderImpl(this) }
    override val settings by lazy { SettingsImpl(this) }
    override val messageSender by lazy { SmsSender(this) }
    override val permissionAccessor by lazy { PermissionAccessorImpl(this) }
    override val actionsRepository by lazy { LocationActionRepositoryImpl() }

    override fun requestMyLocation() = locationResponder.handleLocationRequest(
            LocationRequest(Person(PhoneNumber.OWN)))

    override fun requestLocationOf(person: Person) = locationRequester.requestLocationOf(person)
}