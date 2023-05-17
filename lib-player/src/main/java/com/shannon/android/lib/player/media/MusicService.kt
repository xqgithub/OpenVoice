package com.shannon.android.lib.player.media

import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.os.Binder
import android.os.Handler
import android.os.IBinder
import android.support.v4.media.session.MediaSessionCompat
import android.support.v4.media.session.PlaybackStateCompat
import android.text.TextUtils
import android.util.Log
import android.view.KeyEvent
import com.google.android.exoplayer2.*
import com.google.android.exoplayer2.audio.AudioAttributes
import com.google.android.exoplayer2.ext.mediasession.MediaSessionConnector
import com.google.android.exoplayer2.source.MediaSource
import com.google.android.exoplayer2.source.ProgressiveMediaSource
import com.google.android.exoplayer2.source.ShuffleOrder
import com.google.android.exoplayer2.upstream.DataSource
import com.google.android.exoplayer2.upstream.DefaultDataSource
import com.google.android.exoplayer2.util.Util

/**
 *
 * @ClassName:      MusicService
 * @Description:     java类作用描述
 * @Author:         czhen
 */
class MusicService : Service(), MediaController, MediaSessionConnector.MediaButtonEventHandler {

    private val musicAudioAttributes = AudioAttributes.Builder()
        .setContentType(C.CONTENT_TYPE_MUSIC)
        .setUsage(C.USAGE_MEDIA)
        .build()

    private val musicPlayListener = MusicPlayListener()

    private val exoPlayer: ExoPlayer by lazy {
        ExoPlayer.Builder(this).build().apply {
            setAudioAttributes(musicAudioAttributes, true)
            setWakeMode(C.WAKE_MODE_NETWORK)
            setHandleAudioBecomingNoisy(true)
            addListener(musicPlayListener)
            shuffleModeEnabled = false
            repeatMode = Player.REPEAT_MODE_ALL
            setShuffleOrder(ShuffleOrder.DefaultShuffleOrder(0))
        }
    }
    private val dataSourceFactory: DataSource.Factory by lazy { DefaultDataSource.Factory(this) }

    private lateinit var mediaSession: MediaSessionCompat
    private lateinit var mediaSessionConnector: MediaSessionConnector

    private var mediaPlayerListener: MediaPlayerListener? = null
    private lateinit var serviceHandler: Handler
    override fun onCreate() {
        super.onCreate()

        serviceHandler = Handler(mainLooper)
        mediaSession = MediaSessionCompat(this, TAG)
        mediaSession.setSessionActivity(getSessionActivityPendingIntent())
        mediaSession.isActive = true

        mediaSessionConnector = MediaSessionConnector(mediaSession)
        mediaSessionConnector.setMediaButtonEventHandler(this)
        mediaSessionConnector.setPlayer(exoPlayer)

    }

    override fun play() {
        Log.e(TAG, "play: ${exoPlayer.currentTimeline.isEmpty}")
        if (exoPlayer.currentTimeline.isEmpty) {
            mediaPlayerListener?.playListEmpty()
            return
        }
//        exoPlayer.pauseAtEndOfMediaItems = false
        when (getMediaSessionPlaybackState()) {
            PlaybackStateCompat.STATE_STOPPED,
            PlaybackStateCompat.STATE_NONE -> {
                val firstWindowIndexPlayer =
                    exoPlayer.currentTimeline.getFirstWindowIndex(exoPlayer.shuffleModeEnabled)
                Log.e(TAG, "play: firstWindowIndexPlayer = $firstWindowIndexPlayer")
                play(firstWindowIndexPlayer)
            }
            PlaybackStateCompat.STATE_PAUSED -> {
                exoPlayer.play()
            }
        }
    }

    override fun play(targetPosition: Int) {
        play(targetPosition, 0)
    }

