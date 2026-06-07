package com.vspace.view.main

import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.vspace.view.apps.AppsFragment

/**
 * [FragmentStateAdapter] for the main [ViewPager2] that manages one [AppsFragment]
 * per virtual user.
 *
 * @property appCompatActivity the host activity.
 */
class ViewPagerAdapter(appCompatActivity: AppCompatActivity) : FragmentStateAdapter(appCompatActivity) {
    /** The mutable list of fragments backing the ViewPager pages. */
    private var fragmentList = mutableListOf<AppsFragment>()

    /**
     * Replaces the current fragment list with a new one and notifies the adapter
     * to refresh all pages.
     *
     * @param list the new list of [AppsFragment] instances.
     */
    fun replaceData(list: MutableList<AppsFragment>) {
        this.fragmentList = list
        notifyDataSetChanged()
    }

    override fun getItemCount(): Int {
        return fragmentList.size
    }

    override fun createFragment(position: Int): Fragment {
        return fragmentList[position]
    }
}
