package com.shannon.android.lib.util

import android.content.Context
import android.content.res.Configuration
import com.shannon.android.lib.BuildConfig
import com.shannon.android.lib.R
import com.shannon.android.lib.constant.ConfigConstants
import com.shannon.android.lib.util.PreferencesUtil
import java.util.*

/**
 * Date:2022/8/3
 * Time:17:01
 * author:dimple
 * 多语言管理
 */
class LocaleManager(var context: Context) {

    fun setLocale(context: Context): Context {
        val defaultLanguage =
            if (BuildConfig.BUILD_TYPE == ConfigConstants.BuildType.RELEASE) "en" else "default"
        //出包的时候根据业务需求修改默认值
        val language = PreferencesUtil.getString(PreferencesUtil.Constant.LANGUAGE, defaultLanguage)

        if (language == "default") {
            return context
        }
        val locale = Locale.forLanguageTag(language)
        Locale.setDefault(locale)

        val res = context.resources
        val config = Configuration(res.configuration)
        config.setLocale(locale)
        return context.createConfigurationContext(config)
    }


}