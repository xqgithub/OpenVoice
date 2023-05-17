package com.shannon.openvoice.business.main.viewmedia

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity

/**
 * Date:2022/8/23
 * Time:14:05
 * author:dimple
 */
class SingleImagePagerAdapter(
    activity: FragmentActivity,
    private val imageUrl: String
) : ViewMediaActivity.ViewMediaAdapter(activity) {

    override fun onTransitionEnd(position: Int) {
    }

    override fun getItemCount(): Int {
        return 1
    }

    override fun createFragment(position: Int): Fragment {
        return if (position == 0) {
            ViewMediaFragment.newSingleImageInstance(imageUrl)
        } else {
            throw IllegalStateException()
        }
    }
}