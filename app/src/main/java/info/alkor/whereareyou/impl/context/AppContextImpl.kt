package info.alkor.whereareyou.impl.context

import android.app.Application
import info.alkor.whereareyou.api.context.AppContext
import info.alkor.whereareyou.impl.action.LocalLocationResponder
import info.alkor.whereareyou.impl.action.LocationRequestParserImpl
import info.alkor.whereareyou.impl.action.LocationRequesterImpl
import info.alkor.whereareyou.impl.action.LocationResponseParserImpl
import info.alkor.whereareyou.impl.communication.MessageReceiverImpl
import info.alkor.whereareyou.impl.communication.android.SmsSender
import info.alkor.whereareyou.impl.contact.android.ContactProviderImpl
import info.alkor.whereareyou.impl.location.android.LocationProviderImpl
import info.alkor.whereareyou.impl.persistence.SimpleLocationRequestPersistence
import info.alkor.whereareyou.impl.settings.SettingsImpl
import info.alkor.whereareyou.model.action.LocationRequest
import info.alkor.whereareyou.model.action.Person
import info.alkor.whereareyou.model.action.PhoneNumber

class AppContextImpl : Application(), AppContext {
    override val contactProvider by lazy { ContactProviderImpl(this) }
    override val locationRequestParser by lazy { LocationRequestParserImpl(this) }
    override val locationResponseParser by lazy { LocationResponseParserImpl(this) }
    override val locationRequester by lazy { LocationRequesterImpl(this) }
    override val locationResponder by lazy { LocalLocationResponder(this) }
    override val messageReceiver by lazy { MessageReceiverImpl(this) }
    override val locationProvider by lazy { LocationProviderImpl(this) }
    override val settings by lazy { SettingsImpl(this) }
    override val messageSender by lazy { SmsSender(this) }
    override val locationRequestPersistence by lazy { SimpleLocationRequestPersistence() }

    override fun requestLocation() {
        locationResponder.handleLocationRequest(LocationRequest(Person(PhoneNumber.OWN)))
    }
}