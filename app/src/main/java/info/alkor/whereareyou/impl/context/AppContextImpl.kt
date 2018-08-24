package info.alkor.whereareyou.impl.context

import android.app.Application
import info.alkor.whereareyou.api.context.AppContext
import info.alkor.whereareyou.impl.action.LocationRequestParserImpl
import info.alkor.whereareyou.impl.action.LocationRequesterImpl
import info.alkor.whereareyou.impl.action.LocationResponderImpl
import info.alkor.whereareyou.impl.action.LocationResponseParserImpl
import info.alkor.whereareyou.impl.communication.MessageReceiverImpl
import info.alkor.whereareyou.impl.contact.android.ContactProviderImpl
import info.alkor.whereareyou.impl.location.android.LocationProviderImpl
import info.alkor.whereareyou.impl.settings.SettingsImpl

class AppContextImpl : Application(), AppContext {
    override val contactProvider by lazy { ContactProviderImpl(this) }
    override val locationRequestParser by lazy { LocationRequestParserImpl() }
    override val locationResponseParser by lazy { LocationResponseParserImpl() }
    override val locationRequester by lazy { LocationRequesterImpl(this) }
    override val locationResponder by lazy { LocationResponderImpl(this) }
    override val messageReceiver by lazy { MessageReceiverImpl(this) }
    override val locationProvider by lazy { LocationProviderImpl(this) }
    override val settings by lazy { SettingsImpl(this) }
}