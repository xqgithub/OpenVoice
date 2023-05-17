package com.shannon.openvoice.model

import android.os.Parcelable
import android.text.Spanned
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.IgnoredOnParcel
import kotlinx.parcelize.Parcelize

/**
 *
 * @ProjectName:    OpenVoice
 * @Package:        com.shannon.openvoice.model
 * @ClassName:      TimelineAccount
 * @Description:     java类作用描述
 * @Author:         czhen
 * @CreateDate:     2022/8/1 10:43
 */
@Parcelize
data class TimelineAccount(
    val id: String,
    @SerializedName("username") val localUsername: String,
    @SerializedName("acct") val username: String,
    @SerializedName("display_name") val displayName: String?,
    @SerializedName("last_status_at") val lastStatusAt: String?,
    val url: String,
    val avatar: String,
    val bot: Boolean = false,
    var following: Boolean
) : Parcelable {
    val name: String
        get() = if (displayName.isNullOrEmpty()) {
            localUsername
        } else {
            displayName
        }

    @IgnoredOnParcel
    var fullName: Spanned? = null
}
