package com.shannon.openvoice.business.player

import android.content.ComponentName
import android.content.Context
import android.text.TextUtils
import com.shannon.android.lib.player.media.*
import com.shannon.android.lib.util.PreferencesUtil
import com.shannon.openvoice.business.player.listener.OnPlaylistListener
import com.shannon.openvoice.business.player.listener.PlayerController
import com.shannon.openvoice.business.player.listener.PlayerListener
import com.shannon.openvoice.business.timeline.TimelineViewModel
import com.shannon.openvoice.model.StatusModel
import com.shannon.openvoice.util.CloudStorageHelper
import timber.log.Timber
import java.lang.StringBuilder

/**
 *
 * @Package:        com.shannon.openvoice.business.player
 * @ClassName:      PlayerHelper
 * @Description:     作用描述
 * @Author:         czhen
 * @CreateDate:     2023/3/13 9:35
 */
class PlayerHelper private constructor() : MediaPlayerListener, PlayerController {

    private lateinit var serviceConnection: MusicServiceConnection

    private var mPageId = DEFAULT_PAGE_ID

    private val playerListenerList = arrayListOf<PlayerListener>()

    private val statusList = arrayListOf<MediaInfo>()

    private var playlistListener: OnPlaylistListener? = null

    private val mediaPlayInfo = MediaPlayInfo()

    fun getPlaylistPageId() = mPageId

    fun connectMusicService(context: Context) {
        serviceConnection =
            MusicServiceConnection(context, ComponentName(context, MusicService::class.java), this)
        serviceConnection.connect()
    }

    override fun onConnected() {
        val playMode = PreferencesUtil.getInt(
            PreferencesUtil.Constant.KEY_PLAY_MODE,
            MediaController.PLAY_MODE_ORDER
        )
        switchPlayMode(playMode)
        val speed = PreferencesUtil.getFloat(PreferencesUtil.Constant.KEY_SPEED, 1.0f)
        if (speed != 1.0f) {
            setPlaybackSpeed(speed)
        }
    }

    override fun bindPlaylistCallback(
        pageId: String,
        listener: OnPlaylistListener
    ) {
        if (mPageId == pageId) {
            if (playlistListener != listener) {
                mediaPlayInfo.currentStatus?.apply {
                    playlistListener?.onPlaylistDestroy(this)
                }
            }
            this.playlistListener = listener
        }
    }

    override fun unbindPlaylistCallback(pageId: String) {
        if (mPageId == pageId) {
            this.playlistListener = null
        }
    }

    /**
     * 如果播单的界面被刷新了，就将当前在播放的数据通知给界面更新播放状态
     * @param kind Kind
     */
    override fun refreshed(
        list: List<MediaInfo>?,
        pageId: String,
        listener: OnPlaylistListener
    ) {
        if (mPageId == pageId) {
            if (playlistListener != listener) {
                mediaPlayInfo.currentStatus?.apply {
                    playlistListener?.onPlaylistDestroy(this)
                }
            }

            playlistListener = listener
            mediaPlayInfo.currentStatus?.apply {
                playlistListener?.onIsPlayingChanged(this, mediaPlayInfo.isPlaying)
            }

            if (list != null)
                updatePlaylist(list.toCollection(arrayListOf()))
        }
    }

    private fun updatePlaylist(list: ArrayList<MediaInfo>) {
        if (statusList.isNotEmpty()) {
            val status = mediaPlayInfo.currentStatus
            if (status != null) {
                statusList.clear()
                statusList.add(status)
                Timber.tag("ModelListFragment").d("updatePlaylist : size = ${list.size}")
                list.remove(status)
                Timber.tag("ModelListFragment").d("updatePlaylist : size = ${list.size}")
                serviceConnection.clearUnplayed()
                addMediaSource(list, mPageId, false)
            }
        }
    }

    override fun setPlaylist(
        list: List<MediaInfo>,
        pageId: String,
        listener: OnPlaylistListener
    ) {
        val isConnected = serviceConnection.isConnected()
        Timber.tag("ModelListFragment").d("setPlaylist: isConnected = $isConnected")
        if (!isConnected || list.isEmpty()) return

        statusList.clear()
        statusList.addAll(list)
        if (mPageId != pageId) {
            mediaPlayInfo.currentStatus?.apply { playlistListener?.onPlaylistDestroy(this) }
            mediaPlayInfo.clear()
        }
        mPageId = pageId
        this.playlistListener = listener

        serviceConnection.setMediaSource(convertMediaItem(statusList))
    }

