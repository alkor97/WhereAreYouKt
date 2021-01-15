package info.alkor.whereareyou.impl.communication

import info.alkor.whereareyou.impl.context.AppContext
import info.alkor.whereareyou.model.action.LocationRequest
import info.alkor.whereareyou.model.action.LocationResponse
import info.alkor.whereareyou.model.action.Person
import info.alkor.whereareyou.model.action.SendingStatus
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.launch

abstract class AbstractMessageSender(private val context: AppContext) {

    private val requestFormatter by lazy { context.locationRequestParser }
    private val responseFormatter by lazy { context.locationResponseParser }

    suspend fun send(request: LocationRequest): Channel<SendingStatus> {
        val message = requestFormatter.formatLocationRequest()
        return doSend(request.from, message)
    }

    suspend fun send(response: LocationResponse): Channel<SendingStatus> {
        val message = responseFormatter.formatLocationResponse(response)
        return doSend(response.person, message)
    }

    private suspend fun doSend(person: Person, message: String): Channel<SendingStatus> {
        val channel = Channel<SendingStatus>()

        val scope = CoroutineScope(Dispatchers.IO)
        scope.launch {
            if (canSendSms() && canReadPhoneState())
                sendMessage(person, message, channel)
            else
                channel.send(SendingStatus.SENDING_FAILED)
        }

        return channel
    }

    protected abstract suspend fun sendMessage(person: Person, message: String, channel: Channel<SendingStatus>)

    private fun canSendSms() = context.permissionAccessor.isPermissionGranted(android.Manifest.permission.SEND_SMS)

    private fun canReadPhoneState() = context.permissionAccessor.isPermissionGranted(android.Manifest.permission.READ_PHONE_STATE)
}