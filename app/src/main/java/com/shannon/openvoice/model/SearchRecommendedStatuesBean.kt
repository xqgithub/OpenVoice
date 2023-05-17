package com.shannon.openvoice.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

/**
 * Date:2023/3/13
 * Time:10:34
 * author:dimple
 * 搜索模块，推荐声文实体类
 */
@Parcelize
data class SearchRecommendedStatuesBean(
    val isRecommended: Boolean = false,
    val statuses: List<StatusModel>//声文数据
) : Parcelable