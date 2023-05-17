package com.shannon.openvoice.business.creation

import android.os.Bundle
import androidx.fragment.app.Fragment
import com.google.android.material.tabs.TabLayout
import com.shannon.android.lib.base.activity.KBaseFragment
import com.shannon.android.lib.base.adapter.SimpleFragmentPageAdapter
import com.shannon.android.lib.base.viewmodel.EmptyViewModel
import com.shannon.android.lib.extended.intentToJump
import com.shannon.android.lib.util.PreferencesUtil
import com.shannon.openvoice.R
import com.shannon.openvoice.business.guide.GuidePagesActivity
import com.shannon.openvoice.business.guide.GuideViewModel
import com.shannon.openvoice.business.timeline.listener.RefreshableFragment
import com.shannon.openvoice.databinding.FragmentCreationBinding
import com.shannon.openvoice.util.tabs.TabLayoutMediatorFun

/**
 *
 * @Package:        com.shannon.openvoice.business.creation
 * @ClassName:      CreationFragment
 * @Description:     作用描述
 * @Author:         czhen
 * @CreateDate:     2022/8/17 14:27
 */
class CreationFragment : KBaseFragment<FragmentCreationBinding, EmptyViewModel>(),
    TabLayout.OnTabSelectedListener,
    RefreshableFragment {

    private val tabTitles
            by lazy {
                mutableListOf(
                    getString(R.string.my_model),
                    getString(R.string.official),
                    getString(R.string.transaction_history)
                )
            }
    private val fragments by lazy {
        mutableListOf<Fragment>(
            ModelListFragment.newInstance(ModelListFragment.MODELLIST_NORMAL),
            ModelListFragment.newInstance(ModelListFragment.MODELLIST_SETUP),
            TransactionRecordFragment()
        )
    }


    companion object {
        fun newInstance(): CreationFragment {
            return CreationFragment()
        }
    }

    override fun onInit() {

    }

    override fun onLazyInit() {
        binding.run {
            viewPager.isUserInputEnabled = false
            viewPager.adapter = SimpleFragmentPageAdapter(this@CreationFragment, fragments)
            TabLayoutMediatorFun(tabTitles, tabLayout, viewPager)
            viewPager.offscreenPageLimit = fragments.size
            tabLayout.addOnTabSelectedListener(this@CreationFragment)

            //判断是否显示创作页新手引导
            if (PreferencesUtil.getBool(
                    PreferencesUtil.Constant.AUTHORING_PAGE_DISPLAYED_STATUS,
                    true
                )
            ) {
                intentToJump(
                    requireActivity(),
                    GuidePagesActivity::class.java,
                    bundle = Bundle().apply {
                        putString(
                            GuidePagesActivity.kind_type,
                            GuideViewModel.Kind.AuthoringPage.name
                        )
                    }
                )
            }
        }
    }

    override fun refreshContent() {
        if (isInitialized())
            (fragments[binding.viewPager.currentItem] as RefreshableFragment).refreshContent()
    }

    override fun onTabSelected(tab: TabLayout.Tab) {
    }

    override fun onTabUnselected(tab: TabLayout.Tab) {
    }

    override fun onTabReselected(tab: TabLayout.Tab) {
    }

}