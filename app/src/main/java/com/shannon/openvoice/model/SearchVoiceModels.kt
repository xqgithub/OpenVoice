package com.shannon.openvoice.model

import com.google.gson.annotations.SerializedName

/**
 * Date:2022/9/6
 * Time:10:50
 * author:dimple
 * 搜索模型数据
 */
data class SearchVoiceModels(
    val id: Long,
    val name: String,//模型名称
    @SerializedName("model_type") val modelType: String,//模型类型 offical/官方 user/用户
    @SerializedName("model_source") val modelSource: String,//模型来源 offical/官方 original/原创 purchase/购买
    val status: String,//模型状态 in_progress/生成中 failed/生成失败 unused/可使用 used/使用中
    val price: String?,//价格
    val currency: String?,//货币
    @SerializedName("like_count") val likeCount: Int,//喜欢次数
    @SerializedName("usage_count") val usageCount: Int,//使用次数
    @SerializedName("trading_count") val tradingCount: Long,//交易次数
    @SerializedName("is_tradeable") val isTradeable: Boolean,//是否可以交易
    @SerializedName("is_triable") val isTriable: Boolean,//是否可以试听
    @SerializedName("owner_account") val account: TimelineAccount,//拥有人账号
    @SerializedName("is_official") val isOfficial: Boolean,
    val activated: Boolean,
    @SerializedName("is_model_owner") val isModelOwner: Boolean,
    @SerializedName("is_liked") val isLiked: Boolean,//是否喜欢了该模型
    @SerializedName("payload") val payload: String?,
    @SerializedName("audition_file") val auditionFile: String?,//官方模型的试听文件

)
