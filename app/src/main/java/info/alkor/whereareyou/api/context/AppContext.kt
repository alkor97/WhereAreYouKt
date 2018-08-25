package info.alkor.whereareyou.api.context

import info.alkor.whereareyou.api.action.LocationRequestParser
import info.alkor.whereareyou.api.action.LocationRequester
import info.alkor.whereareyou.api.action.LocationResponder
import info.alkor.whereareyou.api.action.LocationResponseParser
import info.alkor.whereareyou.api.communication.MessageReceiver
import info.alkor.whereareyou.api.contact.ContactProvider
import info.alkor.whereareyou.api.location.LocationProvider
import info.alkor.whereareyou.api.settings.Settings
import info.alkor.whereareyou.model.action.OwnLocationResponse
import kotlinx.coroutines.experimental.channels.Channel

interface AppContext {
    val contactProvider: ContactProvider
    val locationRequestParser: LocationRequestParser
    val locationResponseParser: LocationResponseParser
    val locationRequester: LocationRequester
    val locationResponder: LocationResponder
    val messageReceiver: MessageReceiver
    val locationProvider: LocationProvider
    val settings: Settings
    val locationChannel: Channel<OwnLocationResponse>

    fun requestLocation()
}