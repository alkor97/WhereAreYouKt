package info.alkor.whereareyou.api.persistence

import info.alkor.whereareyou.common.Duration
import info.alkor.whereareyou.model.action.MessageId
import info.alkor.whereareyou.model.action.Person
import info.alkor.whereareyou.model.action.SendingStatus
import info.alkor.whereareyou.model.location.Location

interface LocationRequestEvent {
    val id: MessageId
}

data class NoLocation(override val id: MessageId) : LocationRequestEvent
data class LocationRequested(override val id: MessageId, val person: Person) : LocationRequestEvent
interface WithLocation {
    val location: Location
}

data class FinalLocation(override val id: MessageId, override val location: Location) : WithLocation, LocationRequestEvent
data class IntermediateLocation(override val id: MessageId, override val location: Location) : WithLocation, LocationRequestEvent
data class SendingStatusUpdated(override val id: MessageId, val status: SendingStatus) : LocationRequestEvent
data class ExecutionProgress(override val id: MessageId, val elapsed: Duration) : LocationRequestEvent
data class ExecutionCompleted(override val id: MessageId, val elapsed: Duration) : LocationRequestEvent
