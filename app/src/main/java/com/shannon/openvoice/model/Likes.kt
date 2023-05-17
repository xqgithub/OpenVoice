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
data class Likes(
    val id: Long,
    @SerializedName("model_name") val modelName: String,
    val isLiked: Boolean,
    val account: TimelineAccount,
    @SerializedName("like_at") val likeAt: Date,
    @SerializedName("model_activated") var activated: Boolean?,
    @SerializedName("is_model_owner") val modelOwner: Boolean,
    @SerializedName("model_id") val modelId: Long,
    val price: String?,
    val currency: String?,
    val isOfficial: Boolean,
    val isFollow: Boolean = false,
    val usageCount: Long
) {
    val isModelOwner: Boolean
        get() {
            return modelOwner || isOfficial
        }
}
