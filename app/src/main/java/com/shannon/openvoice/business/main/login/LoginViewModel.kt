package com.shannon.openvoice.business.main.login

import android.app.Activity
import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.text.Spannable
import android.text.SpannableString
import android.text.Spanned
import android.text.TextPaint
import android.text.style.AbsoluteSizeSpan
import android.text.style.ClickableSpan
import android.text.style.ForegroundColorSpan
import android.view.View
import android.widget.TextView
import androidx.annotation.ColorInt
import androidx.core.content.ContextCompat
import androidx.core.net.toUri
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.MutableLiveData
import com.blankj.utilcode.util.LogUtils
import com.blankj.utilcode.util.NetworkUtils
import com.google.gson.JsonObject
import com.shannon.android.lib.BuildConfig
import com.shannon.android.lib.base.viewmodel.BaseViewModel
import com.shannon.android.lib.constant.ConfigConstants
import com.shannon.android.lib.exception.XException
import com.shannon.android.lib.extended.intentToJump
import com.shannon.android.lib.extended.observableToMain
import com.shannon.android.lib.util.CacheUtil
import com.shannon.android.lib.util.PreferencesUtil
import com.shannon.android.lib.util.ToastUtil
import com.shannon.openvoice.FunApplication
import com.shannon.openvoice.FunApplication.Companion.appViewModel
import com.shannon.openvoice.R
import com.shannon.openvoice.business.main.web.WebActivity
import com.shannon.openvoice.model.OAuthToken
import com.shannon.openvoice.network.apiService
import com.shannon.openvoice.network.convertResponse
import com.shannon.openvoice.network.convertResponseCode
import com.shannon.openvoice.util.ThreadPoolUtils
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.toRequestBody
import retrofit2.http.Field
import java.io.File

/**
 *
 * @ProjectName:    OpenVoice
 * @Package:        com.shannon.openvoice.business.main.mine
 * @ClassName:      MineViewModel
 * @Description:     java类作用描述
 * @Author:         czhen
 * @CreateDate:     2022/7/27 10:49
 */
class LoginViewModel : BaseViewModel() {

    /**
     * 邮箱注册
     */
    val emailRegistrationLive = MutableLiveData<OAuthToken>()
    fun emailRegistration(
        context: Context,
        username: String,
        email: String,
        password: String,
        birthday: String,
        agreement: Boolean,
        emailCode: String,
        viewLifecycleOwner: LifecycleOwner
    ) {
        val language = PreferencesUtil.getString(
            PreferencesUtil.Constant.LANGUAGE,
            if (BuildConfig.BUILD_TYPE == ConfigConstants.BuildType.RELEASE) "en" else "default"
        )
        val locale = if (language == "default") {
            appViewModel.getCurrentLanguage(context)
        } else {
            language
        }
        apiService.emailRegistrationV2(
            username,
            email,
            password,
            birthday,
            agreement,
            locale,
            emailCode
        ).convertResponse()
            .funSubscribeRxLife(owner = viewLifecycleOwner, onNext = {
                emailRegistrationLive.postValue(it)
            }, onError = {
                LogUtils.e("onError =-=  $it")
            })
    }

    /**
     * 注册验证码发送邮件
     */
    fun registerSendEmail(
        email: String,
        viewLifecycleOwner: LifecycleOwner,
        onCallback: () -> Unit
    ) {
        val requestBody = JsonObject().let {
            it.add("user", JsonObject().let { node ->
                node.addProperty("email", email)
                node
            })
            it.toString().toRequestBody("application/json".toMediaTypeOrNull())
        }
        apiService.registerSendEmail(requestBody)
            .convertResponse()
            .funSubscribeRxLife(owner = viewLifecycleOwner, onNext = {
                LogUtils.i("subscribe =-=  $it")
                onCallback()
            }, onError = {
                LogUtils.e("onError =-=  $it")
            })
    }


    /**
     * 重置密码发邮件
     */
    fun forgotPwdSendEmail(
        email: String,
        viewLifecycleOwner: LifecycleOwner,
        onCallback: () -> Unit
    ) {
        val requestBody = JsonObject().let {
            it.add("user", JsonObject().let { node ->
                node.addProperty("email", email)
                node
            })
            it.toString().toRequestBody("application/json".toMediaTypeOrNull())
        }
        apiService.forgotPwdSendEmail(requestBody)
            .convertResponse()
            .funSubscribeRxLife(owner = viewLifecycleOwner, onNext = {
                LogUtils.i("subscribe =-=  $it")
                onCallback()
            }, onError = {
                LogUtils.e("onError =-=  $it")
            })
    }

