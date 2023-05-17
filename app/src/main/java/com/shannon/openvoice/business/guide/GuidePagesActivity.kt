package com.shannon.openvoice.business.guide

import android.view.KeyEvent
import androidx.fragment.app.Fragment
import androidx.viewpager2.widget.ViewPager2
import com.blankj.utilcode.util.LogUtils
import com.shannon.android.lib.base.activity.KBaseActivity
import com.shannon.android.lib.base.adapter.SimpleActivityPageAdapter
import com.shannon.android.lib.extended.addArray
import com.shannon.android.lib.util.PreferencesUtil
import com.shannon.openvoice.databinding.ActivityGuidepagesBinding
import com.shannon.openvoice.event.UniversalEvent
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode

/**
 * Date:2022/11/1
 * Time:10:19
 * author:dimple
 * 引导页集合
 */
class GuidePagesActivity : KBaseActivity<ActivityGuidepagesBinding, GuideViewModel>() {

    private val fragments = arrayListOf<Fragment>()

    //默认是推荐页
    private var guidekind = GuideViewModel.Kind.RecommendedPage

    companion object {
        public const val kind_type = "kind_type"
    }


    override fun onInit() {
        EventBus.getDefault().register(this)

        intent.getStringExtra(kind_type)?.let {
            guidekind = GuideViewModel.Kind.valueOf(it)
        }

        initFragments()

        binding.apply {
            //初始化ViewPager
            viewPager.adapter = SimpleActivityPageAdapter(this@GuidePagesActivity, fragments)
            viewPager.isUserInputEnabled = false
            viewPager.offscreenPageLimit = fragments.size
            viewPager.registerOnPageChangeCallback(ViewPagerChange())
        }

    }

    /**
     * 初始化Fragment页面
     */
    private fun initFragments() {
        if (guidekind == GuideViewModel.Kind.RecommendedPage) {
            fragments.addArray(
                GuidePagesFragment.newInstance(GuideViewModel.Kind.RecommendedPageOne),
                GuidePagesFragment.newInstance(GuideViewModel.Kind.RecommendedPageTwo),
                GuidePagesFragment.newInstance(GuideViewModel.Kind.RecommendedPageThree)
            )
        } else {
            fragments.addArray(
                GuidePagesFragment.newInstance(GuideViewModel.Kind.AuthoringPageOne),
                GuidePagesFragment.newInstance(GuideViewModel.Kind.AuthoringPageTwo),
                GuidePagesFragment.newInstance(GuideViewModel.Kind.AuthoringPageThree),
                GuidePagesFragment.newInstance(GuideViewModel.Kind.AuthoringPageFourth)
            )
        }
    }

    private inner class ViewPagerChange : ViewPager2.OnPageChangeCallback() {
        override fun onPageSelected(position: Int) {
            val fragment = fragments[position]
            LogUtils.i("position =-= $position")
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun eventBusGuidePage(event: UniversalEvent) {
        if (event.actionType == UniversalEvent.guidePageKind) {
            val kindType = event.message as GuideViewModel.Kind
            when (kindType) {
                GuideViewModel.Kind.RecommendedPageOne, GuideViewModel.Kind.RecommendedPageTwo,
                GuideViewModel.Kind.AuthoringPageOne, GuideViewModel.Kind.AuthoringPageTwo, GuideViewModel.Kind.AuthoringPageThree -> {
                    val currentItem = binding.viewPager.currentItem
                    binding.viewPager.setCurrentItem(currentItem + 1, true)
                }
                GuideViewModel.Kind.RecommendedPageThree -> {
                    //引导标识修改成不显示
                    PreferencesUtil.putBool(
                        PreferencesUtil.Constant.RECOMMENDED_PAGE_DISPLAYED_STATUS,
                        false
                    )
                    //关闭页面
                    finish()
                }
                GuideViewModel.Kind.AuthoringPageFourth -> {
                    //引导标识修改成不显示
                    PreferencesUtil.putBool(
                        PreferencesUtil.Constant.AUTHORING_PAGE_DISPLAYED_STATUS,
                        false
                    )
                    //关闭页面
                    finish()
                }
            }
        }
    }


    override fun onDestroy() {
        super.onDestroy()
        EventBus.getDefault().unregister(this)
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent): Boolean {
        when (keyCode) {
            KeyEvent.KEYCODE_BACK -> {
                return true
            }
        }
        return super.onKeyDown(keyCode, event)
    }
}