package com.soul.bluetooth

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Lifecycle
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.soul.base.BaseFragment


/**
 *     author : yangzy33
 *     time   : 2024-08-13
 *     desc   :
 *     version: 1.0
 */
class ViewPagerFragmentStateAdapter(
    fragmentManager: FragmentManager,
    lifecycle: Lifecycle
): FragmentStateAdapter(fragmentManager, lifecycle) {
    private val mDataList = mutableListOf<BaseFragment>()

    override fun getItemCount(): Int = mDataList.size

    override fun createFragment(position: Int): BaseFragment {
        return mDataList[position]
    }

    fun addFragment(fragment: BaseFragment) {
        mDataList.add(fragment)
    }
}