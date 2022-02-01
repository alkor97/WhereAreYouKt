package info.alkor.whereareyou.model.action

import info.alkor.whereareyou.model.location.Location
import java.util.*

typealias MessageId = Long
enum class Direction { INCOMING, OUTGOING }

data class LocationAction(
    val id: MessageId?,
    val direction: Direction,
    val person: Person,
    val time: Date,
    val location: Location?,
    val final: Boolean,
    val status: SendingStatus,
    val progress: Float?
)