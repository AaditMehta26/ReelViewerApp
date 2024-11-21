package com.example.reelviewerapp

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter

class ReelPagerAdapter(activity: FragmentActivity, private val videoResIds: List<Int>) :
    FragmentStateAdapter(activity) {
    override fun getItemCount(): Int = videoResIds.size

    override fun createFragment(position: Int): Fragment {
        return ReelFragment.newInstance(videoResIds[position])
    }
}
