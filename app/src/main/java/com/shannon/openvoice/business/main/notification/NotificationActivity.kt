package com.shannon.openvoice.business.main.notification

import com.shannon.android.lib.base.activity.KBaseActivity
import com.shannon.android.lib.base.viewmodel.EmptyViewModel
import com.shannon.android.lib.extended.dp
import com.shannon.android.lib.extended.gone
import com.shannon.android.lib.extended.singleClick
import com.shannon.openvoice.FunApplication
import com.shannon.openvoice.R
import com.shannon.openvoice.business.creation.ModelLikesFragment
import com.shannon.openvoice.databinding.ActivityNotificationBinding

/**
 * Date:2023/3/7
 * Time:17:18
 * author:dimple
 * 通知页面
 */
class NotificationActivity : KBaseActivity<ActivityNotificationBinding, EmptyViewModel>() {

    private lateinit var notificationFragment: NotificationFragment


    override fun onInit() {
        binding.apply {
            toolsbar.apply {
                titleView.text = getString(R.string.notifications)
                FunApplication.appViewModel.setTopViewHeight(
                    this@NotificationActivity,
                    clMain,
                    R.id.toolsbar,
                    5.dp
                )
                vDividingLine.gone()
            }

            notificationFragment = NotificationFragment.newInstance()


            ivClean.singleClick {
                notificationFragment.clearNotificationMessage()
            }

            ivFilter.singleClick {
                notificationFragment.turnOnFilterSettings()
            }
        }
        initFragment()
    }

    /**
     * 初始化Fragment
     */
    private fun initFragment() {
        val transaction = supportFragmentManager.beginTransaction()
        transaction.add(
            R.id.fragmentContainer,
            notificationFragment
        )
        transaction.commit()
    }
}