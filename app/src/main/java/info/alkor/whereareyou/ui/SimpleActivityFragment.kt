package info.alkor.whereareyou.ui

import android.arch.lifecycle.ViewModelProviders
import android.content.res.Resources
import android.databinding.DataBindingUtil
import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import info.alkor.whereareyou.api.context.AppContext
import info.alkor.whereareyou.api.persistence.ExecutionCompleted
import info.alkor.whereareyou.api.persistence.ExecutionProgress
import info.alkor.whereareyou.api.persistence.FinalLocation
import info.alkor.whereareyou.api.persistence.IntermediateLocation
import info.alkor.whereareyou.common.Duration
import info.alkor.whereareyou.impl.settings.GOOGLE_API_KEY
import info.alkor.whereareyou.model.location.Location
import info.alkor.whereareyou.model.location.LocationFormatter
import info.alkor.whereareyoukt.R
import info.alkor.whereareyoukt.databinding.FragmentSimpleBinding
import kotlinx.coroutines.experimental.Job
import kotlinx.coroutines.experimental.channels.consumeEach
import kotlinx.coroutines.experimental.launch
import java.util.concurrent.TimeUnit

/**
 * A placeholder fragment containing a simple view.
 */
class SimpleActivityFragment : Fragment() {

    private var job: Job? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        if (activity == null)
            return null

        val locationViewModel = ViewModelProviders.of(activity!!).get(SingleLocationViewModel::class.java)
        if (context != null) {
            val ctx = context?.applicationContext as AppContext
            job = launch {
                ctx.locationRequestPersistence.events.consumeEach { event ->
                    when (event) {
                        is FinalLocation ->
                            locationViewModel.update(event.location, prepareLink(event.location))
                        is IntermediateLocation ->
                            locationViewModel.update(event.location, prepareLink(event.location))
                        is ExecutionProgress ->
                            locationViewModel.updateProgress(event.elapsed.toElapsedString(resources))
                        is ExecutionCompleted ->
                            locationViewModel.finishProgress(resources.getString(R.string.completed_within_duration, event.elapsed.toString(resources)))
                    }
                }
            }
        }

        val binding: FragmentSimpleBinding = DataBindingUtil.inflate(inflater, R.layout.fragment_simple, container, false)
        binding.setLifecycleOwner(this)
        binding.locationModel = locationViewModel

        return binding.root
    }

    private fun prepareLink(location: Location?) = if (location != null) context?.getString(R.string.location_presenter_url,
            LocationFormatter.format(location),
            "",
            "You",
            GOOGLE_API_KEY) else null

    override fun onDestroyView() {
        job?.cancel()
        job = null
        super.onDestroyView()
    }
}

fun Duration.toElapsedString(resources: Resources): String {
    var first = true
    return byUnit().joinToString(" ") { (value, unit) ->
        val plural = when (unit) {
            TimeUnit.DAYS -> if (first) R.plurals.elapsed_duration_days else R.plurals.duration_days
            TimeUnit.HOURS -> if (first) R.plurals.elapsed_duration_hours else R.plurals.duration_hours
            TimeUnit.MINUTES -> if (first) R.plurals.elapsed_duration_minutes else R.plurals.duration_minutes
            TimeUnit.SECONDS -> if (first) R.plurals.elapsed_duration_seconds else R.plurals.duration_seconds
            TimeUnit.MILLISECONDS -> if (first) R.plurals.elapsed_duration_milliseconds else R.plurals.duration_milliseconds
            TimeUnit.MICROSECONDS -> if (first) R.plurals.elapsed_duration_microseconds else R.plurals.duration_microseconds
            TimeUnit.NANOSECONDS -> if (first) R.plurals.elapsed_duration_nanoseconds else R.plurals.duration_nanoseconds
        }
        first = false
        resources.getQuantityString(plural, value.toInt(), value)
    }
}
