package com.shannon.openvoice.network

import com.blankj.utilcode.util.LogUtils
import com.permissionx.guolindev.PermissionX
import com.shannon.android.lib.BuildConfig
import com.shannon.android.lib.constant.ConfigConstants
import com.shannon.android.lib.extended.isBlankPlus
import com.shannon.android.lib.util.PreferencesUtil
import com.shannon.openvoice.FunApplication
import com.shannon.openvoice.business.main.mine.account.AccountManager.Companion.accountManager
import com.shannon.openvoice.extended.AppViewModel
import okhttp3.Interceptor
import okhttp3.Response
import java.lang.String

/**
 *
 * @ProjectName:    OpenVoice
 * @Package:        com.shannon.openvoice.network
 * @ClassName:      AuthInterceptor
 * @Description:     java类作用描述
 * @Author:         czhen
 * @CreateDate:     2022/7/27 14:53
 */
class AuthInterceptor : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val token = String.format(
            "Bearer %s",
            if (isBlankPlus(accountManager.getAccountData().accessToken))
                PreferencesUtil.getString(
                    PreferencesUtil.Constant.APP_TOKEN,
                    ""
                ) else accountManager.getAccountData().accessToken
        )//String.format("Bearer %s", "eTaoO73WY7voN6hWBGitnhMqaJicJ1k54JNjkHJDlvE")
        val language = PreferencesUtil.getString(
            PreferencesUtil.Constant.LANGUAGE,
            if (BuildConfig.BUILD_TYPE == ConfigConstants.BuildType.RELEASE) "en" else "default"
        )
        val accept_language = if (language == "default") {
            FunApplication.appViewModel.getCurrentLanguage(FunApplication.getInstance().applicationContext)
        } else {
            language
        }
//        LogUtils.d(
//            " token =-= $token",
//            "language =-= $language",
//            "accept_language =-= $accept_language"
//        )
        val requestWithAuth =
//            chain.request().newBuilder().header(
//                "Authorization", token
//            ).build()
            chain.request().newBuilder()
                .addHeader("Authorization", token)
                .addHeader(
                    "Accept-Language",
                    accept_language
                )
                .build()
        return chain.proceed(requestWithAuth)
    }
}