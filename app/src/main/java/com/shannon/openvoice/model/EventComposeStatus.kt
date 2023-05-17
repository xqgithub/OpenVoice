package com.shannon.openvoice.model

import com.shannon.openvoice.business.timeline.TimelineViewModel

/**
 *
 * @Package:        com.shannon.openvoice.model
 * @ClassName:      EventBlackList
 * @Description:     作用描述
 * @Author:         czhen
 * @CreateDate:     2022/8/9 16:28
 */
data class EventComposeStatus(
    val fromPage: TimelineViewModel.Kind,
    val type: Type,
    val status: StatusModel
) {

    enum class Type {
        CREATE,
        REPLAY,
        DELETE,
        REBLOG,
        FAVOURITES,
        PIN,
        VOICE_MODEL
    }
}
