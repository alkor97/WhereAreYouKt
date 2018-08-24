package info.alkor.whereareyou.api.communication

import info.alkor.whereareyou.model.communication.Message

interface MessageReceiver {
    fun onReceive(message: Message)
}
