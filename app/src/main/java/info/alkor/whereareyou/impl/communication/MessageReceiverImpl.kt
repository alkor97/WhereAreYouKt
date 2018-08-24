package info.alkor.whereareyou.impl.communication

import info.alkor.whereareyou.api.communication.MessageReceiver
import info.alkor.whereareyou.api.context.AppContext
import info.alkor.whereareyou.model.communication.Message

class MessageReceiverImpl(private val contex: AppContext) : MessageReceiver {
    override fun onReceive(message: Message) {
        contex.locationRequestParser.parseLocationRequest(message.from, message.body)?.let {
            contex.locationResponder.handleLocationRequest(it)
            return
        }
        contex.locationResponseParser.parseLocationResponse(message.from, message.body)?.let {
            contex.locationRequester.onLocationResponse(it)
            return
        }
    }
}