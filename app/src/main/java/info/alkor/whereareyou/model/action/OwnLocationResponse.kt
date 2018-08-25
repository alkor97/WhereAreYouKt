package info.alkor.whereareyou.model.action

import info.alkor.whereareyou.model.location.Location

data class OwnLocationResponse(val location: Location?, val final: Boolean)