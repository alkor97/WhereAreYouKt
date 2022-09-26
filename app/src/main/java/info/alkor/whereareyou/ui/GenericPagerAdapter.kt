package info.alkor.whereareyou.ui

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Lifecycle
import androidx.viewpager2.adapter.FragmentStateAdapter

data class FragmentDescriptor(val title: String, val creator: () -> Fragment)

class GenericPagerAdapter(fragmentManager: FragmentManager, lifecycle: Lifecycle, private val tabs: List<FragmentDescriptor>) : FragmentStateAdapter(fragmentManager, lifecycle) {
    override fun getItemCount(): Int = tabs.size
    override fun createFragment(position: Int): Fragment = tabs[position].creator()
}