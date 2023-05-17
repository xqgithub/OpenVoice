package com.shannon.android.lib.player.media

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.IBinder
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.MediaControllerCompat
import android.support.v4.media.session.PlaybackStateCompat
import android.util.Log
import android.widget.Toast
import androidx.lifecycle.MutableLiveData
import com.shannon.android.lib.player.PlayerViewListener

/**
 *
 * @ClassName:      MusicServiceConnection
 * @Description:     java类作用描述
 * @Author:         czhen
 */
class MusicServiceConnection(
    private val context: Context,
    private val componentName: ComponentName,
    private val mediaPlayerListener: MediaPlayerListener? = null
) : MediaController {


    @Volatile
    private var mState = CONNECT_STATE_DISCONNECTED

    private lateinit var mediaController: MediaController

    private var mServiceConnection: InnerServiceConnection? = null

    fun connect() {
        if (mState != CONNECT_STATE_DISCONNECTING && mState != CONNECT_STATE_DISCONNECTED) {
            Log.e(TAG, "connect() called while neither disconnecting nor disconnected ")
            return
        }
        setState(CONNECT_STATE_CONNECTING)

        mServiceConnection = InnerServiceConnection()
        val intent = Intent().apply { component = componentName }

        var bound = false
        try {
            bound = context.bindService(intent, mServiceConnection!!, Context.BIND_AUTO_CREATE)
        } catch (ex: Exception) {
            Log.e(TAG, "Failed binding to service $mServiceConnection")
        }
        if (!bound) {
            forceCloseConnection()
        }
    }

    fun disconnect() {
        setState(CONNECT_STATE_DISCONNECTING)
        forceCloseConnection()
    }

    private fun setState(state: Int) {
        mState = state
    }

    private inner class InnerServiceConnection : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, binder: IBinder?) {
            val serviceBinder = binder as MusicService.ServiceBinder
            mediaController = serviceBinder.getMediaController()
            if (mediaPlayerListener != null)
                mediaController.setMediaPlayerListener(mediaPlayerListener)
            setState(CONNECT_STATE_CONNECTED)
            mediaPlayerListener?.onConnected()
            Log.d(TAG, "onServiceConnected: CONNECT_STATE_CONNECTED")
        }

        override fun onServiceDisconnected(name: ComponentName?) {
            setState(CONNECT_STATE_DISCONNECTED)
        }
    }

    private fun forceCloseConnection() {
        if (mServiceConnection != null) {
            try {
                context.unbindService(mServiceConnection!!)
            } catch (e: IllegalArgumentException) {
                Log.d(TAG, "unbindService failed", e)
            }
        }
        setState(CONNECT_STATE_DISCONNECTED)
        mServiceConnection = null
    }

     fun isConnected() = mState == CONNECT_STATE_CONNECTED

    fun bindListener() {
//        if (isConnected()) {
//            mediaController.setMediaPlayerListener(this@MusicServiceConnection)
//        }
    }


    fun recover(targetPosition: Int, positionMs: Long) {
        if (isConnected()) {
            if (targetPosition == -1) return
            mediaController.seekTo(targetPosition, positionMs)
        }
    }

    fun isCurrentController(): Boolean? {
//        return if (isConnected()) {
//            mediaController.isCurrentController(this@MusicServiceConnection)
//        } else {
//            null
//        }
        return null
    }

    override fun isCurrentController(mediaPlayerListener: MediaPlayerListener): Boolean {
        throw IllegalStateException("do not call")
    }

    override fun play() {
        if (isConnected()) mediaController.play()
    }

    override fun play(targetPosition: Int) {
        if (isConnected()) mediaController.play(targetPosition)
    }

    override fun play(targetPosition: Int, positionMs: Long) {
        if (isConnected()) mediaController.play(targetPosition, positionMs)
    }

    override fun seekTo(positionMs: Long) {
        if (isConnected()) mediaController.seekTo(positionMs)
    }

    override fun seekTo(targetPosition: Int, positionMs: Long) {
        if (isConnected()) mediaController.seekTo(targetPosition, positionMs)
    }

    override fun pause() {
        if (isConnected()) mediaController.pause()
    }

    override fun stop() {
        if (isConnected()) mediaController.stop()
    }

    override fun release() {
        if (isConnected()) mediaController.release()
    }

    override fun seekToPreviousItem() {
        if (isConnected()) mediaController.seekToPreviousItem()
    }

    override fun seekToNextItem() {
        if (isConnected()) mediaController.seekToNextItem()
    }

    override fun setMediaSource(sources: List<MediaData>) {
        if (isConnected()) mediaController.setMediaSource(sources)
    }

    override fun addMediaSource(sources: List<MediaData>, fromHeader: Boolean) {
        if (isConnected()) mediaController.addMediaSource(sources, fromHeader)
    }

    override fun removeMediaItem(mediaId: String) {
        if (isConnected()) mediaController.removeMediaItem(mediaId)
    }

    override fun setMediaPlayerListener(mediaPlayerListener: MediaPlayerListener) {
    }

    override fun getCurrentMediaItem(): String {
        return if (isConnected()) mediaController.getCurrentMediaItem() else ""
    }

    override fun switchPlayMode(@MediaController.PlayMode model: Int): Int {
        return if (isConnected()) mediaController.switchPlayMode(model) else MediaController.PLAY_MODE_ORDER
    }

    override fun setPlaybackSpeed(speed: Float) {
        if (isConnected()) mediaController.setPlaybackSpeed(speed)
    }

    override fun getTargetPosition(mediaId: String): Int {
        return if (isConnected()) mediaController.getTargetPosition(mediaId) else -1
    }

    override fun isMediaPlaying(): Boolean {
        return if (isConnected()) mediaController.isMediaPlaying() else false
    }

    override fun clearUnplayed() {
        if (isConnected()) mediaController.clearUnplayed()
    }

    override fun isPlaylistEmpty(): Boolean {
        return if (isConnected()) mediaController.isPlaylistEmpty() else true
    }

    companion object {
        private const val TAG = "MusicServiceConnection"
        const val CONNECT_STATE_DISCONNECTING = 0
        const val CONNECT_STATE_DISCONNECTED = 1
        const val CONNECT_STATE_CONNECTING = 2
        const val CONNECT_STATE_CONNECTED = 3
    }

