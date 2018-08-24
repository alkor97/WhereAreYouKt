package info.alkor.whereareyou.api.communication

import info.alkor.whereareyou.model.action.Person
import info.alkor.whereareyou.model.action.SendingStatus
import kotlinx.coroutines.experimental.channels.ReceiveChannel

interface MessageSender {
    fun send(person: Person, message: String): ReceiveChannel<SendingStatus>
}
