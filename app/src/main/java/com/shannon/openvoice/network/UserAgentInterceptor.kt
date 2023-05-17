package com.shannon.openvoice.network

import android.os.Build
import com.shannon.openvoice.BuildConfig
import okhttp3.Interceptor
import okhttp3.OkHttp
import okhttp3.Response

/**
 *
 * @ProjectName:    OpenVoice
 * @Package:        com.shannon.openvoice.network
 * @ClassName:      UserAgentInterceptor
 * @Description:     java类作用描述
 * @Author:         czhen
 * @CreateDate:     2022/7/26 14:25
 */
class UserAgentInterceptor : Interceptor {
    private val headerName = "User-Agent"
    private val headerValue =
        "OpenVoice/${BuildConfig.VERSION_NAME} Android/${Build.VERSION.RELEASE} OkHttp/${OkHttp.VERSION}"

    override fun intercept(chain: Interceptor.Chain): Response {
        val requestWithUserAgent =
            chain.request().newBuilder().header(headerName, headerValue).build()

        return chain.proceed(requestWithUserAgent)
    }
}