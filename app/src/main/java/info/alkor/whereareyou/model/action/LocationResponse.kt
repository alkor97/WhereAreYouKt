package info.alkor.whereareyou.model.action

import info.alkor.whereareyou.model.location.Location

class LocationResponse(val person: Person, location: Location?, final: Boolean) : OwnLocationResponse(location, final)
