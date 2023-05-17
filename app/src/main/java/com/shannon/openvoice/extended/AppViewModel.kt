package com.shannon.openvoice.extended

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
import android.view.Gravity
import android.view.View
import android.view.WindowManager
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.result.contract.ActivityResultContracts
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.app.ActivityCompat.shouldShowRequestPermissionRationale
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.MutableLiveData
import com.blankj.utilcode.util.AppUtils
import com.blankj.utilcode.util.LogUtils
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.ktx.Firebase
import com.google.firebase.messaging.RemoteMessage
import com.google.firebase.messaging.ktx.messaging
import com.google.gson.JsonObject
import com.gyf.immersionbar.ImmersionBar
import com.shannon.android.lib.base.viewmodel.BaseViewModel
import com.shannon.android.lib.constant.ConfigConstants
import com.shannon.android.lib.exception.XException
import com.shannon.android.lib.extended.dp
import com.shannon.android.lib.extended.isBlankPlus
import com.shannon.android.lib.extended.isNotNull
import com.shannon.android.lib.extended.observableToMain
import com.shannon.android.lib.util.PreferencesUtil
import com.shannon.android.lib.util.PreferencesUtil.Constant.CLIENT_ID
import com.shannon.android.lib.util.PreferencesUtil.Constant.CLIENT_SECRET
import com.shannon.android.lib.util.ThemeUtil
import com.shannon.android.lib.util.ToastUtil
import com.shannon.android.lib.web3.dapp.DappProxy
import com.shannon.openvoice.BuildConfig
import com.shannon.openvoice.FunApplication
import com.shannon.openvoice.R
import com.shannon.openvoice.business.main.MainActivity
import com.shannon.openvoice.business.main.chat.ChatManager
import com.shannon.openvoice.business.main.mine.account.AccountManager
import com.shannon.openvoice.business.main.mine.account.AccountManager.Companion.accountManager
import com.shannon.openvoice.business.main.notification.NotificationActivity
import com.shannon.openvoice.dialog.ConfirmDialog
import com.shannon.openvoice.event.UniversalEvent
import com.shannon.openvoice.event.UniversalEvent.Companion.signOut
import com.shannon.openvoice.model.*
import com.shannon.openvoice.network.apiService
import com.shannon.openvoice.network.convertResponse
import com.shannon.openvoice.util.ConstraintUtil
import com.shannon.openvoice.util.notification.NotificationHelperUtils
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.disposables.Disposable
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.toRequestBody
import org.greenrobot.eventbus.EventBus
import org.json.JSONObject
import java.text.DecimalFormat
import java.text.SimpleDateFormat
import java.util.concurrent.TimeUnit

/**
 * Date:2022/8/4
 * Time:14:24
 * author:dimple
 * 全局的ViewModel
 */
class AppViewModel : BaseViewModel() {

    val oauthRedirectUri: String
        get() {
            val scheme = "oauth2redirect"
            val host = BuildConfig.APPLICATION_ID
            return "$scheme://$host/"
        }

    /**
     * 校验用户证书，获取AccountBean数据
     */
    fun accountVerifyCredentials(
        activity: Activity,
        onErrorAction: () -> Unit = {},
        onCallback: (accountBean: AccountBean) -> Unit
    ) {
        apiService.accountVerifyCredentials()
            .convertResponse()
            .funSubscribeNotLoading(onNext = {
                onCallback(it)
            }, onError = {
                onErrorAction()
                LogUtils.e("onError =-=  $it")
                if (it is XException) {
                    if (it.errorCode == 401) {
                        jump2MainPage(activity)
                    }
                }
            })
    }


    /**
     * 创建oauth2应用
     */
    val authenticateAppLive = MutableLiveData<oauth2Bean>()
    fun authenticateApp(viewLifecycleOwner: LifecycleOwner) {
        val domain = "voicedev.aifun.com"
        val clientName = FunApplication.getInstance().getString(R.string.app_name)
        val scopes = ConfigConstants.CONSTANT.scope
        val website = BuildConfig.HOST_URL
        apiService.authenticateApp(domain, clientName, oauthRedirectUri, scopes, website)
            .convertResponse()
            .funSubscribeRxLife(owner = viewLifecycleOwner, onNext = {
                authenticateAppLive.postValue(it)
            }, onError = {
                LogUtils.e("onError =-=  $it")
            })
    }

