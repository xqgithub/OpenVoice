package com.shannon.openvoice.initializer

import com.shannon.openvoice.BuildConfig
import timber.log.Timber

/**
 *
 * @ProjectName:    OpenVoice
 * @Package:        com.shannon.openvoice.initializer
 * @ClassName:      TimberTask
 * @Description:     日志工具初始化
 * @Author:         czhen
 * @CreateDate:     2022/7/20 15:43
 */
class TimberTask : FunTask() {

    override fun run() {
        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        } else {
            Timber.plant(CrashReportingTree())
        }
    }

    private class CrashReportingTree : Timber.Tree() {
        override fun log(priority: Int, tag: String?, message: String, t: Throwable?) {
        }

    }
}