package com.example.reelviewerapp

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter

class ReelAdapter(
    fragmentActivity: FragmentActivity,
    private val videoUrls: List<Int>
) : FragmentStateAdapter(fragmentActivity) {

    override fun getItemCount(): Int = videoUrls.size

    override fun createFragment(position: Int): Fragment {
        return ReelFragment.newInstance(videoUrls[position])
    }
}
