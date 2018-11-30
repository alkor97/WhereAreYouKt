package info.alkor.whereareyou.impl.communication

import info.alkor.whereareyou.api.communication.MessageSender
import info.alkor.whereareyou.api.communication.SendingStatusCallback
import info.alkor.whereareyou.api.context.AppContext
import info.alkor.whereareyou.model.action.LocationRequest
import info.alkor.whereareyou.model.action.LocationResponse
import info.alkor.whereareyou.model.action.Person

abstract class MessageSenderImpl(private val context: AppContext) : MessageSender {

    private val requestFormatter by lazy { context.locationRequestParser }
    private val responseFormatter by lazy { context.locationResponseParser }

    override fun send(request: LocationRequest, callback: SendingStatusCallback) {
        val message = requestFormatter.formatLocationRequest(request)
        sendMessage(request.person, message, callback)
    }

    override fun send(response: LocationResponse, callback: SendingStatusCallback) {
        val message = responseFormatter.formatLocationResponse(response)
        sendMessage(response.person, message, callback)
    }

    protected abstract fun sendMessage(person: Person, message: String, callback: SendingStatusCallback)
}