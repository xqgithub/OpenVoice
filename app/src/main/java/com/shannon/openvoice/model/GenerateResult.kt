package com.shannon.openvoice.model

import com.google.gson.annotations.SerializedName

/**
 *
 * @Package:        com.shannon.openvoice.model
 * @ClassName:      GenerateResult
 * @Description:     作用描述
 * @Author:         czhen
 * @CreateDate:     2022/9/21 19:50
 */
data class GenerateResult(
    val id: String,
    @SerializedName("tts_generated") val ttsGenerated: Boolean,
    @SerializedName("oss_id") val ossId: String
)