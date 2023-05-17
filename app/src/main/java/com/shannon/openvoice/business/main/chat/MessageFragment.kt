package com.shannon.openvoice.business.main.chat

import android.graphics.Typeface
import android.widget.TextView
import androidx.core.view.get
import androidx.fragment.app.Fragment
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import com.shannon.android.lib.base.activity.KBaseFragment
import com.shannon.android.lib.base.adapter.SimpleFragmentPageAdapter
import com.shannon.android.lib.base.viewmodel.EmptyViewModel
import com.shannon.openvoice.R
import com.shannon.openvoice.business.main.notification.NotificationFragment
import com.shannon.openvoice.business.timeline.listener.RefreshableFragment
import com.shannon.openvoice.databinding.FragmentMessageBinding

/**
 *
 * @Package:        com.shannon.openvoice.business.main.chat
 * @ClassName:      MessageFragment
 * @Description:     作用描述
 * @Author:         czhen
 * @CreateDate:     2023/2/22 10:07
 */
class MessageFragment : KBaseFragment<FragmentMessageBinding, EmptyViewModel>(),
    TabLayoutMediator.TabConfigurationStrategy, TabLayout.OnTabSelectedListener,
    RefreshableFragment {

    private val tabTitles =
        arrayListOf(
            R.string.vovo,
            R.string.notifications
        )
    private val fragments by lazy {
        mutableListOf<Fragment>(
            ConversationFragment.newInstance(),
            NotificationFragment.newInstance()
        )
    }

    companion object {
        fun newInstance(): MessageFragment {
            return MessageFragment()
        }
    }

    override fun onInit() {
    }

    override fun onLazyInit() {
        binding.run {
            viewPager.offscreenPageLimit = 2
            viewPager.adapter = SimpleFragmentPageAdapter(this@MessageFragment, fragments)
            tabLayout.addOnTabSelectedListener(this@MessageFragment)
            TabLayoutMediator(tabLayout, viewPager, this@MessageFragment).attach()
        }
    }

    override fun refreshContent() {
        if (isInitialized())
            (fragments[binding.viewPager.currentItem] as RefreshableFragment).refreshContent()
    }

    override fun onConfigureTab(tab: TabLayout.Tab, position: Int) {
        tab.text = getString(tabTitles[position])
    }

    override fun onTabSelected(tab: TabLayout.Tab) {

        val index = (tab.view.childCount - 1).coerceAtLeast(0)
        val childView = tab.view[index]
        if (childView is TextView) {
            childView.typeface = Typeface.DEFAULT_BOLD
        }
        refreshContent()
    }

    override fun onTabUnselected(tab: TabLayout.Tab) {
        val index = (tab.view.childCount - 1).coerceAtLeast(0)
        val childView = tab.view[index]
        if (childView is TextView) {
            childView.typeface = Typeface.DEFAULT
        }
    }

    override fun onTabReselected(tab: TabLayout.Tab) {
    }
}