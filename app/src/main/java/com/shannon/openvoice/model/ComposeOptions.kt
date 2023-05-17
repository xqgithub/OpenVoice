package com.shannon.openvoice.model

import android.os.Parcelable
import com.shannon.openvoice.db.DraftAttachment
import kotlinx.parcelize.Parcelize

/**
 *
 * @Package:        com.shannon.openvoice.model
 * @ClassName:      ComposeOptions
 * @Description:     作用描述
 * @Author:         czhen
 * @CreateDate:     2022/8/15 14:55
 */
@Parcelize
data class ComposeOptions(
    var draftId: Int? = null,
    var content: String? = null,
    var mentionedUsernames: Set<String>? = null,
    var visibility: StatusModel.Visibility? = null,
    var inReplyToId: String? = null,
    var replyingStatusAuthor: String? = null,
    var mediaAttachments: List<Attachment>? = null,
    var draftAttachments: List<DraftAttachment>? = null,
    var voiceModelId: Long,
    var modifiedInitialState: Boolean? = null//删除并重新编辑
) : Parcelable