package com.shannon.android.lib.player.media

/**
 *
 * @ProjectName:    MusicDemo
 * @Package:        com.shannon.library.music.media
 * @ClassName:      MediaPlayerListener
 * @Description:     java类作用描述
 * @Author:         czhen
 * @CreateDate:     2022/6/8 15:43
 */
interface MediaPlayerListener {

    fun onConnected()

    fun onMediaItemTransition(mediaId: String)

    fun onPlayState(hasPreviousItem: Boolean, hasNextItem: Boolean)

    fun onIsPlayingChanged(isPlaying: Boolean)

    fun onIsLoadingChanged(isLoading: Boolean)

    fun onMediaDuration(mediaDuration: Long)

    fun onMediaProgress(mediaProgress: Int)

    fun onPlaylistUnselected()

    fun playListEmpty()

    fun onCleared()

    fun isPlaylistEmpty(): Boolean
}