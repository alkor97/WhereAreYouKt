package info.alkor.whereareyou.impl.communication

import info.alkor.whereareyou.api.communication.MessageReceiver
import info.alkor.whereareyou.api.context.AppContext
import info.alkor.whereareyou.model.action.Person

class MessageReceiverImpl(private val context: AppContext) : MessageReceiver {

    private val requestParser by lazy { context.locationRequestParser }
    private val responseParser by lazy { context.locationResponseParser }

    private val locationResponder by lazy { context.locationResponder }
    private val locationRequester by lazy { context.locationRequester }

    override fun onReceive(from: Person, message: String) {
        requestParser.parseLocationRequest(from, message)?.let {
            locationResponder.handleLocationRequest(it)
            return
        }
        responseParser.parseLocationResponse(from, message)?.let {
            locationRequester.onLocationResponse(it)
            return
        }
    }
}