package com.shannon.openvoice.business.main.mine.setting

import com.shannon.android.lib.base.activity.KBaseActivity
import com.shannon.android.lib.extended.dp
import com.shannon.android.lib.extended.gone
import com.shannon.openvoice.FunApplication
import com.shannon.openvoice.R
import com.shannon.openvoice.business.creation.ModelLikesFragment
import com.shannon.openvoice.business.creation.ModelListFragment
import com.shannon.openvoice.business.main.mine.MineViewModel
import com.shannon.openvoice.databinding.ActivityModelYouLikesBinding

/**
 * Date:2023/2/28
 * Time:11:34
 * author:dimple
 * 你喜欢的模型页面
 */
class ModelYouLikesAcvity : KBaseActivity<ActivityModelYouLikesBinding, MineViewModel>() {

    override fun onInit() {
        binding.apply {
            toolsbar.apply {
                titleView.text = getString(R.string.model)
                FunApplication.appViewModel.setTopViewHeight(
                    this@ModelYouLikesAcvity,
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
            ModelLikesFragment.newInstance()
        )
        transaction.commit()
    }
}