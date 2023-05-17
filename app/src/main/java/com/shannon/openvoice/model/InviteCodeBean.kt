package com.shannon.openvoice.model

import com.google.gson.annotations.SerializedName

/**
 * Date:2022/11/2
 * Time:11:24
 * author:dimple
 * 邀请码实体类
 */
data class InviteCodeBean(
    val id: Int,
    @SerializedName("invite_code")
    val inviteCode: String
)
