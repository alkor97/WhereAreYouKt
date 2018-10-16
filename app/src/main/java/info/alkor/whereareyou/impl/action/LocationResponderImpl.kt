package info.alkor.whereareyou.impl.action

import android.util.Log
import info.alkor.whereareyou.api.action.LocationResponder
import info.alkor.whereareyou.api.context.AppContext
import info.alkor.whereareyou.model.action.LocationRequest
import kotlinx.coroutines.experimental.launch

class LocationResponderImpl(private val context: AppContext) : LocationResponder {
    override fun handleLocationRequest(request: LocationRequest) {
        launch {
            context.locationProvider.getLocation(context.settings.getLocationQueryTimeout()) { location, _ ->
                Log.i("response receiving", location?.toString())
            }
        }
    }
}