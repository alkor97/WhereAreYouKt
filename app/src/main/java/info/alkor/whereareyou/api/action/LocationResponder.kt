package info.alkor.whereareyou.api.action

import info.alkor.whereareyou.model.action.LocationRequest

interface LocationResponder {
    fun handleLocationRequest(request: LocationRequest)
}