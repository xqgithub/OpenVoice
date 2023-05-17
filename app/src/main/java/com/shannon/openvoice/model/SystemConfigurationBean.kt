package com.shannon.openvoice.model

import com.google.gson.annotations.SerializedName

/**
 * Date:2022/11/2
 * Time:9:36
 * author:dimple
 * 系统配置项
 */
data class SystemConfigurationBean(
    //模型邀请码功能是否开启
    @SerializedName("voice_model_invite_enabled")
    val voiceModelInviteEnabled: Boolean,
    //系统公告是否启用
    @SerializedName("system_announcement_enabled")
    val systemAnnouncementEnabled: Boolean,
    //系统公告
    @SerializedName("system_announcement")
    val systemAnnouncement: String,
    //系统公告
    @SerializedName("activity_url")
    val activityUrl: String
)