    override fun addMediaSource(
        sources: List<MediaInfo>,
        pageId: String,
        fromHeader: Boolean
    ) {
        if (sources.isNotEmpty() && mPageId == pageId) {
            statusList.addAll(sources)
            serviceConnection.addMediaSource(convertMediaItem(sources), fromHeader)
//            mediaPlayInfo.currentStatus?.apply {
//                playlistListener?.onIsPlayingChanged(this, mediaPlayInfo.isPlaying)
//            }
        }
    }

    override fun getPlayInfo(): PlayInfo? {
        return if (mediaPlayInfo.currentStatus != null) {
            PlayInfo(mediaPlayInfo.currentStatus!!.id, mediaPlayInfo.mediaProgress)
        } else {
            null
        }
    }

    override fun isCurrentPlaylist(pageId: String): Boolean {
        return mPageId == pageId
    }

    override fun onPlay(mediaId: String) {
        val currentMediaId = serviceConnection.getCurrentMediaItem()
        val targetPosition = serviceConnection.getTargetPosition(mediaId)
        Timber.tag("ModelListFragment").d("onPlay = $mediaId")
        if (mediaId == currentMediaId) {
            Timber.tag("ModelListFragment").d("play()")
            serviceConnection.play()
        } else if (targetPosition == -1 && mediaPlayInfo.currentStatus != null) {
            Timber.tag("ModelListFragment")
                .d("onPlay : currentMediaId = $currentMediaId ; id = ${mediaPlayInfo.currentStatus!!.id} ; progress = ${mediaPlayInfo.mediaProgress}")
//            onPlay(mediaPlayInfo.currentStatus!!.id, mediaPlayInfo.mediaProgress)
            serviceConnection.play()
        } else {
            serviceConnection.play(targetPosition.coerceAtLeast(0))
            Timber.tag("ModelListFragment").d("onPlay = $targetPosition")
        }
    }

    override fun onPlay(mediaId: String, progress: Int) {
        val targetPosition = serviceConnection.getTargetPosition(mediaId)
        serviceConnection.play(targetPosition, progress.toLong())
    }

    override fun play() {
        serviceConnection.play()
    }

    override fun onPause() {
        if (serviceConnection.isMediaPlaying())
            serviceConnection.pause()
    }

    override fun seekToPreviousItem() {
        serviceConnection.seekToPreviousItem()
    }

    override fun seekToNextItem() {
        serviceConnection.seekToNextItem()
    }

    override fun seekTo(positionMs: Long) {
        serviceConnection.seekTo(positionMs)
    }

    override fun removeMediaItem(mediaId: String) {
        serviceConnection.removeMediaItem(mediaId)
    }

    override fun switchPlayMode(model: Int): Int {
        return serviceConnection.switchPlayMode(model)
    }

    override fun setPlaybackSpeed(speed: Float) {
        serviceConnection.setPlaybackSpeed(speed)
    }

    override fun navigateTo() {
        mediaPlayInfo.currentStatus?.apply { playlistListener?.navigateTo(this) }
    }

    override fun releaseMusic() {
        statusList.clear()
        serviceConnection.release()
        mediaPlayInfo.clear()
        mPageId = DEFAULT_PAGE_ID
    }

    fun addPlayerListener(listener: PlayerListener) {
        if (!playerListenerList.contains(listener)) {
            notifyView(listener)
            playerListenerList.add(listener)
        }
    }

    fun removePlayerListener(listener: PlayerListener) {
        if (playerListenerList.contains(listener)) {
            playerListenerList.remove(listener)
        }
    }

    private fun findStatusModel(mediaId: String): MediaInfo? {
        return statusList.find { it.id == mediaId }
    }

    override fun onMediaItemTransition(mediaId: String) {
        onIsPlayingChanged(false)
        Timber.tag("ModelListFragment").d("onMediaItemTransition")

        findStatusModel(mediaId)?.run {
            mediaPlayInfo.currentStatus = this
            mediaPlayInfo.mediaProgress = 0
            playerListenerList.forEach {
                it.onMediaItemTransition(this)
            }
            onIsPlayingChanged(serviceConnection.isMediaPlaying())
        }
    }

    override fun onPlayState(hasPreviousItem: Boolean, hasNextItem: Boolean) {
        playerListenerList.forEach {
            it.onPlayState(hasPreviousItem, hasNextItem)
        }
        mediaPlayInfo.hasPreviousItem = hasPreviousItem
        mediaPlayInfo.hasNextItem = hasNextItem
    }

