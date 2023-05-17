package com.shannon.android.lib.util

import android.content.Context
import com.shannon.android.lib.extended.nonNull
import com.tencent.mmkv.MMKV

/**
 *
 * @ProjectName:    OpenVoice
 * @Package:        com.shannon.openvoice.util
 * @ClassName:      PreferencesUtil
 * @Description:     本地数据存储
 * @Author:         czhen
 * @CreateDate:     2022/7/22 9:53
 */
object PreferencesUtil {

    fun initialize(context: Context) {
        MMKV.initialize(context)
    }

    fun putString(key: String, value: String) {
        MMKV.defaultMMKV().encode(key, value)
    }

    fun getString(key: String, defaultValue: String): String {
        return MMKV.defaultMMKV().decodeString(key, defaultValue).nonNull(defaultValue)
    }

    fun putInt(key: String, value: Int) {
        MMKV.defaultMMKV().encode(key, value)
    }

    fun getInt(key: String, defaultValue: Int = 0): Int {
        return MMKV.defaultMMKV().decodeInt(key, defaultValue)
    }

    fun putBool(key: String, value: Boolean) {
        MMKV.defaultMMKV().encode(key, value)
    }

    fun getBool(key: String, defaultValue: Boolean = false): Boolean {
        return MMKV.defaultMMKV().decodeBool(key, defaultValue)
    }

    fun putLong(key: String, value: Long) {
        MMKV.defaultMMKV().encode(key, value)
    }

    fun getLong(key: String, defaultValue: Long = 0L): Long {
        return MMKV.defaultMMKV().decodeLong(key, defaultValue)
    }

    fun putDouble(key: String, value: Double) {
        MMKV.defaultMMKV().encode(key, value)
    }

    fun getDouble(key: String, defaultValue: Double = 0.0): Double {
        return MMKV.defaultMMKV().decodeDouble(key, defaultValue)
    }

    fun putFloat(key: String, value: Float) {
        MMKV.defaultMMKV().encode(key, value)
    }

    fun getFloat(key: String, defaultValue: Float = 0.0f): Float {
        return MMKV.defaultMMKV().decodeFloat(key, defaultValue)
    }

    object Constant {
        const val THEME_MODE = "themeMode"
        const val KEY_SPEED = "speed"
        const val KEY_PLAY_MODE = "playMode"
        const val LANGUAGE = "language"
        const val ACCOUNT_DATA = "accountData"
        const val CLIENT_ID = "clientId"
        const val CLIENT_SECRET = "clientSecret"
        const val APP_TOKEN = "appToken"
        const val NOTIFICATION_FILTER = "notificationFilter"
        const val ALREADY_LOGGED_REGISTERED_MAIL = "alreadyLoggedRegisteredMail"
        const val RECOMMENDED_PAGE_DISPLAYED_STATUS = "recommendedPageDisplayedStatus"
        const val AUTHORING_PAGE_DISPLAYED_STATUS = "authoringPageDisplayedStatus"
        const val VOICE_MODEL_INVITE_ENABLED = "voiceModelInviteEnabled"
        const val RELEASE_AGREEMENT_DISPLAYED_STATUS = "releaseAgreementDisplayedStatus"
        const val SYSTEM_ANNOUNCEMENT_ENABLED = "systemAnnouncementEnabled"
        const val SYSTEM_ANNOUNCEMENT = "systemAnnouncement"
        const val ACTIVITY_URL = "activityUrl"
        const val GOOGLE_FCM_MESSAGE_TOKEN = "googleFcmMessageToken"
        const val GOOGLE_FCM_MESSAGE_TOKEN_ISBIND = "googleFcmMessageTokenIsbind"
    }
}