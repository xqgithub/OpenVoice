package com.shannon.openvoice.model

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import com.shannon.android.lib.extended.isBlankPlus
import kotlinx.parcelize.Parcelize

/**
 * Date:2023/3/13
 * Time:11:12
 * author:dimple
 * 搜索模块，推荐模型实体类
 */
data class SearchRecommendedModelsBean(
    @SerializedName("voice_models") val voiceModels: List<SearchVoiceModels>,//声音模型
    val voices: List<StatusModel>? = null//声文数据
) {
    val statuses: List<StatusModel>
        get() = if (isBlankPlus(voices)) {
            arrayListOf()
        } else {
            voices!!
        }
}