    /**
     * 获取oauth2应用token
     */

//    val fetchOAuthTokenLive = MutableLiveData<OAuthToken>()
    fun fetchOAuthToken(
        viewLifecycleOwner: LifecycleOwner,
        onCallback: (oAuthToken: OAuthToken) -> Unit
    ) {
        val domain = "voicedev.aifun.com"
        val scopes = ConfigConstants.CONSTANT.scope
        val grantType = "client_credentials"
        val clientId = PreferencesUtil.getString(CLIENT_ID, "")
        val clientSecret = PreferencesUtil.getString(CLIENT_SECRET, "")
        apiService.fetchOAuthToken(
            domain,
            clientId,
            clientSecret,
            oauthRedirectUri,
            scopes,
            grantType
        )
            .convertResponse()
            .funSubscribeRxLife(owner = viewLifecycleOwner, onNext = {
                onCallback(it)
            }, onError = {
                LogUtils.e("onError =-=  $it")
            })
    }

    /**
     * 获取系统功能配置项
     */
    fun systemConfiguration(
        viewLifecycleOwner: LifecycleOwner, onResult: (SystemConfigurationBean) -> Unit = {}
    ) {
        apiService.systemConfiguration()
            .convertResponse().funSubscribeRxLife(
                owner = viewLifecycleOwner,
                onNext = {
                    //更新模型邀请码功能是否开启数据
                    PreferencesUtil.putBool(
                        PreferencesUtil.Constant.VOICE_MODEL_INVITE_ENABLED,
                        it.voiceModelInviteEnabled
                    )
                    //更新数据
                    PreferencesUtil.putBool(
                        PreferencesUtil.Constant.SYSTEM_ANNOUNCEMENT_ENABLED,
                        it.systemAnnouncementEnabled
                    )
                    PreferencesUtil.putString(
                        PreferencesUtil.Constant.SYSTEM_ANNOUNCEMENT,
                        it.systemAnnouncement
                    )
                    PreferencesUtil.putString(
                        PreferencesUtil.Constant.ACTIVITY_URL,
                        it.activityUrl
                    )
                    onResult(it)
                },
                onError = {
                    LogUtils.e("onError =-=  $it")
                }
            )
    }


    /**
     *  检查更新请求数据
     */
    fun updateVersions(
        viewLifecycleOwner: LifecycleOwner,
        onCallback: (updateBean: UpdateBean) -> Unit
    ) {
        apiService.updateVersions("android")
            .convertResponse()
            .funSubscribeRxLife(
                owner = viewLifecycleOwner,
                onNext = {
                    onCallback(it)
//                    onCallback(it.copy(downloadUrl = "http://192.168.2.57:18080/examples/AIFUN-candy.apk"))
                },
                onError = {
                    LogUtils.e("onError =-=  $it")
                }
            )
    }

    /**
     * 更新准备
     */
    fun updatePreparation(context: Context, updateBean: UpdateBean): Boolean {
        var isUpdateVersion = false

        val localVersionNumberArray = arrayOfNulls<Int>(3)
        AppUtils.getAppVersionName().split(".", limit = 3).forEachIndexed { index, s ->
            localVersionNumberArray[index] = s.toInt()
        }

        val serviceVersionNumberArray = arrayOfNulls<Int>(3)
        updateBean.versionNumber.split(".", limit = 3).forEachIndexed { index, s ->
            serviceVersionNumberArray[index] = s.toInt()
        }

        if (localVersionNumberArray[0]!! < serviceVersionNumberArray[0]!!) {
            toUpdate(context, updateBean)
            isUpdateVersion = true
        } else if (localVersionNumberArray[0]!! == serviceVersionNumberArray[0]!!) {
            if (localVersionNumberArray[1]!! < serviceVersionNumberArray[1]!!) {
                toUpdate(context, updateBean)
                isUpdateVersion = true
            } else if (localVersionNumberArray[1]!! == serviceVersionNumberArray[1]!!) {
                if (localVersionNumberArray[2]!! < serviceVersionNumberArray[2]!!) {
                    toUpdate(context, updateBean)
                    isUpdateVersion = true
                }
            }
        }
        return isUpdateVersion
    }

