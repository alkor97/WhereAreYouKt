package info.alkor.whereareyou.api.communication

import info.alkor.whereareyou.model.action.LocationRequest
import info.alkor.whereareyou.model.action.LocationResponse
import info.alkor.whereareyou.model.action.SendingStatus

typealias SendingStatusCallback = (SendingStatus) -> Unit

interface MessageSender {
    fun send(request: LocationRequest, callback: SendingStatusCallback)
    fun send(response: LocationResponse, callback: SendingStatusCallback)
}
