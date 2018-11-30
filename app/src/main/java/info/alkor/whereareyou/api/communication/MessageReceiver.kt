package info.alkor.whereareyou.api.communication

import info.alkor.whereareyou.model.action.Person

interface MessageReceiver {
    fun onReceive(from: Person, message: String)
}
