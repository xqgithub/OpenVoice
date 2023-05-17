package com.shannon.openvoice.business.main.mine.setting

import android.os.Build.VERSION_CODES.P
import android.view.View
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import com.shannon.android.lib.base.activity.KBaseActivity
import com.shannon.android.lib.constant.ConfigConstants.CONSTANT.activityType
import com.shannon.android.lib.extended.*
import com.shannon.android.lib.util.ThemeUtil
import com.shannon.android.lib.util.ToastUtil
import com.shannon.openvoice.FunApplication
import com.shannon.openvoice.FunApplication.Companion.appViewModel
import com.shannon.openvoice.R
import com.shannon.openvoice.business.main.enum.ActivityType
import com.shannon.openvoice.business.main.mine.MineViewModel
import com.shannon.openvoice.business.main.mine.account.AccountManager
import com.shannon.openvoice.business.main.mine.account.AccountManager.Companion.accountManager
import com.shannon.openvoice.components.TextAndTextUi
import com.shannon.openvoice.databinding.ActivityMyannouncmentBinding

/**
 * Date:2022/8/15
 * Time:16:26
 * author:dimple
 * 我的账号页面
 */
class MyAccountActivity : KBaseActivity<ActivityMyannouncmentBinding, MineViewModel>() {

    override fun onInit() {
        binding.apply {
            toolsbar.apply {
                titleView.text = resources.getString(R.string.your_account)
                FunApplication.appViewModel.setTopViewHeight(
                    this@MyAccountActivity,
                    clMain,
                    R.id.toolsbar,
                    5.dp
                )
                vDividingLine.gone()
            }
            tvChangePassword.visibility(accountManager.getAccountData().provider == "email")
            ivMore.visibility(accountManager.getAccountData().provider == "email")
            placeAccountView.text = resources.getString(R.string.your_account).plus(":")
            accountView.text =  if (accountManager.getAccountData().provider == "wallet") accountManager.getAccountData().walletAddress
                ?: "" else accountManager.getAccountData().email

            tvChangePassword.singleClick {
                intentToJump(this@MyAccountActivity, ChangePasswordActiviity::class.java)
            }

            tvDestroyAccount.singleClick {
                intentToJump(this@MyAccountActivity, DestroyAccountActivity::class.java)
            }
        }
    }
}