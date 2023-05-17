package com.shannon.openvoice.network

import okhttp3.Interceptor
import okhttp3.Response
import okhttp3.ResponseBody
import timber.log.Timber
import java.lang.String

/**
 *
 * @ProjectName:    OpenVoice
 * @Package:        com.shannon.openvoice.network
 * @ClassName:      ResponseLogInterceptor
 * @Description:     响应日志
 * @Author:         czhen
 * @CreateDate:     2022/7/27 14:53
 */
class ResponseLogInterceptor : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {

        val response = chain.proceed(chain.request())
        var body: ResponseBody? = null
        try {
            body = response.peekBody(1024 * 1024)
            Timber.d("response : ${body.string()}")
        } catch (e:Exception){}
        return response
    }
}