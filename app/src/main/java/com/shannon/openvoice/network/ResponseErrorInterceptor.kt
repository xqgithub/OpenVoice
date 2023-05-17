package com.shannon.openvoice.network

import okhttp3.Interceptor
import okhttp3.Response
import okhttp3.ResponseBody
import org.json.JSONObject

/**
 *
 * @Package:        com.shannon.openvoice.network
 * @ClassName:      ResponseErrorInterceptor
 * @Description:     临时处理异常情况
 * @Author:         czhen
 * @CreateDate:     2022/8/25 16:02
 */
class ResponseErrorInterceptor : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val response = chain.proceed(chain.request())
        var body: ResponseBody? = null
        try {
            body = response.peekBody(1024 * 1024)
            val content = body.string()
            if (response.isSuccessful && parseError(content)) {
                return response.newBuilder().code(5001).build()
            }
        } catch (e: Exception) {
        } finally {
            body?.close()
        }
        return response
    }

    private fun parseError(jsonError: String): Boolean {
        return try {
            val jsonObject = JSONObject(jsonError)
            jsonObject.has("error")
        } catch (e: Exception) {
            false
        }
    }
}