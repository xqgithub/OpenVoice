package com.shannon.openvoice.business.player.listener

import com.shannon.openvoice.business.player.MediaInfo
import com.shannon.openvoice.model.StatusModel

/**
 *
 * @Package:        com.shannon.openvoice.business.player
 * @ClassName:      PlayerListener
 * @Description:     作用描述
 * @Author:         czhen
 * @CreateDate:     2023/3/13 11:17
 */
interface PlayerListener {

    fun onMediaItemTransition(item: MediaInfo)

    fun onIsPlayingChanged(isPlaying: Boolean)

    fun onPlayState(isSkipToPreviousEnabled: Boolean, isSkipToNextEnabled: Boolean)

    fun onIsLoading(isLoading: Boolean)

    fun onMediaDuration(mediaDuration: Long)

    fun onMediaProgress(mediaProgress: Int)

    fun playListEmpty()

    fun onCleared()

}