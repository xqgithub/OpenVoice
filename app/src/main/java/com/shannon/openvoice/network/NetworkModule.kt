package com.shannon.openvoice.network

import at.connyduck.calladapter.kotlinresult.KotlinResultCallAdapterFactory
import com.google.gson.Gson
import com.shannon.android.lib.constant.ConfigConstants
import com.shannon.android.lib.util.JsonUtil
import com.shannon.android.lib.util.MLogUtils
import com.shannon.openvoice.BuildConfig
import com.shannon.openvoice.FunApplication
import okhttp3.*
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.adapter.rxjava3.RxJava3CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.create
import java.security.SecureRandom
import java.security.cert.CertificateException
import java.security.cert.X509Certificate
import java.util.concurrent.TimeUnit
import javax.net.ssl.HostnameVerifier
import javax.net.ssl.SSLContext
import javax.net.ssl.TrustManager
import javax.net.ssl.X509TrustManager

/**
 *
 * @ProjectName:    OpenVoice
 * @Package:        com.shannon.openvoice.network
 * @ClassName:      NetworkModule
 * @Description:     java类作用描述
 * @Author:         czhen
 * @CreateDate:     2022/7/26 14:00
 */
class NetworkModule {

    private val uploadHttpClient: OkHttpClient
    private val okHttpClient: OkHttpClient
    private val retrofit: Retrofit

    init {
        uploadHttpClient = providesUploadHttpClient()
        okHttpClient = providesHttpClient()
        retrofit = providesRetrofit()
    }

    private fun providesHttpClient(): OkHttpClient {
        val cacheSize = 25 * 1024 * 1024L
        val context = FunApplication.getInstance()
        val builder = OkHttpClient.Builder()
//            .sslSocketFactory(
//                SSLSocketClient.getSSLSocketFactory(),
//                (SSLSocketClient.getTrustManager())[0] as X509TrustManager
//            )
//            .hostnameVerifier(SSLSocketClient.getHostnameVerifier())
            .addInterceptor(UserAgentInterceptor())
            .addInterceptor(AuthInterceptor())
            .addInterceptor(ResponseErrorInterceptor())
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .cache(Cache(context.cacheDir, cacheSize))

        return builder.apply {
            if (BuildConfig.DEBUG) {
                addInterceptor(HttpLoggingInterceptor().apply {
                    level = HttpLoggingInterceptor.Level.BODY
                })
//                var mMessage = StringBuilder()
//                //添加Log信息拦截器
//                val loggingInterceptor = HttpLoggingInterceptor {
//                    var msg = it
//                    // 请求或者响应开始
//                    if (msg.startsWith("--> POST")
//                        || msg.startsWith("--> GET")
//                        || msg.startsWith("--> PUT")
//                    ) {
//                        mMessage.setLength(0)
//                    }
//                    // 以{}或者[]形式的说明是响应结果的json数据，需要进行格式化
//                    if ((msg.startsWith("{") && msg.endsWith("}"))
//                        || (msg.startsWith("[") && msg.endsWith("]"))
//                    ) {
//                        msg = JsonUtil.formatJson(JsonUtil.decodeUnicode(msg))
//                    }
//                    mMessage.append(msg.plus("\n"))
//
//
//                    // 响应结束，打印整条日志
//                    if (msg.startsWith("<-- END")) {
//                        MLogUtils.longInfo(
//                            ConfigConstants.CONSTANT.interface_log,
//                            mMessage.toString()
//                        )
//                    }
//                }.also {
//                    it.level = HttpLoggingInterceptor.Level.BODY
//                }
//                addInterceptor(loggingInterceptor)
            }
        }.build()
    }

    private fun providesUploadHttpClient(): OkHttpClient {
        val cacheSize = 25 * 1024 * 1024L
        val context = FunApplication.getInstance()
        val builder = OkHttpClient.Builder()
            .addInterceptor(UserAgentInterceptor())
            .addInterceptor(AuthInterceptor())
            .readTimeout(100, TimeUnit.SECONDS)
            .writeTimeout(100, TimeUnit.SECONDS)
            .cache(Cache(context.cacheDir, cacheSize))

        return builder.apply {
            if (BuildConfig.DEBUG) {
                addInterceptor(HttpLoggingInterceptor().apply {
                    level = HttpLoggingInterceptor.Level.BASIC
                })
            }
        }.build()
    }

    private fun providesRetrofit(): Retrofit {
        return Retrofit.Builder()
            .baseUrl(BuildConfig.HOST_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create(Gson()))
            .addCallAdapterFactory(RxJava3CallAdapterFactory.create())
            .addCallAdapterFactory(KotlinResultCallAdapterFactory.create())
            .build()
    }

    fun api(): ApiService = retrofit.create()

    fun mediaUploadApi(): MediaUploadApi {
        return retrofit.newBuilder()
            .client(uploadHttpClient)
            .build()
            .create()
    }

    fun buildWebSocket(listener: WebSocketListener): WebSocket {
        val request = Request.Builder().url("").build()
        return okHttpClient.newWebSocket(request, listener)
    }

    companion object {
        val instance: NetworkModule by lazy { NetworkModule() }
    }
}

