package com.shannon.openvoice.business.compose.models

import android.content.Context
import android.content.Intent
import androidx.fragment.app.Fragment
import com.shannon.android.lib.base.activity.KBaseActivity
import com.shannon.android.lib.base.adapter.SimpleActivityPageAdapter
import com.shannon.android.lib.base.adapter.SimpleFragmentPageAdapter
import com.shannon.android.lib.base.viewmodel.EmptyViewModel
import com.shannon.android.lib.extended.addArray
import com.shannon.openvoice.R
import com.shannon.openvoice.business.creation.ModelListFragment
import com.shannon.openvoice.business.timeline.TimelineFragment
import com.shannon.openvoice.business.timeline.TimelineViewModel
import com.shannon.openvoice.databinding.ActivityChooseModelBinding
import com.shannon.openvoice.event.UniversalEvent
import com.shannon.openvoice.util.tabs.TabLayoutMediatorFun
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode

/**
 *
 * @Package:        com.shannon.openvoice.business.compose.models
 * @ClassName:      ChooseModelActivity
 * @Description:     发布声文-选择模型
 * @Author:         czhen
 * @CreateDate:     2023/3/8 9:22
 */
class ChooseModelActivity : KBaseActivity<ActivityChooseModelBinding, EmptyViewModel>() {
    private var fragmentList = mutableListOf<Fragment>()
    private val nameList = mutableListOf<String>()

    override fun onInit() {
        setTitleText(R.string.model)
        initTitles()
        initFragments()
        EventBus.getDefault().register(this)
        binding.run {
            viewPager.adapter = SimpleActivityPageAdapter(this@ChooseModelActivity, fragmentList)
            viewPager.offscreenPageLimit = 2
            TabLayoutMediatorFun(nameList, tabLayout, viewPager)
        }
    }

    private fun initFragments() {
        fragmentList.addArray(
            ModelsFragment(),
            ModelListFragment.newInstance(
                ModelListFragment.MODELLIST_MODIFY_OFFICAL
            )
        )
    }

    private fun initTitles() {
        nameList.addArray(getString(R.string.my_model), getString(R.string.official))
    }


    @Subscribe(threadMode = ThreadMode.MAIN)
    fun eventBusCloseModelComprehensiveActivity(event: UniversalEvent) {
        if (event.actionType == UniversalEvent.modifySelectedSoundModel) {
            onBackPressed()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        EventBus.getDefault().unregister(this)
    }

    companion object {

        fun newIntent(context: Context) = Intent(context, ChooseModelActivity::class.java)
    }
}