    override fun onIsPlayingChanged(isPlaying: Boolean) {
        Timber.tag("ModelListFragment").d("onIsPlayingChanged : isPlaying = $isPlaying")

        playerListenerList.forEach {
            it.onIsPlayingChanged(isPlaying)
        }
        mediaPlayInfo.isPlaying = isPlaying
        mediaPlayInfo.currentStatus?.apply {
            Timber.tag("ModelListFragment")
                .d("onIsPlayingChanged : mediaId = $id isPlaying = $isPlaying")

            playlistListener?.onIsPlayingChanged(this, isPlaying)
        }
    }

    override fun onIsLoadingChanged(isLoading: Boolean) {
        playerListenerList.forEach {
            it.onIsLoading(isLoading)
        }
        mediaPlayInfo.isLoading = isLoading
    }

    override fun onMediaDuration(mediaDuration: Long) {
        playerListenerList.forEach {
            it.onMediaDuration(mediaDuration)
        }
        mediaPlayInfo.mediaDuration = mediaDuration
    }

    override fun onMediaProgress(mediaProgress: Int) {
        playerListenerList.forEach {
            it.onMediaProgress(mediaProgress)
        }
        mediaPlayInfo.mediaProgress = mediaProgress
    }

    override fun onPlaylistUnselected() {

    }

    override fun playListEmpty() {
        playerListenerList.forEach {
            it.playListEmpty()
        }
    }

    override fun onCleared() {
        statusList.clear()
        mediaPlayInfo.clear()
        mPageId = DEFAULT_PAGE_ID
        playerListenerList.forEach {
            it.onCleared()
        }
    }

    override fun isPlaylistEmpty(): Boolean {
        return serviceConnection.isPlaylistEmpty()
    }

    override fun decodeParentId(): String {
        val result = getPlaylistPageId().split(SPLITER)
        return result[1]
    }

    override fun decodeTimelineKindValue(): Int {
        val result = getPlaylistPageId().split(SPLITER)
        return result[0].toInt()
    }

    private fun notifyView(playerListener: PlayerListener) {
        mediaPlayInfo.apply {
            if (currentStatus != null) {
                playerListener.onMediaItemTransition(currentStatus!!)
                playerListener.onPlayState(hasPreviousItem, hasNextItem)
                playerListener.onIsPlayingChanged(isPlaying)
                playerListener.onIsLoading(isLoading)
                playerListener.onMediaDuration(mediaDuration)
                playerListener.onMediaProgress(mediaProgress)
            }
        }
    }


    private fun convertMediaItem(data: List<MediaInfo>): List<MediaData> {
        val mediaSources = arrayListOf<MediaData>()
        data.forEach {
            mediaSources.add(
                MediaData(it.id, it.resUrl)
            )
        }
        return mediaSources
    }

    companion object {

        const val SPLITER = "-_-"
        private val cloudStorageHelper by lazy { CloudStorageHelper() }

        val instance by lazy(LazyThreadSafetyMode.SYNCHRONIZED) {
            PlayerHelper()
        }

        fun convert2MediaInfo(list: List<StatusModel>): List<MediaInfo> {
            val mediaSources = arrayListOf<MediaInfo>()
            list.forEach {
                val statusOssId = it.actionableStatus.ossId
                if (it.actionableStatus.ttsGenerated && !TextUtils.isEmpty(statusOssId)) {
                    mediaSources.add(
                        MediaInfo(
                            it.id,
                            cloudStorageHelper.getResourceUrl(statusOssId!!),
                            it.account.avatar,
                            it
                        )
                    )
                }
            }
            return mediaSources
        }

        private fun encodeMediaId(id: String, prefix: String = ""): String {
            return if (prefix.isNotEmpty()) prefix.plus(SPLITER).plus(id) else id
        }

        val DEFAULT_PAGE_ID = generatePageId(TimelineViewModel.Kind.DEFAULT)

        fun generatePageId(
            kind: TimelineViewModel.Kind,
            id: String = "",
            searchCondition: String = ""
        ): String {
            Timber.tag("ModelListFragment").d("kind.ordinal = ${kind.ordinal}")

            val sb = StringBuffer(kind.ordinal.toString())
            sb.append(SPLITER)
            if (id.isNotEmpty()) {
                sb.append(id)
            }
            sb.append(SPLITER)
            if (searchCondition.isNotEmpty()) {
                sb.append(searchCondition)
            }
            return sb.toString().also {
                Timber.tag("ModelListFragment").d("generatePageId = $it")

            }
        }
    }


}