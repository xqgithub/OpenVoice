package com.shannon.openvoice.business.player.listener

import com.shannon.android.lib.player.media.MediaController
import com.shannon.openvoice.business.player.MediaInfo
import com.shannon.openvoice.business.timeline.TimelineViewModel
import com.shannon.openvoice.business.player.PlayInfo

/**
 *
 * @Package:        com.shannon.openvoice.business.player.listener
 * @ClassName:      PlayerController
 * @Description:     作用描述
 * @Author:         czhen
 * @CreateDate:     2023/3/13 15:50
 */
interface PlayerController {
    /**
     * 绑定播单的事件监听
     * @param kind Kind
     * @param listener OnPlaylistListener
     */
    fun bindPlaylistCallback(
        pageId: String,
        listener: OnPlaylistListener
    )

    /**
     * 解除绑定播单的事件监听
     * @param kind Kind
     */
    fun unbindPlaylistCallback(pageId: String)

    /**
     * 如果播单的界面被刷新了，就将当前在播放的数据通知给界面更新播放状态
     *
     * 如果界面刷新的话，就更新本地的播单的数据
     * 除了正在播放的数据，其它的数据都删除掉，然后将新数据添加到播单中
     * @param kind Kind
     */
    fun refreshed(list: List<MediaInfo>?, pageId: String,listener: OnPlaylistListener)


    /**
     * 设置播单
     * @param list List<StatusModel>
     * @param kind Kind
     * @param listener OnPlaylistListener
     */
    fun setPlaylist(
        list: List<MediaInfo>,
        pageId: String,
        listener: OnPlaylistListener
    )

    fun addMediaSource(
        sources: List<MediaInfo>,
        pageId: String,
        fromHeader: Boolean
    )

    fun getPlayInfo(): PlayInfo?

    fun onPlay(mediaId: String)

    fun onPlay(mediaId: String, progress: Int)

    fun play()

    fun onPause()

    fun seekToPreviousItem()

    fun seekToNextItem()

    fun seekTo(positionMs: Long)

    fun removeMediaItem(mediaId: String)

    fun switchPlayMode(@MediaController.PlayMode model: Int): Int

    fun setPlaybackSpeed(speed: Float)

    /**
     * 定位到列表中的播放项
     */
    fun navigateTo()

    fun releaseMusic()

    /**
     * 是否是当前的播单，避免在暂停播放再开始播放时重复设置播单
     * id 适用于试听功能
     * @param id String
     */
    fun isCurrentPlaylist(pageId: String): Boolean

    fun decodeParentId(): String

    fun decodeTimelineKindValue(): Int
}