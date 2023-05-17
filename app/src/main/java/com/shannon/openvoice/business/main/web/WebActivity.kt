package com.shannon.openvoice.business.main.web

import android.widget.LinearLayout
import androidx.activity.OnBackPressedCallback
import com.just.agentweb.AgentWeb
import com.shannon.android.lib.base.activity.KBaseActivity
import com.shannon.android.lib.base.viewmodel.EmptyViewModel
import com.shannon.android.lib.constant.ConfigConstants
import com.shannon.android.lib.extended.dp
import com.shannon.android.lib.extended.gone
import com.shannon.android.lib.extended.singleClick
import com.shannon.android.lib.util.ToastUtil
import com.shannon.openvoice.FunApplication
import com.shannon.openvoice.R
import com.shannon.openvoice.databinding.ActivityWebBinding

/**
 * Date:2022/10/8
 * Time:9:38
 * author:dimple
 * 通用webView
 */
class WebActivity : KBaseActivity<ActivityWebBinding, EmptyViewModel>() {

    //url地址
    private var url: String? = ""

    //标题名称
    private var titleName: String? = ""

    private var mAgentWeb: AgentWeb? = null

    private var preWeb: AgentWeb.PreAgentWeb? = null


    override fun onInit() {
        url = intent?.getStringExtra(ConfigConstants.CONSTANT.webUrl)
        titleName = intent?.getStringExtra(ConfigConstants.CONSTANT.webTitleName)

        binding.apply {
            toolsbar.apply {
                titleView.text = titleName
                FunApplication.appViewModel.setTopViewHeight(
                    this@WebActivity,
                    clMain,
                    R.id.toolsbar,
                    5.dp
                )
                vDividingLine.gone()

//                navigationButton.singleClick {
//                    ToastUtil.showCenter("我点击了返回按钮")
//                }
            }
            preWeb = AgentWeb.with(this@WebActivity)
                .setAgentWebParent(webcontent, LinearLayout.LayoutParams(-1, -1))
                .useDefaultIndicator()
                .createAgentWeb()
                .ready()

            //加载网页
            mAgentWeb = preWeb?.go(url)

            //返回监听
            onBackPressedDispatcher.addCallback(this@WebActivity,
                object : OnBackPressedCallback(true) {
                    override fun handleOnBackPressed() {
                        mAgentWeb?.let { web ->
                            if (web.webCreator.webView.canGoBack()) {
                                web.webCreator.webView.goBack()
                            } else {
                                finish()
                            }
                        }
                    }
                })
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