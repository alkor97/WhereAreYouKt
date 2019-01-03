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
import info.alkor.whereareyoukt.databinding.SingleRequestFragmentBinding
import kotlinx.coroutines.experimental.Job
import kotlinx.coroutines.experimental.channels.consumeEach
import kotlinx.coroutines.experimental.launch


class SingleRequestFragment : Fragment() {

    companion object {
        fun newInstance() = SingleRequestFragment()
    }

    private var job: Job? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val viewModel = activity?.run {
            ViewModelProviders.of(this).get(SingleRequestViewModel::class.java)
        }

        if (context != null) {
            val ctx = context?.applicationContext as AppContext
            job = launch {
                ctx.locationRequestPersistence.events.consumeEach { viewModel?.handleEvent(it) }
            }
        }

        val binding: SingleRequestFragmentBinding = DataBindingUtil.inflate(inflater, R.layout.single_request_fragment, container, false)
        binding.setLifecycleOwner(this)
        binding.model = viewModel

        return binding.root
    }

    override fun onDestroyView() {
        job?.cancel()
        job = null
        super.onDestroyView()
    }
}
