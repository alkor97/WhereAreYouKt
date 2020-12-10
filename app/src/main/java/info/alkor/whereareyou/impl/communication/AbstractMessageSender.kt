package info.alkor.whereareyou.impl.communication

import info.alkor.whereareyou.impl.context.AppContext
import info.alkor.whereareyou.model.action.LocationRequest
import info.alkor.whereareyou.model.action.LocationResponse
import info.alkor.whereareyou.model.action.Person
import info.alkor.whereareyou.model.action.SendingStatus

typealias SendingStatusCallback = (SendingStatus) -> Unit

abstract class AbstractMessageSender(private val context: AppContext) {

    private val requestFormatter by lazy { context.locationRequestParser }
    private val responseFormatter by lazy { context.locationResponseParser }

    fun send(request: LocationRequest, callback: SendingStatusCallback) {
        val message = requestFormatter.formatLocationRequest(request)
        doSend(request.from, message, callback)
    }

    fun send(response: LocationResponse, callback: SendingStatusCallback) {
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