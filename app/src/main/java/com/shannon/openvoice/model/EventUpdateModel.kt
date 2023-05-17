package com.shannon.openvoice.model

/**
 *
 * @Package:        com.shannon.openvoice.model
 * @ClassName:      EventUpdateModel
 * @Description:     作用描述
 * @Author:         czhen
 * @CreateDate:     2022/9/8 15:17
 */
data class EventUpdateModel(val modelId: Long, val price: String)

data class EventDeleteModel(val modelId: Long)
