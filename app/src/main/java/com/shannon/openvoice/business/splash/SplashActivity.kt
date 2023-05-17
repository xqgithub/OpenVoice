package com.shannon.openvoice.business.splash

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.shannon.android.lib.constant.ConfigConstants
import com.shannon.android.lib.extended.intentToJump
import com.shannon.android.lib.util.PreferencesUtil
import com.shannon.android.lib.util.PreferencesUtil.Constant.APP_TOKEN
import com.shannon.android.lib.util.PreferencesUtil.Constant.CLIENT_ID
import com.shannon.android.lib.util.PreferencesUtil.Constant.CLIENT_SECRET
import com.shannon.openvoice.FunApplication.Companion.appViewModel
import com.shannon.openvoice.R
import com.shannon.openvoice.business.guide.GuidePagesActivity
import com.shannon.openvoice.business.guide.GuideViewModel
import com.shannon.openvoice.business.main.MainActivity
import com.shannon.openvoice.business.main.enum.ActivityType
import com.shannon.openvoice.business.main.login.LoginActivity
import com.shannon.openvoice.business.main.mine.account.AccountManager.Companion.accountManager
import org.greenrobot.eventbus.EventBus

/**
 *
 * @ProjectName:    OpenVoice
 * @Package:        com.shannon.openvoice.business.splash
 * @ClassName:      SplashActivity
 * @Description:     启动页
 * @Author:         czhen
 * @CreateDate:     2022/7/21 14:38
 */
class SplashActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)
////        临时更新token数据，之后需要在登录接口完成
//        accountManager.upDateToken("gTqlhlGscOszAlVGJEvR1IinT5JeIpfO7OrLWaRudzg")
////        校验用户证书
//        appViewModel.accountVerifyCredentials(this)

        appViewModel.systemConfiguration(this@SplashActivity)
        if (!isTaskRoot) {
            finish()
        } else {
            if (accountManager.isLogin()) {
                appViewModel.accountVerifyCredentials(this@SplashActivity) { bean ->
                    accountManager.updateAccount(bean)
                    EventBus.getDefault().post(bean)
                }
                intentToJump(
                    this@SplashActivity, MainActivity::class.java,
                    isFinish = true
                )
            } else {
                appViewModel.authenticateApp(this)
            }

            dataEcho()
        }
    }

    /**
     * 数据回显
     */
    private fun dataEcho() {
        appViewModel.authenticateAppLive.observe(this@SplashActivity) { bean ->
            PreferencesUtil.putString(CLIENT_ID, bean.clientId)
            PreferencesUtil.putString(CLIENT_SECRET, bean.clientSecret)
            appViewModel.fetchOAuthToken(this@SplashActivity) { oAuthToken ->
                PreferencesUtil.putString(APP_TOKEN, oAuthToken.appToken)
                startActivity(MainActivity.newIntent(this@SplashActivity))
                finish()
            }
        }
    }
}