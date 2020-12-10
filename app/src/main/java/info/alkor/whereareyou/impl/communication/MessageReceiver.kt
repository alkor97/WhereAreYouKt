package info.alkor.whereareyou.impl.communication

import info.alkor.whereareyou.impl.context.AppContext
import info.alkor.whereareyou.model.action.Person

class MessageReceiver(private val context: AppContext) {

    private val requestParser by lazy { context.locationRequestParser }
    private val responseParser by lazy { context.locationResponseParser }

    private val locationResponder by lazy { context.locationResponder }
    private val locationRequester by lazy { context.locationRequester }

    fun onReceive(from: Person, message: String) {
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