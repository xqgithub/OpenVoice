package com.shannon.openvoice.business.main.home

import android.view.MotionEvent
import androidx.fragment.app.Fragment
import com.google.android.material.tabs.TabLayout
import com.shannon.android.lib.base.activity.KBaseFragment
import com.shannon.android.lib.base.adapter.SimpleFragmentPageAdapter
import com.shannon.android.lib.base.viewmodel.EmptyViewModel
import com.shannon.android.lib.extended.addArray
import com.shannon.android.lib.extended.visibility
import com.shannon.openvoice.R
import com.shannon.openvoice.business.main.mine.account.AccountManager
import com.shannon.openvoice.business.player.PlayerHelper
import com.shannon.openvoice.business.player.listener.OnDataSourceListener
import com.shannon.openvoice.business.player.listener.OnPlayerViewShownListener
import com.shannon.openvoice.business.timeline.TimelineFragment
import com.shannon.openvoice.business.timeline.TimelineViewModel
import com.shannon.openvoice.business.timeline.listener.RefreshableFragment
import com.shannon.openvoice.databinding.FragmentHomeBinding
import com.shannon.openvoice.event.UniversalEvent
import com.shannon.openvoice.extended.loadAvatar
import com.shannon.openvoice.extended.startActivityIntercept
import com.shannon.openvoice.util.tabs.ITabConfigurationStrategy
import com.shannon.openvoice.util.tabs.TabLayoutMediatorFun
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode

/**
 *
 * @ProjectName:    OpenVoice
 * @Package:        com.shannon.openvoice.business.main.home
 * @ClassName:      HomeFragment
 * @Description:     关注
 * @Author:         czhen
 * @CreateDate:     2022/7/25 16:46
 */
class HomeFragment : KBaseFragment<FragmentHomeBinding, EmptyViewModel>(), RefreshableFragment,
    ITabConfigurationStrategy, OnPlayerViewShownListener, OnDataSourceListener {
    private var fragmentList = mutableListOf<Fragment>()
    private val nameList = mutableListOf<String>()

    companion object {
        fun newInstance(): HomeFragment {
            return HomeFragment()
        }
    }

    override fun onInit() {
        EventBus.getDefault().register(this)
        initTitles()
        initFragments()
        binding.run {
            viewPager.adapter = SimpleFragmentPageAdapter(this@HomeFragment, fragmentList)
            viewPager.isUserInputEnabled = AccountManager.accountManager.isLogin()
            viewPager.offscreenPageLimit = 2
            TabLayoutMediatorFun(nameList, tabLayout, viewPager, this@HomeFragment)
            PlayerHelper.instance.addPlayerListener(miniPlayerView)
            miniPlayerView.dataSourceListener = this@HomeFragment
        }
    }

    private fun initFragments() {
        fragmentList.addArray(
            TimelineFragment.newInstance(TimelineViewModel.Kind.PUBLIC_LOCAL),
            TimelineFragment.newInstance(TimelineViewModel.Kind.HOME)
        )
    }

    private fun initTitles() {
        nameList.addArray(getString(R.string.trends_for_you), getString(R.string.tab_following))
    }

    override fun onDestroy() {
        super.onDestroy()
        EventBus.getDefault().unregister(this)
        PlayerHelper.instance.removePlayerListener(binding.miniPlayerView)
    }

    override fun refreshContent() {
        if (isInitialized())
            (fragmentList[binding.viewPager.currentItem] as RefreshableFragment).refreshContent()
    }

    override fun onConfigureTab(tab: TabLayout.Tab, position: Int) {
        tab.view.setOnTouchListener { v, event ->
            if (event.action == MotionEvent.ACTION_UP) {
                if (position < 1 || AccountManager.accountManager.isLogin()) v.performClick()

                return@setOnTouchListener requireContext().startActivityIntercept(position < 1) { }
            }
            return@setOnTouchListener true
        }
    }

    override fun onShown(show: Boolean) {
        binding.miniPlayerView.visibility(show)
    }

    override fun bindPlaylist(type: Int): Boolean {
        val fragment = fragmentList[binding.viewPager.currentItem]
        return if (fragment is OnDataSourceListener) {
            fragment.bindPlaylist(type)
        } else {
            false
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun eventBusLogin(event: UniversalEvent) {
        if (event.actionType == UniversalEvent.loginSuccess) {
            binding.viewPager.isUserInputEnabled = true
        }
    }
}