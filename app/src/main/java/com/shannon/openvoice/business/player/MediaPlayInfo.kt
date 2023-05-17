package com.shannon.openvoice.business.player

import com.shannon.openvoice.model.StatusModel

/**
 *
 * @Package:        com.shannon.openvoice.business.player
 * @ClassName:      MediaPlayInfo
 * @Description:     作用描述
 * @Author:         czhen
 * @CreateDate:     2023/3/13 15:07
 */
data class MediaPlayInfo(
    var currentStatus: MediaInfo? = null,
    var hasPreviousItem: Boolean = false,
    var hasNextItem: Boolean = false,
    var isPlaying: Boolean = false,
    var isLoading: Boolean = false,
    var mediaDuration: Long = 0,
    var mediaProgress: Int = 0
){
    fun clear(){
        currentStatus = null
        hasPreviousItem = false
        hasNextItem = false
        isPlaying = false
        isLoading = false
        mediaDuration = 0
        mediaProgress = 0
    }

}
