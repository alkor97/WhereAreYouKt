package info.alkor.whereareyou.impl.action

import android.util.Log
import info.alkor.whereareyou.api.action.LocationRequester
import info.alkor.whereareyou.api.communication.MessageSender
import info.alkor.whereareyou.api.context.AppContext
import info.alkor.whereareyou.model.action.LocationResponse
import info.alkor.whereareyou.model.action.Person
import kotlinx.coroutines.experimental.channels.consumeEach
import kotlinx.coroutines.experimental.launch

class LocationRequesterImpl(private val context: AppContext) : LocationRequester {

    private var sender: MessageSender? = null

    override fun requestLocationOf(person: Person) {
        val message = "Hey, where are you?"
        val channel = sender?.send(person, message)
        launch {
            channel?.consumeEach {
                Log.i("request sending", it.name)
            }
        }
    }

    override fun onLocationResponse(response: LocationResponse) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }
}