//    override fun onMediaItemTransition(mediaId: String) {
//        Log.e("PlayerImpl", "onMediaItemTransition: $mediaId ")
//        nowPlaying.value = mediaId
//    }
//
//    override fun onPlayState(hasPreviousItem: Boolean, hasNextItem: Boolean) {
//        val isSkipToPreviousEnabledValue = isSkipToPreviousEnabled.value ?: false
//        if (isSkipToPreviousEnabledValue != hasPreviousItem) {
//            isSkipToPreviousEnabled.value = hasPreviousItem
//        }
//        val isSkipToNextEnabledValue = isSkipToNextEnabled.value ?: false
//        if (isSkipToNextEnabledValue != hasNextItem) {
//            isSkipToNextEnabled.value = hasNextItem
//        }
//    }
//
//    override fun onIsPlayingChanged(isPlaying: Boolean) {
//        this.isPlaying.value = isPlaying
//    }
//
//    override fun onIsLoadingChanged(isLoading: Boolean) {
//        val onIsLoadingValue = onIsLoading.value ?: false
//        if (onIsLoadingValue != isLoading) {
//            onIsLoading.postValue(isLoading)
//        }
//    }
//
//    override fun onMediaDuration(mediaDuration: Long) {
//        this.mediaDuration.postValue(mediaDuration.toInt())
//    }
//
//    override fun onMediaProgress(mediaProgress: Int) {
//      this.  mediaProgress.postValue(mediaProgress)
//    }
//
//    override fun onPlaylistUnselected() {
//        playlistUnselected.postValue(true)
//    }
//
//    override fun playListEmpty() {
//        playListEmpty.value = true
//    }

}