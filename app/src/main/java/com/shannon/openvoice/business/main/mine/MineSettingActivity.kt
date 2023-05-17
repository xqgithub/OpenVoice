package com.shannon.openvoice.business.main.mine

import com.shannon.android.lib.base.activity.KBaseActivity
import com.shannon.android.lib.extended.dp
import com.shannon.android.lib.extended.gone
import com.shannon.openvoice.FunApplication
import com.shannon.openvoice.R
import com.shannon.openvoice.databinding.ActivityMineSettingBinding

/**
 * Date:2023/2/22
 * Time:16:30
 * author:dimple
 * 个人设置页面
 */
class MineSettingActivity : KBaseActivity<ActivityMineSettingBinding, MineViewModel>() {

    override fun onInit() {
        binding.apply {
            toolsbar.apply {
                titleView.text = resources.getString(R.string.Settting)
                FunApplication.appViewModel.setTopViewHeight(
                    this@MineSettingActivity,
                    clMain,
                    R.id.toolsbar,
                    5.dp
                )
                vDividingLine.gone()
            }

            initFragmentLayout()
        }
    }

    /**
     * 初始化Fragment
     */
    private fun initFragmentLayout() {
        val transaction = supportFragmentManager.beginTransaction()
        transaction.add(
            R.id.fragmentContainer,
            MineFragment()
        )
        transaction.commit()
    }
}