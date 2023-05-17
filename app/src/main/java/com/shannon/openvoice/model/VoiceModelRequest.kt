package com.shannon.openvoice.model

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

/**
 *
 * @ClassName:      VoiceModelResult
 * @Description:     java类作用描述
 * @Author:         czhen
 */
@Parcelize
data class VoiceModelRequest(
    @SerializedName("oss_objects") val ossObj: List<String>,
    val price: String,
    val currency: String?,
    val address: String,
    @SerializedName("royalties_fee") val royaltiesFee: String,
    @SerializedName("invite_id") val invite_id: Int
) : Parcelable
