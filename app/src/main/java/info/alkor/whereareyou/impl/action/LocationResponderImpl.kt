package info.alkor.whereareyou.impl.action

import android.util.Log
import info.alkor.whereareyou.api.context.AppContext
import info.alkor.whereareyou.model.action.LocationRequest
import info.alkor.whereareyou.model.action.LocationResponse
import info.alkor.whereareyou.model.action.finishesSending
import kotlin.coroutines.experimental.suspendCoroutine

class LocationResponderImpl(private val context: AppContext) : LocalLocationResponder(context) {

    private val persistence by lazy { context.locationRequestPersistence }
    private val sender by lazy { context.messageSender }

    private val loggingTag = "responder"

    override suspend fun sendResponse(request: LocationRequest, response: LocationResponse) = suspendCoroutine<Unit> { cont ->
        val person = request.person
        sender.send(response) {
            persistence.onCommunicationStatusUpdate(request, it)
            Log.i(loggingTag, "status of location response sent to $person is $it")
            if (it.finishesSending()) {
                cont.resume(Unit)
            }
            Log.i(loggingTag, "location response sent to $person")
        }
    }
}