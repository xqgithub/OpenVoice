package com.shannon.openvoice.model

import com.google.gson.annotations.SerializedName

/**
 * Date:2022/9/14
 * Time:9:48
 * author:dimple
 * 检查更新实体类
 */
data class UpdateBean(
    @SerializedName("version_number") val versionNumber: String,//app版本号
    val description: String,//更新描述
    val platform: String,//平台 android/ios
    @SerializedName("download_url") val downloadUrl: String,//下载地址
    @SerializedName("is_force_update") val isForceUpdate: Boolean,//是否强制更新
    @SerializedName("is_latest_version") val isLatestVersion: Boolean//是否最新版本
)
