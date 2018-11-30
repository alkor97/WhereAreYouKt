package info.alkor.whereareyou.api.persistence

import info.alkor.whereareyou.common.Duration
import info.alkor.whereareyou.model.action.Person
import info.alkor.whereareyou.model.action.SendingStatus
import info.alkor.whereareyou.model.location.Location

sealed class LocationRequestEvent
object NoLocation : LocationRequestEvent()
data class LocationRequested(val person: Person) : LocationRequestEvent()
data class FinalLocation(val location: Location) : LocationRequestEvent()
data class IntermediateLocation(val location: Location) : LocationRequestEvent()
data class SendingStatusUpdated(val status: SendingStatus) : LocationRequestEvent()
data class ExecutionProgress(val elapsed: Duration) : LocationRequestEvent()
data class ExecutionCompleted(val elapsed: Duration) : LocationRequestEvent()
