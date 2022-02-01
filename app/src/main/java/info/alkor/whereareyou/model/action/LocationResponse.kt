package info.alkor.whereareyou.model.action

import info.alkor.whereareyou.model.location.Location
import java.util.*

data class LocationResponse(
    val person: Person,
    val time: Date,
    val location: Location?,
    val final: Boolean
)
