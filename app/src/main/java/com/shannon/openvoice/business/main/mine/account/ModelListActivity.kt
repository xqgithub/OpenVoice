package com.shannon.openvoice.business.main.mine.account

import androidx.fragment.app.Fragment
import com.shannon.android.lib.base.activity.KBaseActivity
import com.shannon.android.lib.base.adapter.SimpleActivityPageAdapter
import com.shannon.android.lib.extended.addArray
import com.shannon.android.lib.extended.dp
import com.shannon.android.lib.extended.gone
import com.shannon.android.lib.extended.visible
import com.shannon.openvoice.FunApplication
import com.shannon.openvoice.R
import com.shannon.openvoice.business.creation.ModelLikesFragment
import com.shannon.openvoice.business.creation.ModelListFragment
import com.shannon.openvoice.business.main.mine.MineViewModel
import com.shannon.openvoice.business.main.mine.account.AccountManager.Companion.accountManager
import com.shannon.openvoice.databinding.ActivityModelListBinding
import com.shannon.openvoice.util.tabs.TabLayoutMediatorFun

/**
 * Date:2022/8/26
 * Time:14:37
 * author:dimple
 * 模型列表
 */
class ModelListActivity : KBaseActivity<ActivityModelListBinding, MineViewModel>() {

    private lateinit var accountId: String

    private var fragmentList = mutableListOf<Fragment>()
    private val nameList = mutableListOf<String>()


    override fun onInit() {
        accountId = intent.getStringExtra("accountId")!!

        initTitles()
        initFragments()

        binding.apply {
            toolsbar.apply {
                titleView.text = getString(R.string.model)
                FunApplication.appViewModel.setTopViewHeight(
                    this@ModelListActivity,
                    clMain,
                    R.id.toolsbar,
                    5.dp
                )
                vDividingLine.gone()
            }


//            sbLabelOfficeModel.apply {
//                isEnabled = false
//                singleClick {
//                    isEnabled = false
//                    sbLabelMyModel.isEnabled = true
//                    replaceFragmentLayout(
//                        ModelListFragment.newInstance(
//                            ModelListFragment.MODELLIST_SETUP
//                        )
//                    )
//                }
//            }
//
//            sbLabelMyModel.apply {
//                isEnabled = true
//                singleClick {
//                    isEnabled = false
//                    sbLabelOfficeModel.isEnabled = true
//                    replaceFragmentLayout(ModelListFragment.newInstance(ModelListFragment.MODELLIST_NORMAL))
//                }
//            }

            viewPager.adapter = SimpleActivityPageAdapter(this@ModelListActivity, fragmentList)
            viewPager.offscreenPageLimit = 2
            viewPager.isUserInputEnabled = false
            TabLayoutMediatorFun(nameList, tabLayout, viewPager)
        }
//        initFragmentLayout()
    }

    /**
     * 初始化Fragment
     */
//    private fun initFragmentLayout() {
//        val transaction = supportFragmentManager.beginTransaction()
//        transaction.add(
//            R.id.fragmentContainer,
//            if (accountManager.accountIsMe(accountId)) ModelListFragment.newInstance(
//                ModelListFragment.MODELLIST_SETUP
//            ) else {
//                binding.sbLabelOfficeModel.gone()
//                binding.sbLabelMyModel.gone()
//                ModelLikesFragment.newInstance(accountId)
//            }
//        )
//        transaction.commit()
//    }

    /**
     * 替换Fragment页面
     */
//    private fun replaceFragmentLayout(fragment: Fragment) {
//        val transaction = supportFragmentManager.beginTransaction()
//        transaction.replace(
//            R.id.fragmentContainer,
//            fragment
//        )
//        transaction.commit()
//    }


    /**
     * 初始化tablayout的标题
     */
    private fun initTitles() {
        nameList.addArray(getString(R.string.my_model), getString(R.string.official))
    }

    /**
     * 初始化Fragment
     */
    private fun initFragments() {
        if (accountManager.accountIsMe(accountId)) {
            binding.fragmentContainer.gone()
            binding.cltabLayout.visible()

            fragmentList.addArray(
                ModelListFragment.newInstance(ModelListFragment.MODELLIST_NORMAL),
                ModelListFragment.newInstance(
                    ModelListFragment.MODELLIST_SETUP
                )
            )
        } else {
            binding.fragmentContainer.visible()
            binding.cltabLayout.gone()

            val transaction = supportFragmentManager.beginTransaction()
            transaction.add(
                R.id.fragmentContainer,
                ModelLikesFragment.newInstance(accountId)
            )
            transaction.commit()
        }
    }

}