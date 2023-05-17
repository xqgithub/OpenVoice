package com.shannon.openvoice.business.player

import android.os.Parcelable
import android.text.Spanned
import com.shannon.openvoice.model.HashTag
import com.shannon.openvoice.model.StatusModel
import kotlinx.parcelize.Parcelize

/**
 *
 * @Package:        com.shannon.openvoice.business.player
 * @ClassName:      MediaData
 * @Description:     作用描述
 * @Author:         czhen
 * @CreateDate:     2023/3/14 15:09
 */
@Parcelize
data class MediaInfo(
    val id: String,
    val resUrl: String,
    val avatar: String,
    val statusModel: StatusModel? = null
) : Parcelable {

}