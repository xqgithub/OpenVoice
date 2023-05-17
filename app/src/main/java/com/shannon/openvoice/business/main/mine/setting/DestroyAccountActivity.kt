package com.shannon.openvoice.business.main.mine.setting

import android.widget.LinearLayout
import com.just.agentweb.AgentWeb
import com.shannon.android.lib.base.activity.KBaseActivity
import com.shannon.android.lib.constant.ConfigConstants
import com.shannon.android.lib.extended.dp
import com.shannon.android.lib.extended.gone
import com.shannon.android.lib.extended.showDialogConfirmWithIcon
import com.shannon.android.lib.extended.singleClick
import com.shannon.openvoice.FunApplication
import com.shannon.openvoice.R
import com.shannon.openvoice.business.main.mine.MineViewModel
import com.shannon.openvoice.databinding.ActivityDestroyaccountBinding

/**
 * Date:2022/12/20
 * Time:9:58
 * author:dimple
 * 账号注销
 */
class DestroyAccountActivity : KBaseActivity<ActivityDestroyaccountBinding, MineViewModel>() {

    private var mAgentWeb: AgentWeb? = null

    private var preWeb: AgentWeb.PreAgentWeb? = null


    override fun onInit() {

        binding.apply {
            toolsbar.apply {
                titleView.text = getString(R.string.cancellation_agreement)
                FunApplication.appViewModel.setTopViewHeight(
                    this@DestroyAccountActivity,
                    clMain,
                    R.id.toolsbar,
                    5.dp
                )
                vDividingLine.gone()
            }

            sbRefuse.singleClick {
                finish()
            }
            sbAgree.singleClick {
                showDialogConfirmWithIcon(
                    getString(R.string.cancellation_confirm__content),
                    R.drawable.ic_opv_warning,
                    getString(R.string.sure),
                    getString(R.string.button_no),
                    doConfirm = {
                        viewModel.accountsSuspend(this@DestroyAccountActivity)
                    }, doCancel = {
                        finish()
                    })
            }


            preWeb = AgentWeb.with(this@DestroyAccountActivity)
                .setAgentWebParent(webcontent, LinearLayout.LayoutParams(-1, -1))
                .useDefaultIndicator()
                .createAgentWeb()
                .ready()

            //加载网页
            mAgentWeb = preWeb?.go(ConfigConstants.CONSTANT.userLogoutAgreementValue)

//            //返回监听
//            onBackPressedDispatcher.addCallback(this@DestroyAccountActivity,
//                object : OnBackPressedCallback(true) {
//                    override fun handleOnBackPressed() {
//                        mAgentWeb?.let { web ->
//                            if (web.webCreator.webView.canGoBack()) {
//                                web.webCreator.webView.goBack()
//                            } else {
//                                finish()
//                            }
//                        }
//                    }
//                })

        }
    }


    override fun onPause() {
        mAgentWeb?.webLifeCycle?.onPause()
        super.onPause()
    }

    override fun onResume() {
        mAgentWeb?.webLifeCycle?.onResume()
        super.onResume()
    }

    override fun onDestroy() {
        mAgentWeb?.webLifeCycle?.onDestroy()
        setSupportActionBar(null)
        super.onDestroy()
    }

}