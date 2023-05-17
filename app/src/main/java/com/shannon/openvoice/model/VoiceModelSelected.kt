package com.shannon.openvoice.model

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

/**
 * Date:2022/11/3
 * Time:14:25
 * author:dimple
 * 发布页面，被选中的模型
 */
@Parcelize
data class VoiceModelSelected(
    val selectedPosition: Int,
    var models: List<VoiceModelResult>
) : Parcelable
