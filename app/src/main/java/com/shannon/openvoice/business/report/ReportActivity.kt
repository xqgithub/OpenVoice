package com.shannon.openvoice.business.report

import android.content.Context
import android.content.Intent
import androidx.fragment.app.commit
import com.shannon.android.lib.base.activity.KBaseActivity
import com.shannon.openvoice.R
import com.shannon.openvoice.databinding.ActivityReportBinding
import com.shannon.openvoice.model.StatusModel

/**
 *
 * @Package:        com.shannon.openvoice.business.report
 * @ClassName:      ReportActivity
 * @Description:     作用描述
 * @Author:         czhen
 * @CreateDate:     2022/8/22 11:24
 */
class ReportActivity : KBaseActivity<ActivityReportBinding, ReportViewModel>() {

    override fun onInit() {
        val status = intent.getParcelableExtra<StatusModel>(STATUS)
        val accountId = status?.actionableStatus?.account?.id
            ?: intent.getStringExtra(ACCOUNT_ID)!!
        val userName = status?.actionableStatus?.account?.username
            ?: intent.getStringExtra(
                USER_NAME
            )!!
        setTitleText(getString(R.string.report_users, userName))
        viewModel.onInit(status, accountId, userName)
        supportFragmentManager.commit {
            replace(R.id.containerView, ReportFragment())
        }
    }

    fun replaceCompleteFragment() {
        supportFragmentManager.commit {
            replace(R.id.containerView, ReportCompleteFragment())
        }
    }

    companion object {
        private const val STATUS = "status"
        private const val ACCOUNT_ID = "accountId"
        private const val USER_NAME = "userName"

        /**
         * 声文列表调用
         * @param context Context
         * @param status StatusModel
         * @return Intent
         */
        fun newIntent(context: Context, status: StatusModel) =
            Intent(context, ReportActivity::class.java).apply {
                putExtra(STATUS, status)
            }

        /**
         * 用户主页调用
         * @param context Context
         * @param accountId String 用户ID
         * @param userName String 用户昵称(username)
         * @return Intent
         */
        fun newIntent(context: Context, accountId: String, userName: String) =
            Intent(context, ReportActivity::class.java).apply {
                putExtra(ACCOUNT_ID, accountId)
                putExtra(USER_NAME, userName)
            }
    }
}