    /**
     * 去更新
     */
    fun toUpdate(context: Context, updateBean: UpdateBean) {
        ConfirmDialog(
            context,
            width = 248.dp,
            height = WindowManager.LayoutParams.WRAP_CONTENT
        ).apply {
            setBG(
                R.attr.bulletFrameBackground
            )
            setCanceledOnTouchOutsideAndCanceled(
                !updateBean.isForceUpdate,
                !updateBean.isForceUpdate
            )

            setTitleText(
                _visibility = View.VISIBLE,
                content = context.getString(
                    R.string.tittle_update,
                    updateBean.versionNumber
                ),
                _textSize = 16f,
                _textColorInt = ThemeUtil.getColor(
                    context,
                    R.attr.textColorPrimary
                )
            )
            setContentText(
                content = updateBean.description,
                _textColorInt = ThemeUtil.getColor(
                    context,
                    android.R.attr.textColorPrimary
                ),
                _gravity = Gravity.LEFT
            )
            changeTvContentPositiontvTitle(1)


            setWebViewContent(_visibility = View.GONE)
            setConfirmText(
                content = context.getString(R.string.live_update),
                _textSize = 14f,
                _textColorInt = ThemeUtil.getColor(
                    context,
                    android.R.attr.textColorLink
                )
            ) {
                //google商店更新
                if (!launchAppStoreDetail(
                        context,
                        AppUtils.getAppPackageName()
                    )
                ) {
                    ToastUtil.showCenter(context.getString(R.string.tips_no_googlestore))
                }

                if (!updateBean.isForceUpdate) {
                    dismissDialog()
                }

            }
            setConfirm2Text(_visibility = View.GONE) {}
            show()
        }

//        context.showDialogOnlyConfirm(
//            title = context.getString(
//                R.string.tittle_update,
//                updateBean.versionNumber
//            ),
//            message = updateBean.description,
//            okText = context.getString(R.string.live_update),
//            isCanceledOnTouchOutside = !updateBean.isForceUpdate
//        ) {
////            if (updateBean.downloadUrl.endsWith(".apk")) {
////                //应用内更新
////                PermissionX.init(context as FragmentActivity)
////                    .permissions(
////                        Manifest.permission.READ_EXTERNAL_STORAGE,
////                        Manifest.permission.WRITE_EXTERNAL_STORAGE
////                    )
////                    .request { allGranted, _, _ ->
////                        if (allGranted) {
////                            UpdateAppUtils.getInstance().apply {
////                                initUtils(context, updateBean)
////                                readyDownloadApk(
////                                    AppUtils.getAppName(),
////                                    context.getString(R.string.desc_update_progress)
////                                )
////                            }
////                        }
////                    }
////            } else {
////                //google商店更新
////                if (!launchAppStoreDetail(
////                        context,
////                        AppUtils.getAppPackageName()
////                    )
////                ) {
////                    ToastUtil.showCenter(context.getString(R.string.tips_no_googlestore))
////                }
////            }
//            //google商店更新
//            if (!launchAppStoreDetail(
//                    context,
//                    AppUtils.getAppPackageName()
//                )
//            ) {
//                ToastUtil.showCenter(context.getString(R.string.tips_no_googlestore))
//            }
//        }
    }

    /**
     * 验证模型邀请码
     */
    fun verifyInviteCode(
        invitationCode: String,
        viewLifecycleOwner: LifecycleOwner,
        onCallback: (isSuccess: Boolean, inviteCodeBean: InviteCodeBean?) -> Unit
    ) {
        val requestBody = JsonObject().let {
            it.addProperty("invite_code", invitationCode)
            it.toString().toRequestBody("application/json".toMediaTypeOrNull())
        }
        apiService.verifyInviteCode(requestBody)
            .convertResponse().funSubscribeRxLife(
                owner = viewLifecycleOwner,
                onNext = {
                    onCallback(true, it)
                },
                onError = {
                    onCallback(false, null)
                    LogUtils.e("onError =-=  $it")
                }
            )
    }

    /**
     * 获取域名
     */
    private fun getDomainName(): String {
        return BuildConfig.HOST_URL.let { url ->
            var resulturl = url
            var temp = resulturl.replace("/", "")
            if (temp.contains(":")) {
                resulturl = temp.substring(temp.indexOf(":") + 1, temp.length)
            }
            resulturl
        }
    }

