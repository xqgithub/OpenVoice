package com.shannon.openvoice.util.tabs

import android.view.View
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator

class TabLayoutMediatorFun(tabTitleList: MutableList<String>,
                           private val tabLayout: TabLayout,
                           private val viewPager: ViewPager2,
                           private val iTabConfigurationStrategy: ITabConfigurationStrategy? = null,
                           private val tabLayoutMediatorStrategy: TabLayoutMediator.TabConfigurationStrategy = TabLayoutMediatorStrategy(tabLayout.context, tabTitleList, viewPager,iTabConfigurationStrategy)) : TabLayout.OnTabSelectedListener, ViewPager2.OnPageChangeCallback() {

    private var previousScrollState = ViewPager2.SCROLL_STATE_IDLE
    private var scrollState = ViewPager2.SCROLL_STATE_IDLE

    init {
        TabLayoutMediator(tabLayout, viewPager, tabLayoutMediatorStrategy).attach()
        tabLayout.addOnTabSelectedListener(this)
        viewPager.registerOnPageChangeCallback(this)
    }

    //tab
    override fun onTabSelected(tab: TabLayout.Tab?) {
        tab?.apply {
            if (tabLayoutMediatorStrategy is OnTabPageChangeCallback && customView is View) {
                tabLayoutMediatorStrategy.onTabSelected(customView as View)
            }
        }
    }

    override fun onTabUnselected(tab: TabLayout.Tab?) {
        tab?.apply {
            if (tabLayoutMediatorStrategy is OnTabPageChangeCallback && customView is View) {
                tabLayoutMediatorStrategy.onTabUnselected(customView as View)
            }
        }
    }

    override fun onTabReselected(tab: TabLayout.Tab?) {
    }

    //viewpager
    override fun onPageSelected(position: Int) {
        val tab = tabLayout.getTabAt(position)
        val customView = tab?.customView
        if (tabLayoutMediatorStrategy is OnTabPageChangeCallback && customView is View) {
            tabLayoutMediatorStrategy.onTabSelected(customView)
        }
    }

    override fun onPageScrollStateChanged(state: Int) {
        previousScrollState = scrollState
        scrollState = state
    }

    override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {
        super.onPageScrolled(position, positionOffset, positionOffsetPixels)
        //当previousScrollState == 0 && scrollState == 2 时表示该次滚动为用户点击TabLayout造成的滚动，忽略掉。。。。
        if (!(previousScrollState == 0 && scrollState == 2)) {
            val formatPositionOffset = Math.round(positionOffset * 100).toFloat() / 100
            val selectedChild = tabLayout.getTabAt(position)?.customView
            val nextChild = tabLayout.getTabAt(position + 1)?.customView
            if (selectedChild is View && nextChild is View && tabLayoutMediatorStrategy is OnTabPageChangeCallback) {
                tabLayoutMediatorStrategy.onPageScrolled(selectedChild, nextChild, formatPositionOffset)
            }
        }
    }
}