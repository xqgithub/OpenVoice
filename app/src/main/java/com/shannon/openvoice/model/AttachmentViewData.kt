package com.shannon.openvoice.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

/**
 * Date:2022/8/23
 * Time:13:58
 * author:dimple
 */
@Parcelize
data class AttachmentViewData(
    val attachment: Attachment,
    val statusId: String,
    val statusUrl: String
) : Parcelable {
    companion object {
        @JvmStatic
        fun list(status: StatusModel): List<AttachmentViewData> {
            val actionable = status.actionableStatus
            return actionable.attachments.map {
                AttachmentViewData(it, actionable.id, actionable.url!!)
            }
        }
    }
}
