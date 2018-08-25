package info.alkor.whereareyou.ui

import android.arch.lifecycle.ViewModelProviders
import android.databinding.DataBindingUtil
import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import info.alkor.whereareyou.api.context.AppContext
import info.alkor.whereareyoukt.R
import info.alkor.whereareyoukt.databinding.FragmentSimpleBinding
import kotlinx.coroutines.experimental.Job
import kotlinx.coroutines.experimental.channels.consumeEach
import kotlinx.coroutines.experimental.launch

/**
 * A placeholder fragment containing a simple view.
 */
class SimpleActivityFragment : Fragment() {

    private var job: Job? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val locationViewModel = ViewModelProviders.of(activity!!).get(SingleLocationViewModel::class.java)
        if (context != null) {
            val ctx = context?.applicationContext as AppContext
            job = launch {
                ctx.locationChannel.consumeEach {
                    locationViewModel.update(it.location)
                }
            }
        }

        val binding: FragmentSimpleBinding = DataBindingUtil.inflate(inflater, R.layout.fragment_simple, container, false)
        binding.setLifecycleOwner(this)
        binding.locationModel = locationViewModel
        return binding.root
    }

    override fun onDestroyView() {
        job?.cancel()
        job = null
        super.onDestroyView()
    }
}
