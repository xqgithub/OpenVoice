package com.shannon.openvoice.model

import android.accounts.Account
import com.google.gson.annotations.SerializedName
import java.util.*

/**
 * Date:2022/8/17
 * Time:11:41
 * author:dimple
 * 用户信息实体类
 */
data class AccountBean(
    var id: String = "",
    var accessToken: String = "",//用户登录后的token
    @SerializedName("username") var username: String = "",//用户账号
//    @SerializedName("acct") var username: String = "",
    @SerializedName("display_name") var displayName: String = "", //用户昵称
    var note: String = "",
    var url: String = "",
    var avatar: String = "",
    var header: String = "",
    var locked: Boolean = false,
    @SerializedName("followers_count") var followersCount: Int = 0,
    @SerializedName("following_count") var followingCount: Int = 0,
    @SerializedName("statuses_count") var statusesCount: Int = 0,
    @SerializedName("voice_model_count") var voiceModelCount: Int = 0,//拥有的模型数
    @SerializedName("is_suspended") var suspended: Boolean = false,//是否被注销
    var source: AccountSource? = null,
    var bot: Boolean = false,
    var emojis: List<Emoji>? = emptyList(), // nullable for backward compatibility
    var fields: List<Field>? = emptyList(), // nullable for backward compatibility
    var moved: Account? = null,
    var birthday: Date? = null,
    var email: String = "",
    @SerializedName("profile_completed") var profileCompleted: Boolean = true,
    var provider: String? = null,// email,wallet
    @SerializedName("wallet_address") var walletAddress: String? = null,

    ) {
    val name: String
        get() = if (displayName.isNullOrEmpty()) {
            username
        } else displayName
//    fun isRemote(): Boolean = this.username != this.localUsername
}

data class AccountSource(
    val privacy: StatusModel.Visibility?,
    val sensitive: Boolean?,
    val note: String?,
    val fields: List<StringField>?
)

data class Field(
    val name: String,
    val value: String,
    @SerializedName("verified_at") val verifiedAt: Date?
)

data class StringField(
    val name: String,
    val value: String
)

