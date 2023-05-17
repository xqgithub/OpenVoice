package com.shannon.plugin.version

import java.text.SimpleDateFormat

/**
 *
 * @ProjectName:    OpenVoice
 * @Package:        com.shannon.plugin.version
 * @ClassName:      AndroidConfig
 * @Description:     java类作用描述
 * @Author:         czhen
 * @CreateDate:     2022/7/15 17:54
 */
object AndroidConfig {

    const val appName = "OpenVoice"
    const val applicationId = "com.shannon.openvoice"
    const val compileSdkVersion = 33
    const val minSdkVersion = 21
    const val targetSdkVersion = 33
    const val versionCode = 17
    const val versionName = "1.0.8"
    const val versionBuild = " Build 48"

    val baleDate: String = SimpleDateFormat("MMddHHmm").format(System.currentTimeMillis())

}