package info.alkor.whereareyou.impl.communication

import info.alkor.whereareyou.api.communication.MessageReceiver
import info.alkor.whereareyou.api.context.AppContext
import info.alkor.whereareyou.model.communication.Message

class MessageReceiverImpl(private val context: AppContext) : MessageReceiver {
    override fun onReceive(message: Message) {
        context.locationRequestParser.parseLocationRequest(message.from, message.body)?.let {
            context.locationResponder.handleLocationRequest(it)
            return
        }
        context.locationResponseParser.parseLocationResponse(message.from, message.body)?.let {
            context.locationRequester.onLocationResponse(it)
            return
        }
    }
}