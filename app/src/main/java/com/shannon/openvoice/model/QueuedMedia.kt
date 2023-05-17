package com.shannon.openvoice.model

import android.net.Uri

/**
 *
 * @Package:        com.shannon.openvoice.model
 * @ClassName:      QueuedMedia
 * @Description:     作用描述
 * @Author:         czhen
 * @CreateDate:     2022/8/11 11:19
 */
data class QueuedMedia(
    val localId: Int,
    val uri: Uri,
    val type: Type,
    val mediaSize: Long,
    val uploadPercent: Int = 0,
    val id: String? = null
) {
    enum class Type {
        IMAGE, VIDEO;
    }
}
