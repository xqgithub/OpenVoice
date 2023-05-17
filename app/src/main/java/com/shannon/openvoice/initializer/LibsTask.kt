package com.shannon.openvoice.initializer

import android.content.Intent
import com.amazonaws.mobileconnectors.s3.transferutility.TransferService
import com.blankj.utilcode.util.LogUtils
import com.shannon.android.lib.util.CrashHandler
import com.shannon.android.lib.util.ToastUtil
import com.shannon.openvoice.BuildConfig
import com.shannon.openvoice.FunApplication

/**
 *
 * @ProjectName:    OpenVoice
 * @Package:        com.shannon.openvoice.initializer
 * @ClassName:      LibsTask
 * @Description:     一些三方库的初始化，对于比较耗时的Lib应该独立Task
 * @Author:         czhen
 * @CreateDate:     2022/7/20 15:37
 */
class LibsTask : FunTask() {

    override fun run() {
        ToastUtil.init(FunApplication.getInstance())

        //加载全部异常捕获
        CrashHandler.getInstance().init(FunApplication.getInstance().applicationContext)
        //启动 AWS TransferService服务
        FunApplication.getInstance().applicationContext.startService(
            Intent(
                FunApplication.getInstance().applicationContext,
                TransferService::class.java
            )
        )
        //设置日志文件开关
        LogUtils.getConfig().isLogSwitch = BuildConfig.DEBUG
    }
}