    /**
     * 文本显示限定长度
     */
    fun textViewShowLimitedLength(content: String, maxLen: Int): String {
        content.isNotNull({
            var count = 0
            var endIndex = 0
            val itemArray = content.toCharArray()
            for (i in content.indices) {
                val item = itemArray[i]
                if (item.code < 128) {
                    count += 1
                } else {
                    count += 2
                }
                if (maxLen == count || (item.code >= 128 && maxLen + 1 == count)) {
                    endIndex = i
                }
            }
            return if (count <= maxLen) {
                content
            } else {
                "${content.substring(0, endIndex)}..."
            }

        }, {
            return content
        })
        return ""
    }

    /**
     * 根据状态栏高度设置控件距离高度
     */
    fun setTopViewHeight(
        mActivity: Activity,
        constraintLayout: ConstraintLayout,
        uiId: Int,
        offSetTop: Int = 0
    ) {
        val constraintUtil = ConstraintUtil(constraintLayout)
        var StatusBarHeight = ImmersionBar.getStatusBarHeight(mActivity)
        with(constraintUtil.begin()) {
            setMarginTop(
                uiId,
                offSetTop + StatusBarHeight
            )
            commit()
        }
    }

    /**
     * 根据导航栏的高度设置控件距离
     */
    fun setBottomViewHeight(
        mActivity: Activity,
        constraintLayout: ConstraintLayout,
        uiId: Int,
        offSetBottom: Int = 0
    ) {
        val constraintUtil = ConstraintUtil(constraintLayout)
//        var StatusBarHeight = ImmersionBar.getStatusBarHeight(mActivity)
        var navigationBarHeight = ImmersionBar.getNavigationBarHeight(mActivity)
        with(constraintUtil.begin()) {
            setMarginBottom(
                uiId,
                offSetBottom + navigationBarHeight
            )
            commit()
        }
    }

    /**
     * APP应用更新，跳转到google市场
     */
    fun launchAppStoreDetail(
        context: Context,
        appPkg: String,
        marketPkg: String = "com.android.vending"
    ): Boolean {
        try {
            if (isBlankPlus(appPkg)) {
                return false
            }
            val uri = Uri.parse("market://details?id=$appPkg")
            val intent = Intent(Intent.ACTION_VIEW, uri)
            intent.setPackage(marketPkg)
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            if (isIntentResolvable(context, intent)) {
                context.startActivity(intent)
            } else {
                return false
            }
        } catch (e: Exception) {
            e.printStackTrace()
            return false
        }
        return true
    }

    /**
     * 判断Intent有没有对应的Activity去处理
     */
    private fun isIntentResolvable(context: Context, intent: Intent): Boolean {
        return intent.resolveActivity(context.packageManager) != null
    }

    /**
     * 获取本应用语言配置数据
     */
    fun getLanguageDatas(context: Context): MutableList<LanguageBean> {
        val LanguageEntries =
            context.resources.getStringArray(R.array.language_entries)
        val LanguageValues =
            context.resources.getStringArray(R.array.language_values)
        val languages = mutableListOf<LanguageBean>()
        for (i in LanguageEntries.indices) {
            languages.add(LanguageBean(LanguageEntries[i], LanguageValues[i]))
        }
        return languages
    }

    /**
     * 获取选择语言条目
     */
    fun getLanguageEntriesFromValues(context: Context): String {

        val LanguageEntries =
            context.resources.getStringArray(R.array.language_entries)
        val LanguageValues =
            context.resources.getStringArray(R.array.language_values)

        val value = PreferencesUtil.getString(PreferencesUtil.Constant.LANGUAGE, "default")

        val index = LanguageValues.indexOf(value)
        return LanguageEntries[index]
    }


    /**
     * 获取当前系统语言格式
     */
    fun getCurrentLanguage(context: Context): String {
        var local = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            context.resources.configuration.locales[0]
        } else {
            context.resources.configuration.locale
        }
        val language = local.language.lowercase()
        var country = local.country
        val script = local.script
        return if (getLanguageDatas(context).size <= 2) {
            "en"
        } else {
            if (language == "zh" && script == "Hant") {//表示是中文繁体
                "$language-HK"
            } else if (language == "zh" && country == "CN") {//中文简体
                "$language-CN"
            } else {//默认是英文
                "en"
            }
        }