    override fun play(targetPosition: Int, positionMs: Long) {
        if (exoPlayer.currentTimeline.isEmpty) {
            mediaPlayerListener?.playListEmpty()
            return
        }
        if (targetPosition < 0 || targetPosition >= exoPlayer.currentTimeline.windowCount) return
        Log.e(TAG, "play duration : ${exoPlayer.duration}")
        exoPlayer.seekTo(targetPosition, Math.min(positionMs, exoPlayer.duration))
        exoPlayer.play()
    }

    override fun seekTo(positionMs: Long) {
        exoPlayer.seekTo(positionMs)
    }

    override fun seekTo(targetPosition: Int, positionMs: Long) {
        exoPlayer.seekTo(targetPosition, positionMs.coerceAtLeast(0))
    }

    override fun pause() {
        exoPlayer.pause()
    }

    override fun stop() {
        exoPlayer.stop()
        exoPlayer.clearMediaItems()
    }

    override fun release() {
        exoPlayer.playWhenReady = false
        exoPlayer.stop()
        exoPlayer.clearMediaItems()
        mediaDataList.clear()
    }

    override fun seekToPreviousItem() {
        val previousWindowIndex = exoPlayer.currentTimeline.getPreviousWindowIndex(
            exoPlayer.currentMediaItemIndex, exoPlayer.repeatMode, exoPlayer.shuffleModeEnabled
        )
        Log.e(TAG, "seekToPreviousItem: previousWindowIndex = $previousWindowIndex")
        exoPlayer.seekToPreviousMediaItem()

        val canToPlay = !exoPlayer.playWhenReady && exoPlayer.hasPreviousMediaItem()
        if (canToPlay) exoPlayer.play()
    }

    override fun seekToNextItem() {
        val nextWindowIndex = exoPlayer.currentTimeline.getNextWindowIndex(
            exoPlayer.currentMediaItemIndex, exoPlayer.repeatMode, exoPlayer.shuffleModeEnabled
        )
        Log.e(TAG, "seekToPreviousItem: nextWindowIndex = $nextWindowIndex")
        if (nextWindowIndex == -1) return
        exoPlayer.seekToNextMediaItem()
        val canToPlay = !exoPlayer.playWhenReady
        if (canToPlay) exoPlayer.play()
    }

    private val mediaDataList = arrayListOf<MediaData>()
    override fun setMediaSource(sources: List<MediaData>) {
        Log.e(TAG, "setMediaSource: sources = ${sources.size}")
        release()
        mediaDataList.addAll(sources)
        val dataList = arrayListOf<MediaSource>()
        sources.forEach {
            val mediaItem = MediaItem.Builder()
                .setMediaId(it.mediaId)
                .setUri(it.mediaUri)
                .build()
            val mediaSource = ProgressiveMediaSource.Factory(dataSourceFactory)
                .createMediaSource(mediaItem)
            dataList.add(mediaSource)
        }
        exoPlayer.setMediaSources(dataList)
        exoPlayer.prepare()
        if (mediaDataList.isEmpty()) {
            mediaPlayerListener?.onPlayState(hasPreviousItem = false, hasNextItem = false)
        }
    }

    private val playListPool = HashMap<String, PlayListData>()
    fun setMediaSource(playListKindName: String, sources: List<MediaData>) {
        exoPlayer.stop()
        val dataList = arrayListOf<MediaSource>()
        sources.forEach {
            val mediaItem = MediaItem.Builder()
                .setMediaId(it.mediaId)
                .setUri(it.mediaUri)
                .build()
            val mediaSource = ProgressiveMediaSource.Factory(dataSourceFactory)
                .createMediaSource(mediaItem)
            dataList.add(mediaSource)
        }
        exoPlayer.setMediaSources(dataList)
        exoPlayer.prepare()
    }

