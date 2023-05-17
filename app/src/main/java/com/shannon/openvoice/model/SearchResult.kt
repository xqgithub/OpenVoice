package com.shannon.openvoice.model

import com.google.gson.annotations.SerializedName

/**
 *
 * @Package:        com.shannon.openvoice.model
 * @ClassName:      SearchResult
 * @Description:     搜索结果集
 * @Author:         czhen
 * @CreateDate:     2022/8/10 14:35
 */
data class SearchResult(
    val accounts: List<TimelineAccount>,//用户数据
    val statuses: List<StatusModel>,//声文数据
    val hashtags: List<HashTag>,//话题数据
    @SerializedName("voice_models") val voiceModels: List<SearchVoiceModels>,//声音模型
)
