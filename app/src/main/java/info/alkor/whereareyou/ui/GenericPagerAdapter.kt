package info.alkor.whereareyou.ui

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter

data class FragmentDescriptor(val title: String, val creator: () -> Fragment)

class GenericPagerAdapter(fragmentManager: FragmentManager, private val tabs: List<FragmentDescriptor>) : FragmentPagerAdapter(fragmentManager) {
    override fun getItem(position: Int) = tabs[position].creator()
    override fun getCount() = tabs.size
    override fun getPageTitle(position: Int) = tabs[position].title
}