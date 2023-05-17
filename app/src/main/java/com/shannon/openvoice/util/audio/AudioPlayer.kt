package com.shannon.openvoice.util.audio

import android.media.*
import android.os.Build
import android.util.Log
import java.io.FileInputStream
import java.io.InputStream
import kotlin.concurrent.thread

/**
 * <描述当前功能>
 * @author: czhen
 * @date: 2022/11/30
 */
class AudioPlayer {

    private val audioTrack: AudioTrack by lazy {
        val trackBufferSize = AudioRecord.getMinBufferSize(
            GlobalConfig.SAMPLE_RATE_INHZ,
            GlobalConfig.CHANNEL_CONFIG,
            GlobalConfig.AUDIO_FORMAT
        )
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            AudioTrack.Builder()
                .setAudioAttributes(
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_MEDIA)
                        .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                        .setLegacyStreamType(AudioManager.STREAM_MUSIC)
                        .build()
                )
                .setAudioFormat(
                    AudioFormat.Builder().setSampleRate(GlobalConfig.SAMPLE_RATE_INHZ)
                        .setChannelMask(GlobalConfig.CHANNEL_CONFIG_PLAYER)
                        .setEncoding(GlobalConfig.AUDIO_FORMAT).build()
                )
                .setTransferMode(AudioTrack.MODE_STREAM)
                .setBufferSizeInBytes(trackBufferSize)
                .build()
        } else {
            AudioTrack(
                AudioManager.STREAM_MUSIC,
                GlobalConfig.SAMPLE_RATE_INHZ,
                GlobalConfig.CHANNEL_CONFIG_PLAYER,
                GlobalConfig.AUDIO_FORMAT,
                trackBufferSize,
                AudioTrack.MODE_STREAM
            )
        }
    }


    fun play(path: String, onFinished: () -> Unit) {
        if (audioTrack.state != AudioTrack.STATE_UNINITIALIZED && audioTrack.playState != AudioTrack.PLAYSTATE_PLAYING) {
            val trackBufferSize = AudioRecord.getMinBufferSize(
                GlobalConfig.SAMPLE_RATE_INHZ,
                GlobalConfig.CHANNEL_CONFIG,
                GlobalConfig.AUDIO_FORMAT
            )
            thread {
                try {
                    val dis: InputStream = FileInputStream(path)
                    audioTrack.play()
                    var length: Int
                    val a = ByteArray(trackBufferSize)
                    while (dis.read(a).also { length = it } != -1) {
                        audioTrack.write(a, 0, length)
                    }
                    onFinished()
                } catch (e: Exception) {
                    e.printStackTrace()
                } finally {
                    audioTrack.stop()
                }
            }
        }
    }

    fun stop() {
        if (audioTrack.state != AudioTrack.STATE_UNINITIALIZED && audioTrack.playState == AudioTrack.PLAYSTATE_PLAYING) {
            audioTrack.stop()
        }
    }

    fun release() {
        if (audioTrack.state != AudioTrack.STATE_UNINITIALIZED) {
            audioTrack.release()
        }
    }
}