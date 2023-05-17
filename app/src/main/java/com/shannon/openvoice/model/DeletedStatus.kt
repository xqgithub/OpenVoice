package com.shannon.openvoice.model

import com.google.gson.annotations.SerializedName
import java.util.*

/**
 *
 * @Package:        com.shannon.openvoice.model
 * @ClassName:      DeletedStatus
 * @Description:     作用描述
 * @Author:         czhen
 * @CreateDate:     2022/8/9 15:09
 */
data class DeletedStatus(
    var text: String?,
    @SerializedName("in_reply_to_id") var inReplyToId: String?,
    @SerializedName("spoiler_text") val spoilerText: String,
    val visibility: StatusModel.Visibility,
    val sensitive: Boolean,
    @SerializedName("media_attachments") var attachments: ArrayList<Attachment>?,
    @SerializedName("created_at") val createdAt: Date
) {
    fun isEmpty(): Boolean {
        return text == null && attachments == null
    }
}
