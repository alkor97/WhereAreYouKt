package info.alkor.whereareyou.ui

import android.databinding.DataBindingUtil
import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import info.alkor.whereareyoukt.R
import info.alkor.whereareyoukt.databinding.FragmentSimpleBinding

/**
 * A placeholder fragment containing a simple view.
 */
class SimpleActivityFragment : Fragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val activity = activity as SimpleActivity
        val binding: FragmentSimpleBinding = DataBindingUtil.inflate(inflater, R.layout.fragment_simple, container, false)
        binding.locationModel = activity.locationViewModel
        return binding.root
    }
}
