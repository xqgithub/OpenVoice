package com.shannon.openvoice.model

import com.google.gson.annotations.SerializedName
import java.util.*

/**
 *
 * @Package:        com.shannon.openvoice.model
 * @ClassName:      Likes
 * @Description:     作用描述
 * @Author:         czhen
 * @CreateDate:     2022/8/24 13:48
 */
data class ModelLikes(
    val id: Long,
    val account: TimelineAccount,
    @SerializedName("like_at") val likeAt: Date,
    @SerializedName("following")  val isFollow:Boolean
)