    override fun addMediaSource(sources: List<MediaData>, fromHeader: Boolean) {
        val dataList = arrayListOf<MediaSource>()
        sources.forEach {
            val mediaItem = MediaItem.Builder()
                .setMediaId(it.mediaId)
                .setUri(it.mediaUri)
                .build()
            val mediaSource = ProgressiveMediaSource.Factory(dataSourceFactory)
                .createMediaSource(mediaItem)
            dataList.add(mediaSource)
        }
        if (fromHeader) {
            mediaDataList.addAll(0, sources)
            exoPlayer.addMediaSources(0, dataList)
        } else {
            mediaDataList.addAll(sources)
            exoPlayer.addMediaSources(dataList)
        }

    }

    override fun removeMediaItem(mediaId: String) {
        val index = mediaDataList.indexOfFirst { it.mediaId == mediaId }
        if (index != -1) {
            mediaDataList.removeAt(index)
            exoPlayer.removeMediaItem(index)
            if (exoPlayer.currentTimeline.isEmpty) {
                mediaPlayerListener?.onCleared()
            }
        }
    }

    override fun clearUnplayed() {
        val currentMediaItemIndex = exoPlayer.currentMediaItemIndex
        val data = mediaDataList[currentMediaItemIndex]
        exoPlayer.removeMediaItems(0, currentMediaItemIndex)
        exoPlayer.removeMediaItems(1, exoPlayer.mediaItemCount)
        mediaDataList.clear()
        mediaDataList.add(data)
        Log.e(TAG, "clearUnplayed : ${exoPlayer.mediaItemCount}")
    }

    override fun isPlaylistEmpty(): Boolean {
        return exoPlayer.currentTimeline.isEmpty
    }

    private var oldMediaId = ""
    override fun setMediaPlayerListener(mediaPlayerListener: MediaPlayerListener) {
        if (this.mediaPlayerListener != mediaPlayerListener) {
            oldMediaId = getCurrentMediaItem()
            this.mediaPlayerListener?.onPlaylistUnselected()
        }
        Log.e(TAG, "setMediaPlayerListener: play")
        this.mediaPlayerListener = mediaPlayerListener
    }

    override fun isCurrentController(mediaPlayerListener: MediaPlayerListener): Boolean {
        return mediaPlayerListener == this.mediaPlayerListener
    }

    override fun getCurrentMediaItem(): String {
        return exoPlayer.currentMediaItem?.mediaId ?: oldMediaId
    }

    override fun switchPlayMode(@MediaController.PlayMode model: Int): @MediaController.PlayMode Int {
        exoPlayer.shuffleModeEnabled = model == MediaController.PLAY_MODE_SHUFFLE
        mediaPlayerListener?.onPlayState(
            exoPlayer.hasPreviousMediaItem(),
            exoPlayer.hasNextMediaItem()
        )
        Log.e(
            TAG,
            "switchPlayMode: shuffleModeEnabled = ${model == MediaController.PLAY_MODE_SHUFFLE}"
        )
        return model
    }

    override fun setPlaybackSpeed(speed: Float) {
        exoPlayer.setPlaybackSpeed(speed)
    }

    override fun getTargetPosition(mediaId: String): Int {
        return mediaDataList.indexOfFirst { TextUtils.equals(it.mediaId, mediaId) }
    }

    override fun isMediaPlaying(): Boolean {
        return (getMediaSessionPlaybackState() == PlaybackStateCompat.STATE_PLAYING)
    }

    private fun getSessionActivityPendingIntent(): PendingIntent? {
        val intent = packageManager?.getLaunchIntentForPackage(packageName)?.let {
            PendingIntent.getActivity(this, 0, it, PendingIntent.FLAG_IMMUTABLE)
        }
        return intent
    }

    override fun onBind(intent: Intent?): IBinder {
        return ServiceBinder()
    }

    inner class ServiceBinder : Binder() {
        fun getMediaController(): MediaController {
            return this@MusicService
        }
    }

