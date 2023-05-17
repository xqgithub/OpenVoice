package com.shannon.openvoice.model

import com.google.gson.annotations.SerializedName

/**
 *
 * @Package:        com.shannon.openvoice.model
 * @ClassName:      NewVoiceCover
 * @Description:     作用描述
 * @Author:         czhen
 * @CreateDate:     2022/8/12 14:53
 */
data class NewVoiceCover(
    val status: String,
    @SerializedName("in_reply_to_id") val inReplyToId: String?,
    val visibility: String,
    @SerializedName("media_ids") val mediaIds: List<String>?,
    @SerializedName("voice_model_id") val voiceModelId: Long
)