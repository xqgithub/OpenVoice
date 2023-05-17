package com.shannon.openvoice.model

import com.google.gson.annotations.SerializedName
import java.util.*

/**
 *
 * @Package:        com.shannon.openvoice.model
 * @ClassName:      WorkLikes
 * @Description:     作用描述
 * @Author:         czhen
 * @CreateDate:     2022/9/9 9:30
 */
data class WorkLikes(
    val id: Long,
    val account: TimelineAccount,
    @SerializedName("like_at") val likeAt: Date,
    @SerializedName("voice_model") var model: StatusModel.VoiceModel?
)
