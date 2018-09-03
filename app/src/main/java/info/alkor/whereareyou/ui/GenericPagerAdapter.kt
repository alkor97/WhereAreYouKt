package info.alkor.whereareyou.ui

import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentPagerAdapter

data class FragmentDescriptor(val title: String, val creator: () -> Fragment)

class GenericPagerAdapter(fragmentManager: FragmentManager, private val tabs: List<FragmentDescriptor>) : FragmentPagerAdapter(fragmentManager) {
    override fun getItem(position: Int) = tabs[position].creator()
    override fun getCount() = tabs.size
    override fun getPageTitle(position: Int) = tabs[position].title
}