    private inner class MusicPlayListener : Player.Listener {

        override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
            val mediaId = mediaItem?.mediaId ?: ""

            if (exoPlayer.currentTimeline.isEmpty) return

            val previousWindowIndex = exoPlayer.currentTimeline.getPreviousWindowIndex(
                exoPlayer.currentMediaItemIndex, exoPlayer.repeatMode, exoPlayer.shuffleModeEnabled
            )
            val nextWindowIndex = exoPlayer.currentTimeline.getNextWindowIndex(
                exoPlayer.currentMediaItemIndex, exoPlayer.repeatMode, exoPlayer.shuffleModeEnabled
            )
            val firstWindowIndexPlayer =
                exoPlayer.currentTimeline.getFirstWindowIndex(exoPlayer.shuffleModeEnabled)
            val lastWindowIndexPlayer =
                exoPlayer.currentTimeline.getLastWindowIndex(exoPlayer.shuffleModeEnabled)
            Log.e(
                TAG,
                "firstWindowIndexPlayer = $firstWindowIndexPlayer ; lastWindowIndexPlayer = $lastWindowIndexPlayer ;${exoPlayer.currentTimeline.javaClass.name}"
            )
            Log.e(
                TAG,
                "onMediaItemTransition: mediaItem.uri = ${mediaItem?.localConfiguration?.uri} ; mediaId = $mediaId ; previousWindowIndex = $previousWindowIndex ; nextWindowIndex = $nextWindowIndex"
            )

//            if (getMediaSessionPlaybackState() == PlaybackStateCompat.STATE_PLAYING)
            mediaPlayerListener?.onMediaItemTransition(mediaId)
            mediaPlayerListener?.onPlayState(
                exoPlayer.hasPreviousMediaItem(),
                exoPlayer.hasNextMediaItem()
            )
        }

        override fun onPlayWhenReadyChanged(playWhenReady: Boolean, reason: Int) {
            Log.d(TAG, "onPlayWhenReadyChanged: playWhenReady = $playWhenReady ; reason = $reason")

        }

        override fun onPlaybackStateChanged(playbackState: Int) {
            Log.d(TAG, "onPlaybackStateChanged: playbackState = $playbackState ")
            if (playbackState == Player.STATE_BUFFERING || playbackState == Player.STATE_READY) {
                mediaPlayerListener?.onPlayState(
                    exoPlayer.hasPreviousMediaItem(),
                    exoPlayer.hasNextMediaItem()
                )
            }
        }

        override fun onPlayerError(error: PlaybackException) {
            Log.e(TAG, "onPlayerError: ", error)
            pause()
            seekToNextItem()

        }

        override fun onEvents(player: Player, events: Player.Events) {
            if (events.containsAny(
                    Player.EVENT_POSITION_DISCONTINUITY,
                    Player.EVENT_TIMELINE_CHANGED
                )
            ) {
                updateProgress()
                Log.d(TAG, "onPositionDiscontinuity: reason = ${player.currentPosition} ")
            }
        }

        override fun onMediaMetadataChanged(mediaMetadata: MediaMetadata) {
            Log.d(TAG, "onMediaMetadataChanged: mediaMetadata = $mediaMetadata ")
        }

        override fun onIsPlayingChanged(isPlaying: Boolean) {
            Log.d(TAG, "onIsPlayingChanged: isPlaying = $isPlaying ")
            mediaPlayerListener?.onIsPlayingChanged(isPlaying)
        }

