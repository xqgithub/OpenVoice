package com.shannon.openvoice.business.player.listener

import com.shannon.openvoice.business.player.MediaInfo

/**
 *
 * @Package:        com.shannon.openvoice.business.player.listener
 * @ClassName:      OnPlaylistListener
 * @Description:     作用描述
 * @Author:         czhen
 * @CreateDate:     2023/3/13 14:10
 */
interface OnPlaylistListener {

    fun onIsPlayingChanged(item: MediaInfo, isPlaying: Boolean)

    fun onPlaylistDestroy(item: MediaInfo)

    fun navigateTo(item: MediaInfo)

}