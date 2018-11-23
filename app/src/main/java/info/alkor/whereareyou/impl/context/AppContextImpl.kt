package info.alkor.whereareyou.impl.context

import android.app.Application
import info.alkor.whereareyou.api.context.AppContext
import info.alkor.whereareyou.common.seconds
import info.alkor.whereareyou.impl.action.LocationRequestParserImpl
import info.alkor.whereareyou.impl.action.LocationRequesterImpl
import info.alkor.whereareyou.impl.action.LocationResponderImpl
import info.alkor.whereareyou.impl.action.LocationResponseParserImpl
import info.alkor.whereareyou.impl.communication.MessageReceiverImpl
import info.alkor.whereareyou.impl.contact.android.ContactProviderImpl
import info.alkor.whereareyou.impl.location.android.LocationProviderImpl
import info.alkor.whereareyou.impl.settings.SettingsImpl
import info.alkor.whereareyou.model.action.DurationCompleted
import info.alkor.whereareyou.model.action.DurationProgress
import info.alkor.whereareyou.model.action.OwnLocationResponse
import kotlinx.coroutines.experimental.Job
import kotlinx.coroutines.experimental.channels.BroadcastChannel
import kotlinx.coroutines.experimental.delay
import kotlinx.coroutines.experimental.launch
import java.util.*
import kotlin.concurrent.schedule

class AppContextImpl : Application(), AppContext {
    override val contactProvider by lazy { ContactProviderImpl(this) }
    override val locationRequestParser by lazy { LocationRequestParserImpl() }
    override val locationResponseParser by lazy { LocationResponseParserImpl() }
    override val locationRequester by lazy { LocationRequesterImpl(this) }
    override val locationResponder by lazy { LocationResponderImpl(this) }
    override val messageReceiver by lazy { MessageReceiverImpl(this) }
    override val locationProvider by lazy { LocationProviderImpl(this) }
    override val settings by lazy { SettingsImpl(this) }
    override val locationResponsesChannel = BroadcastChannel<Any>(5)

    override fun requestLocation() {
        val queryTimeout = settings.getLocationQueryTimeout()

        val timer = Timer()
        var counter = 0
        val task = timer.schedule(seconds(1).toMillis(), seconds(1).toMillis()) {
            launch {
                locationResponsesChannel.send(DurationProgress(seconds(++counter)))
            }
        }

        val job = Job()
        locationProvider.getLocation(queryTimeout) { location, final ->
            launch(parent = job) {
                locationResponsesChannel.send(OwnLocationResponse(location, final))
                if (final) {
                    task.cancel()
                    delay(seconds(1).toMillis())
                    locationResponsesChannel.send(DurationCompleted(seconds(counter)))
                    job.cancel()
                }
            }
        }
    }
}