        override fun onIsLoadingChanged(isLoading: Boolean) {
            val playbackState = getMediaSessionPlaybackState()
            if ((isLoading && playbackState == PlaybackStateCompat.STATE_PLAYING) || playbackState == PlaybackStateCompat.STATE_PAUSED) return

            mediaPlayerListener?.onIsLoadingChanged(isLoading)
            Log.d(
                TAG,
                "onIsLoadingChanged: isLoading = $isLoading ; playbackState = ${getMediaSessionPlaybackStateText()}"
            )
        }


    }

    companion object {
        private const val TAG = "MusicService"
        private const val MAX_UPDATE_INTERVAL_MS = 1000L
        private const val MIN_UPDATE_INTERVAL_MS = 200L

    }

    override fun onMediaButtonEvent(player: Player, mediaButtonEvent: Intent): Boolean {
        val keyEvent = mediaButtonEvent.getParcelableExtra<KeyEvent>(Intent.EXTRA_KEY_EVENT)
        if (keyEvent?.action == KeyEvent.ACTION_UP) {
            Log.d(TAG, "keyCode = ${keyEvent.keyCode}")
            when (keyEvent.keyCode) {
                KeyEvent.KEYCODE_MEDIA_PREVIOUS -> {
                    seekToPreviousItem()
                    Log.d(TAG, "KEYCODE_MEDIA_PREVIOUS")
                }
                KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE -> {
                    if (exoPlayer.playWhenReady) {
                        pause()
                    } else {
                        play()
                    }
                    Log.d(TAG, "KEYCODE_MEDIA_PLAY_PAUSE")
                }
                KeyEvent.KEYCODE_MEDIA_NEXT -> {
                    seekToNextItem()
                    Log.d(TAG, "KEYCODE_MEDIA_NEXT")
                }
            }
        }
        return true
    }

    private fun getMediaSessionPlaybackState(): Int {
        val playWhenReady = exoPlayer.playWhenReady
        return when (exoPlayer.playbackState) {
            Player.STATE_BUFFERING -> if (playWhenReady) PlaybackStateCompat.STATE_BUFFERING else PlaybackStateCompat.STATE_PAUSED
            Player.STATE_READY -> if (playWhenReady) PlaybackStateCompat.STATE_PLAYING else PlaybackStateCompat.STATE_PAUSED
            Player.STATE_ENDED -> PlaybackStateCompat.STATE_STOPPED
            Player.STATE_IDLE -> PlaybackStateCompat.STATE_NONE
            else -> PlaybackStateCompat.STATE_NONE
        }
    }

    private fun getMediaSessionPlaybackStateText(): String {
        return when (getMediaSessionPlaybackState()) {
            PlaybackStateCompat.STATE_BUFFERING -> "STATE_BUFFERING"
            PlaybackStateCompat.STATE_PAUSED -> "STATE_PAUSED"
            PlaybackStateCompat.STATE_PLAYING -> "STATE_PLAYING"
            PlaybackStateCompat.STATE_STOPPED -> "STATE_STOPPED"
            PlaybackStateCompat.STATE_NONE -> "STATE_NONE"
            else -> "NULL"
        }
    }

    private fun updateProgress() {

        val positionMs = exoPlayer.contentPosition
        mediaPlayerListener?.onMediaDuration(exoPlayer.duration)
        mediaPlayerListener?.onMediaProgress(positionMs.toInt())

        serviceHandler.removeCallbacks(updateProgressAction)
        val playbackState = exoPlayer.playbackState
        if (exoPlayer.isPlaying) {
            val mediaTimeUntilNextFullSecondMs = 1000 - positionMs % 1000

            val playbackSpeed = exoPlayer.playbackParameters.speed
            var delayMs =
                if (playbackSpeed > 0) (mediaTimeUntilNextFullSecondMs / playbackSpeed).toLong() else 1000L
            delayMs = Util.constrainValue(delayMs, MIN_UPDATE_INTERVAL_MS, MAX_UPDATE_INTERVAL_MS)

            serviceHandler.postDelayed(updateProgressAction, delayMs)
        } else if (playbackState != Player.STATE_ENDED && playbackState != Player.STATE_IDLE) {
            serviceHandler.postDelayed(updateProgressAction, MAX_UPDATE_INTERVAL_MS)

        }
    }

    private val updateProgressAction: Runnable = Runnable {
        updateProgress()
    }

}

data class PlayListData(
    val kindName: String,
    val mediaIndex: Int,
    val mediaPositionMs: Long,
    val mediaPlayerListener: MediaPlayerListener
)