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
import info.alkor.whereareyou.common.Duration
import info.alkor.whereareyou.impl.settings.GOOGLE_API_KEY
import info.alkor.whereareyou.model.action.DurationCompleted
import info.alkor.whereareyou.model.action.DurationProgress
import info.alkor.whereareyou.model.action.OwnLocationResponse
import info.alkor.whereareyou.model.location.Location
import info.alkor.whereareyou.model.location.toMinimalText
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
                ctx.locationResponsesChannel.consumeEach {
                    when (it) {
                        is OwnLocationResponse -> locationViewModel.update(it.location, prepareLink(it.location))
                        is DurationProgress -> locationViewModel.updateProgress(it.value.toElapsedString(resources))
                        is DurationCompleted -> locationViewModel.finishProgress(resources.getString(R.string.completed_within_duration, it.value.toString(resources)))
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
            location.toMinimalText(),
            "",
            "You",
            GOOGLE_API_KEY) else null

    override fun onDestroyView() {
        job?.cancel()
        job = null
        super.onDestroyView()
    }
}

fun Duration.toElapsedString(resources: Resources) = byUnit().joinToString(" ") { (value, unit) ->
    val plural = when (unit) {
        TimeUnit.DAYS -> R.plurals.elapsed_duration_days
        TimeUnit.HOURS -> R.plurals.elapsed_duration_hours
        TimeUnit.MINUTES -> R.plurals.elapsed_duration_minutes
        TimeUnit.SECONDS -> R.plurals.elapsed_duration_seconds
        TimeUnit.MILLISECONDS -> R.plurals.elapsed_duration_milliseconds
        TimeUnit.MICROSECONDS -> R.plurals.elapsed_duration_microseconds
        TimeUnit.NANOSECONDS -> R.plurals.elapsed_duration_nanoseconds
    }
    resources.getQuantityString(plural, value.toInt(), value)
}
