package com.shannon.openvoice.model

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

/**
 *
 * @ProjectName:    OpenVoice
 * @Package:        com.shannon.openvoice.model
 * @ClassName:      HashTag
 * @Description:     话题
 * @Author:         czhen
 * @CreateDate:     2022/8/1 11:48
 */
@Parcelize
data class HashTag(
    val name: String,
    val url: String,
    @SerializedName("statuses_count") val statusesCount: Int,
) : Parcelable
