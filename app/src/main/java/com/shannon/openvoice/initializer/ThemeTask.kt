package com.shannon.openvoice.initializer

import com.shannon.android.lib.util.ThemeUtil
import com.shannon.openvoice.FunApplication
import com.shannon.android.lib.util.PreferencesUtil

/**
 *
 * @ProjectName:    OpenVoice
 * @Package:        com.shannon.openvoice.initializer
 * @ClassName:      ThemeTask
 * @Description:     主题设置
 * @Author:         czhen
 * @CreateDate:     2022/7/22 10:14
 */
class ThemeTask : FunTask() {

    override fun run() {
        val themeMode = PreferencesUtil.getString(
            PreferencesUtil.Constant.THEME_MODE,
            ThemeUtil.APP_THEME_DEFAULT
        )
        ThemeUtil.setAppNightMode(themeMode)
    }
}