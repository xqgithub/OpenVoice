package com.shannon.openvoice.model

import com.google.gson.annotations.SerializedName

/**
 * Date:2022/8/24
 * Time:10:49
 * author:dimple
 *
 */
data class oauth2Bean(
    val id: String,
    val name: String,//您的应用程序的名称。
    var website: String? = null,//与您的应用程序关联的网站。
    @SerializedName("redirect_uri") var redirectUri: String = "",
    @SerializedName("client_id") val clientId: String,//客户端 ID 密钥，用于获取 OAuth 令牌
    @SerializedName("client_secret") val clientSecret: String,//客户端密钥，用于获取 OAuth 令牌
    @SerializedName("vapid_key")  val vapidKey: String,//Used for Push Streaming API. Returned with POST /api/v1/apps. Equivalent to PushSubscription#server_key
)