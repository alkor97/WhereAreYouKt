package info.alkor.whereareyou.model.action

import info.alkor.whereareyou.model.location.Location

typealias MessageId = Long
enum class Direction { INCOMING, OUTGOING }

data class LocationAction(
        val id: MessageId?,
        val direction: Direction,
        val person: Person,
        val location: Location?,
        val final: Boolean,
        val status: SendingStatus,
        val progress: Float?)