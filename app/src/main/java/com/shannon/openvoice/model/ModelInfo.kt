package com.shannon.openvoice.model

/**
 *
 * @Package:        com.shannon.openvoice.model
 * @ClassName:      ModelInfo
 * @Description:     作用描述
 * @Author:         czhen
 * @CreateDate:     2022/8/17 17:47
 */
data class ModelInfo(
    val modelId: Long,
    val modelName: String,
    val creator: String,
    val currentPrice: String,
    val currency: String
)
