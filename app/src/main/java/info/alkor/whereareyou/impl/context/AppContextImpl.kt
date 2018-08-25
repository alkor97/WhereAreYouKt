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
import info.alkor.whereareyou.model.action.OwnLocationResponse
import info.alkor.whereareyou.model.location.Location
import kotlinx.coroutines.experimental.channels.Channel
import kotlinx.coroutines.experimental.channels.consumeEach
import kotlinx.coroutines.experimental.launch

class AppContextImpl : Application(), AppContext {
    override val contactProvider by lazy { ContactProviderImpl(this) }
    override val locationRequestParser by lazy { LocationRequestParserImpl() }
    override val locationResponseParser by lazy { LocationResponseParserImpl() }
    override val locationRequester by lazy { LocationRequesterImpl(this) }
    override val locationResponder by lazy { LocationResponderImpl(this) }
    override val messageReceiver by lazy { MessageReceiverImpl(this) }
    override val locationProvider by lazy { LocationProviderImpl(this) }
    override val settings by lazy { SettingsImpl(this) }
    override val locationChannel by lazy { Channel<OwnLocationResponse>() }

    override fun requestLocation() {
        val queryTimeout = settings.getLocationQueryTimeout()
        val tmp = Channel<Location>()
        launch {
            tmp.consumeEach {
                locationChannel.send(OwnLocationResponse(it, false))
            }
        }
        launch {
            val response = locationProvider.getLocation(queryTimeout, tmp)
            locationChannel.send(OwnLocationResponse(response, true))
            tmp.cancel()
        }
    }
}