        //        var resultLanguage = if (getLanguageDatas(context).size <= 2) {
//            "en"
//        } else if (language == "zh" && script == "Hant") {//表示是中文繁体
//            "$language-HK"
//        } else if (language == "zh" && country == "CN") {//中文简体
//            "$language-CN"
//        } else {//默认是英文
//            "en"
//        }
    }

    /**
     * 数据转换显示
     */
    var dataShowingConversions = { datas: Int ->
        var result = ""
        val myformat = DecimalFormat("0.0")
        result = if (datas in 10000..999999) {
            "${myformat.format(datas.toDouble() / 1000)}K"
        } else if (datas in 1000000..99999999) {
            "${myformat.format(datas.toDouble() / 1000000)}m"
        } else if (datas in 100000000..100000000000) {
            "${myformat.format(datas.toDouble() / 1000000000)}b"
        } else if (datas in 0..9999) {
            datas.toString()
        } else {
            "${myformat.format(datas.toDouble() / 1000000000000)}t"
        }
        result
    }

    /**
     * 退出程序
     */
    private var mExitTime: Long = 0
    fun exitAPP(activity: Activity) {
        // 双击退出程序
        if (System.currentTimeMillis() - mExitTime > 2000) {
            mExitTime = System.currentTimeMillis()
            ToastUtil.showCenter(activity.getString(R.string.tips_exit_app))
        } else {
            activity.finish()
            System.exit(0)
        }
    }

    /**
     * 清空数据，跳转到登录页面
     */
    fun jump2MainPage(activity: Activity) {
        signOut()
        val intent = MainActivity.newIntent(activity)
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
        activity.startActivity(intent)
    }

    private fun signOut() {
        //关闭声文播放器
        EventBus.getDefault().post(UniversalEvent(UniversalEvent.closeAudioPlayer, null))
        EventBus.getDefault().post(UniversalEvent(UniversalEvent.signOut, null))
        //清空与钱包的连接
        DappProxy.instance.removeSessions()
        DappProxy.instance.logout()
        //清空用户数据
        AccountManager.accountManager.upDateToken("")
        ChatManager.instance.onDestroy()
    }

    /**
     * 跳转到权限设置
     */
    fun toPermissionSetting(activity: Activity) {
        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.LOLLIPOP_MR1) {
            toSystemConfig(activity);
        } else {
            try {
                toApplicationInfo(activity);
            } catch (e: Exception) {
                e.printStackTrace()
                toSystemConfig(activity);
            }
        }
    }

    /**
     * 系统设置界面
     */
    private fun toSystemConfig(activity: Activity) {
        try {
            val intent = Intent(Settings.ACTION_SETTINGS)
            activity.startActivity(intent)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    /**
     * 应用信息界面
     */
    fun toApplicationInfo(activity: Activity) {
        Intent().apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
            data = Uri.fromParts("package", activity.packageName, null)
            activity.startActivity(this)
        }
    }

    /**
     * 接收后台提示语
     */
    fun backgroundPrompt(promptJson: Any) {
        val resultJSON = JSONObject(promptJson.toString())
        val errorMsg = resultJSON.optString("error", "")
        if (!isBlankPlus(errorMsg)) {
            ToastUtil.showCenter(errorMsg)
        }
    }


    /**
     *字符串转时间戳
     */
    fun date2TimeStamp(date_str: String, format: String = "yyyy-MM-dd"): Long {
        try {
            val sdf = SimpleDateFormat(format)
            return sdf.parse(date_str).time
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return -1
    }

    /**
     * 个位数补零显示
     */
    fun oneDigitWithZero(param: Int): String {
        var result = param.toString()
        if (param < 10) {
            result = "0${param}"
        }
        return result
    }

    //APP在前端心跳
    private var isAPPForeground_HeartBeat_Disposable: Disposable? = null

    /**
     * APP应用 心跳处理
     */
    fun isAPPForegroundHeartBeat(isforeground: Boolean) {
        if (isforeground) {
            if (AccountManager.accountManager.isLogin()) {
                if (isBlankPlus(isAPPForeground_HeartBeat_Disposable)) {
                    isAPPForeground_HeartBeat_Disposable =
                        Observable.interval(0, 2 * 60, TimeUnit.SECONDS)
                            .compose(observableToMain())
                            .subscribe {
                                val requestBody = JsonObject().let {
                                    it.addProperty("platform", "0")
                                    it.toString()
                                        .toRequestBody("application/json".toMediaTypeOrNull())
                                }
                                apiService.pings(requestBody)
                                    .convertResponse()
                                    .funSubscribeNotLoading(onNext = {

                                    }, onError = {
                                        LogUtils.e("onError =-=  $it")
                                    })
//                            LogUtils.i("=-=  onResume APP是否处在前端  ${AppUtils.isAppForeground()}")
                            }
                }
            } else {
                if (!isBlankPlus(isAPPForeground_HeartBeat_Disposable) &&
                    !isAPPForeground_HeartBeat_Disposable!!.isDisposed
                ) {
                    isAPPForeground_HeartBeat_Disposable!!.dispose()
                    isAPPForeground_HeartBeat_Disposable = null
                }
            }
        } else {
            if (!isBlankPlus(isAPPForeground_HeartBeat_Disposable) &&
                !isAPPForeground_HeartBeat_Disposable!!.isDisposed
            ) {
                isAPPForeground_HeartBeat_Disposable!!.dispose()
                isAPPForeground_HeartBeat_Disposable = null
            }
        }
    }

    /**  google fcm message  start **/

    /**
     * 启用/停用 FCM 自动初始化功能
     */
    fun runtimeEnableAutoInit(enable: Boolean = true) {
        // [START fcm_runtime_enable_auto_init]
        Firebase.messaging.isAutoInitEnabled = enable
        // [END fcm_runtime_enable_auto_init]
    }

    /**
     * 启用 Analytics 数据收集
     */
    fun analyticsCollectioInit(context: Context, enable: Boolean = true) {
        FirebaseAnalytics.getInstance(context).setAnalyticsCollectionEnabled(enable)
    }

    /**
     * 上报设备 google fcm token
     */
    fun reportDeviceToken() {
        val token = accountManager.getAccountData().accessToken
        val googleFcmMessageTokenIsbind =
            PreferencesUtil.getBool(PreferencesUtil.Constant.GOOGLE_FCM_MESSAGE_TOKEN_ISBIND, false)
        val googleFcmMessageToken =
            PreferencesUtil.getString(PreferencesUtil.Constant.GOOGLE_FCM_MESSAGE_TOKEN, "")

        LogUtils.i(
            "token =-= $token",
            "googleFcmMessageTokenIsbind =-= $googleFcmMessageTokenIsbind",
            "googleFcmMessageToken =-= $googleFcmMessageToken"
        )

//        if (!isBlankPlus(token)) {
//            apiService.reportDeviceToken(
//                PreferencesUtil.getString(
//                    PreferencesUtil.Constant.GOOGLE_FCM_MESSAGE_TOKEN,
//                    ""
//                )
//            )
//                .convertResponse()
//                .funSubscribe(
//                    showLoading = false,
//                    onNext = {
////                        PreferencesUtil.putBool(
////                            PreferencesUtil.Constant.GOOGLE_FCM_MESSAGE_TOKEN_ISBIND,
////                            true
////                        )
//                        LogUtils.i(
//                            " =-= 设备token 已经被绑定，谢谢 "
//                        )
//                    },
//                    onError = {
//                        LogUtils.e("onError =-=  $it")
//                    }
//                )
//        }

        if (!isBlankPlus(token) && !googleFcmMessageTokenIsbind && !isBlankPlus(
                googleFcmMessageToken
            )
        ) {
            apiService.reportDeviceToken(googleFcmMessageToken)
                .convertResponse()
                .funSubscribe(
                    showLoading = false,
                    onNext = {
                        LogUtils.i("=-= google fcm 已经绑定")
                        PreferencesUtil.putBool(
                            PreferencesUtil.Constant.GOOGLE_FCM_MESSAGE_TOKEN_ISBIND,
                            true
                        )
                    },
                    onError = {
                        LogUtils.e("onError =-=  $it")
                    }
                )
        }

    }
    /**  google fcm message  end **/
}