    /**
     * 重置密码
     */
    fun resetPassword(
        reset_password_token: String,
        password: String,
        password_confirmation: String,
        viewLifecycleOwner: LifecycleOwner,
        onCallback: (isSuccess: Boolean) -> Unit
    ) {
        val requestBody = JsonObject().let {
            it.add("user", JsonObject().let { node ->
                node.addProperty("reset_password_token", reset_password_token)
                node.addProperty("password", password)
                node.addProperty("password_confirmation", password_confirmation)
                node
            })
            it.toString().toRequestBody("application/json".toMediaTypeOrNull())
        }
        apiService.resetPassword(requestBody)
            .convertResponse()
            .funSubscribeRxLife(owner = viewLifecycleOwner, onNext = {
                LogUtils.i("subscribe =-=  $it")
                onCallback(true)
            }, onError = {
                LogUtils.e("onError =-=  $it")
                onCallback(false)
            })
    }

    /**
     * 用户登录
     */
    fun userLogin(
        userName: String,
        password: String,
        viewLifecycleOwner: LifecycleOwner,
        onCallback: (oAuthToken: OAuthToken) -> Unit
    ) {
        ThreadPoolUtils(ThreadPoolUtils.Type.FixedThread, 1).execute {
            if (!NetworkUtils.isConnected() || !NetworkUtils.isAvailable()) {
                ToastUtil.showCenter(
                    FunApplication.getInstance().getString(R.string.tips_network_exception)
                )
                return@execute
            }

            val domain = "voicedev.aifun.com"
            val scopes = ConfigConstants.CONSTANT.scope
            val grantType = "password"
            val clientId = PreferencesUtil.getString(PreferencesUtil.Constant.CLIENT_ID, "")
            val clientSecret = PreferencesUtil.getString(PreferencesUtil.Constant.CLIENT_SECRET, "")
            apiService.userLogin(
                domain,
                clientId,
                clientSecret,
                appViewModel.oauthRedirectUri,
                scopes,
                grantType,
                userName,
                password
            ).convertResponse()
                .funSubscribeRxLife(owner = viewLifecycleOwner, onNext = {
                    onCallback(it)
                }, onError = {
                    LogUtils.e("onError =-=  $it")
                })
        }
    }

    /**
     * 设置用户协议和隐私政策
     */
    fun setUserAgreementPrivacyPolicy(
        activity: Activity,
        content: String,
        param: Array<String>,
        @ColorInt color: Array<Int> = arrayOf(),
        textSizes: Array<Int> = arrayOf(),
        enableClickEvent: Boolean = false
    ): SpannableString {
        val msp = SpannableString(content)

        for (i in param.indices) {
            val startIndex = content.indexOf(param[i])
            val endIndex = startIndex + param[i].length
            //设置点击事件
            if (enableClickEvent) {
                msp.setSpan(
                    object : ClickableSpan() {
                        override fun onClick(widget: View) {
                            when (i) {
                                0 -> {
                                    intentToJump(
                                        activity,
                                        WebActivity::class.java,
                                        bundle = Bundle().apply {
                                            putString(
                                                ConfigConstants.CONSTANT.webUrl,
                                                ConfigConstants.CONSTANT.userAgreementValue
                                            )
                                            putString(
                                                ConfigConstants.CONSTANT.webTitleName,
                                                activity.getString(R.string.user_agreement_1)
                                            )
                                        })
                                }
                                1 -> {
                                    intentToJump(
                                        activity,
                                        WebActivity::class.java,
                                        bundle = Bundle().apply {
                                            putString(
                                                ConfigConstants.CONSTANT.webUrl,
                                                ConfigConstants.CONSTANT.privacyPolicyValue
                                            )
                                            putString(
                                                ConfigConstants.CONSTANT.webTitleName,
                                                activity.getString(R.string.privacy_policy_1)
                                            )
                                        })
                                }
                            }
                        }

                        override fun updateDrawState(ds: TextPaint) {
                            //去掉下划线
                            ds.isUnderlineText = false
                        }

                    }, startIndex, endIndex,
                    Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                )
            }
            //设置颜色
            val colorSpan = ForegroundColorSpan(if (i < color.size) color[i] else color[0])
            msp.setSpan(colorSpan, startIndex, endIndex, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
            if (textSizes.isNotEmpty()) {
                msp.setSpan(
                    AbsoluteSizeSpan(textSizes[i], true),
                    startIndex,
                    endIndex,
                    Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
                )
            }
        }
        return msp
    }

    fun nonce(onResult: (String) -> Unit) {
        apiService.nonce()
            .convertResponse()
            .funSubscribe {
                onResult(it.nonce)
            }
    }

    fun signIn(
        message: String,
        signature: String,
        onResult: (OAuthToken) -> Unit,
        onError: (Throwable) -> Unit
    ) {
        apiService.signIn(message, signature)
            .convertResponse()
            .funSubscribe(onError = {
                onError(it)
            }, onNext = {
                onResult(it)
            })
    }

    fun updateAccountInfo(username: String, birthday: String, onResult: () -> Unit) {
        apiService.updateAccountInfo(username, birthday)
            .convertResponseCode()
            .funSubscribeNotLoading {
                onResult()
            }
    }
}