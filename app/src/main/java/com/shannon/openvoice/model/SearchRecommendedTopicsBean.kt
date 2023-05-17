package com.shannon.openvoice.model

import android.os.Parcelable
import com.shannon.android.lib.extended.isBlankPlus
import kotlinx.parcelize.Parcelize

/**
 * Date:2023/3/10
 * Time:14:02
 * author:dimple
 * 搜索模块，推荐话题实体类
 */
@Parcelize
data class SearchRecommendedTopicsBean(
    val hashtags: List<HashTag>,
    val voices: List<StatusModel>? = null//声文数据
) : Parcelable {
    val statuses: List<StatusModel>
        get() = if (isBlankPlus(voices)) {
            arrayListOf()
        } else {
            voices!!
        }
}