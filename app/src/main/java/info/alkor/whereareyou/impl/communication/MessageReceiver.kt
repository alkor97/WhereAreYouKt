package info.alkor.whereareyou.impl.communication

import info.alkor.whereareyou.impl.context.AppContext
import info.alkor.whereareyou.model.action.Person
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MessageReceiver(private val context: AppContext) {

    private val requestParser by lazy { context.locationRequestParser }
    private val responseParser by lazy { context.locationResponseParser }

    private val locationResponder by lazy { context.locationResponder }
    private val locationRequester by lazy { context.locationRequester }

    private val scope = CoroutineScope(Dispatchers.IO)
    private val personRepository by lazy { context.personsRepository }

    fun onReceive(from: Person, message: String) {
        requestParser.parseLocationRequest(from, message)?.let {
            scope.launch {
                if (personRepository.isPersonRegistered(it.from)) {
                    locationResponder.handleLocationRequest(it)
                }
            }
            return
        }
        responseParser.parseLocationResponse(from, message)?.let {
            locationRequester.onLocationResponse(it)
            return
        }
    }
}