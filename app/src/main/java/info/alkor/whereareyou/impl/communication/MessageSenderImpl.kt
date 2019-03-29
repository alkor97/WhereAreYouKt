package info.alkor.whereareyou.impl.communication

import info.alkor.whereareyou.api.communication.MessageSender
import info.alkor.whereareyou.api.communication.SendingStatusCallback
import info.alkor.whereareyou.api.context.AppContext
import info.alkor.whereareyou.model.action.LocationRequest
import info.alkor.whereareyou.model.action.LocationResponse
import info.alkor.whereareyou.model.action.Person
import info.alkor.whereareyou.model.action.SendingStatus

abstract class MessageSenderImpl(private val context: AppContext) : MessageSender {

    private val requestFormatter by lazy { context.locationRequestParser }
    private val responseFormatter by lazy { context.locationResponseParser }

    override fun send(request: LocationRequest, callback: SendingStatusCallback) {
        val message = requestFormatter.formatLocationRequest(request)
        doSend(request.from, message, callback)
    }

    override fun send(response: LocationResponse, callback: SendingStatusCallback) {
        val message = responseFormatter.formatLocationResponse(response)
        doSend(response.person, message, callback)
    }

    private fun doSend(person: Person, message: String, callback: SendingStatusCallback) {
        if (canSendSms() && canReadPhoneState())
            sendMessage(person, message, callback)
        else
            callback(SendingStatus.SENDING_FAILED)
    }

    protected abstract fun sendMessage(person: Person, message: String, callback: SendingStatusCallback)

    private fun canSendSms() = context.permissionAccessor.isPermissionGranted(android.Manifest.permission.SEND_SMS)

    private fun canReadPhoneState() = context.permissionAccessor.isPermissionGranted(android.Manifest.permission.READ_PHONE_STATE)
}