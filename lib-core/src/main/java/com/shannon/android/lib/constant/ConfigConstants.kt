package com.shannon.android.lib.constant

import android.annotation.SuppressLint
import android.app.Application
import com.shannon.android.lib.util.LocaleManager

/**
 * Date:2022/7/29
 * Time:13:47
 * author:dimple
 * 全局【常量】或【变量】配置说明
 */
object ConfigConstants {

    /** 常量 **/
    object CONSTANT {
        /** TAG名称 **/
        const val TAG_ALL = "open_voice_tag"

        /** 错误日志名称 **/
        const val ERROR_JOURNAL = "AOpenVoiceErrorLog"

        /** 接口日志名称 **/
        const val interface_log = "interface_log"

        /** UI设计标准 **/
        //pad的设计图宽
        const val PAD_WIDTH = 1024

        //pad的设计图高
        const val PAD_HEIGHT = 768

        //phone的设计图宽
        const val PHONE_WIDTH = 375

        //phone的设计图高
        const val PHONE_HEIGHT = 812

        /** 页面类型 **/
        const val activityType = "activitytype"

        /** 关注者或追随者类型 **/
        const val userIdentitType = "userIdentitType"

        /** 获取oauth2的参数 **/
        const val scope = "read write follow push"

        /** 广播action字段 **/
        //关闭广播
        const val NOTIFACATIO_CLOSE = "notifacatio_close"

        /** 更新文件下载的APK 保存目录 **/
        const val APK_DIR = "AOpenVoiceApk"


        /** web url&title **/
        const val webTitleName = "webTitleName"
        const val webUrl = "webUrl"

        /** 用户协议 **/
        const val userAgreementValue =
            "https://www.openvoice.club/agreement.html"

        /** 隐私政策 **/
        const val privacyPolicyValue =
            "https://www.openvoice.club/privacy.html"

        /** 用户注销协议 **/
        const val userLogoutAgreementValue =
            "https://www.openvoice.club/cancellation.html"

        /** 发布声音协议 **/
        const val reminderValue =
            "https://www.openvoiceover.com/reminder.html"
    }

    /** 变量 **/
    object VARIABLE {
        /** 全局Application
         * 用于除主程序外（主程序直接使用FunApplication.getInstance()），需要引用Application的地方
         * **/
        var globalApplication: Application? = null

        /**
         * app应用多语言管理
         */
        @SuppressLint("StaticFieldLeak")
        lateinit var localeManager: LocaleManager

        /**
         * 首页升级弹框标识，确保整个过程只显示一次
         */
        var isUpgradePopupDisplay = true

    }

    object BuildType {
        const val RELEASE = "release"
        const val DEBUG = "debug"
        const val FOX = "fox"
        const val RELEASE_TEST = "releaseTest"
    }
}