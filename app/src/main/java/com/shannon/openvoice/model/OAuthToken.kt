package com.shannon.openvoice.model

import com.google.gson.annotations.SerializedName

/**
 * Date:2022/8/24
 * Time:14:32
 * author:dimple
 * 获取应用token
 */
data class OAuthToken(
    @SerializedName("access_token") val appToken: String,
    @SerializedName("token_type") val tokenType: String,
    val scope: String,
    @SerializedName("created_at") val createdAt: String,
)