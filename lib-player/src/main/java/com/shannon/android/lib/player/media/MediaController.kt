package com.shannon.android.lib.player.media

import androidx.annotation.FloatRange
import androidx.annotation.IntDef

/**
 *
 * @ClassName:      MediaController
 * @Description:     java类作用描述
 * @Author:         czhen
 */
interface MediaController {

    companion object {
        const val PLAY_MODE_SHUFFLE = 1
        const val PLAY_MODE_ORDER = 2
    }

    @kotlin.annotation.Retention(AnnotationRetention.SOURCE)
    @kotlin.annotation.Target(
        AnnotationTarget.FIELD,
        AnnotationTarget.FUNCTION,
        AnnotationTarget.TYPE_PARAMETER,
        AnnotationTarget.VALUE_PARAMETER,
        AnnotationTarget.LOCAL_VARIABLE,
        AnnotationTarget.TYPE
    )
    @IntDef(value = [PLAY_MODE_SHUFFLE, PLAY_MODE_ORDER])
    annotation class PlayMode {}

    fun play()

    fun play(targetPosition: Int)

    fun play(targetPosition: Int, positionMs: Long)

    fun seekTo(positionMs: Long)

    fun seekTo(targetPosition: Int, positionMs: Long)

    fun pause()

    fun stop()

    fun release()

    fun seekToPreviousItem()

    fun seekToNextItem()

    fun setMediaSource(sources: List<MediaData>)

    fun addMediaSource(sources: List<MediaData>, fromHeader: Boolean)

    fun removeMediaItem(mediaId: String)

    fun setMediaPlayerListener(mediaPlayerListener: MediaPlayerListener)

    fun isCurrentController(mediaPlayerListener: MediaPlayerListener): Boolean

    fun getCurrentMediaItem(): String

    fun switchPlayMode(@MediaController.PlayMode model: Int): @PlayMode Int

    fun setPlaybackSpeed(
        @FloatRange(
            from = 0.0,
            to = 2.0,
            fromInclusive = false,
            toInclusive = false
        ) speed: Float
    )

    fun getTargetPosition(mediaId: String): Int

    fun isMediaPlaying(): Boolean

    fun clearUnplayed()

    fun isPlaylistEmpty(): Boolean

}