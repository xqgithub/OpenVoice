package com.shannon.openvoice.model

import com.google.gson.annotations.SerializedName
import com.shannon.android.lib.player.media.MediaData
import com.shannon.openvoice.business.player.MediaInfo

/**
 *
 * @Package:        com.shannon.openvoice.model
 * @ClassName:      TradeList
 * @Description:     作用描述
 * @Author:         czhen
 * @CreateDate:     2022/9/1 10:52
 */
data class TradeList(
    val id: Long,
    @SerializedName("voice_model") val voiceModel: StatusModel.VoiceModel,
    val count: Long,
    val isPlaying: Boolean,
    val playlist: List<MediaInfo>?,
    val playingMediaId: String?,
    val playingProgress: Int?,
    var backgroundColor:Int = 0
) {

    @JvmName("getPlaylistJava")
    fun getPlaylist() = playlist ?: arrayListOf()

    @JvmName("getPlayingMediaIdJava")
    fun getPlayingMediaId() = playingMediaId ?: ""

    @JvmName("getPlayingProgressJava")
    fun getPlayingProgress() = playingProgress ?: 0
}
