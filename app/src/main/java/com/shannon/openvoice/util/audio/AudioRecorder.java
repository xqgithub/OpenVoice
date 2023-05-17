package com.shannon.openvoice.util.audio;


import static com.shannon.openvoice.util.audio.GlobalConfig.AUDIO_FORMAT;
import static com.shannon.openvoice.util.audio.GlobalConfig.CHANNEL_CONFIG;
import static com.shannon.openvoice.util.audio.GlobalConfig.SAMPLE_RATE_INHZ;

import android.annotation.SuppressLint;
import android.media.AudioAttributes;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioRecord;
import android.media.AudioTrack;
import android.media.MediaRecorder;
import android.os.Build;
import android.util.Log;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;


public class AudioRecorder {

    private static final String TAG = "AudioRecorder";


    private boolean isRecording;
    private AudioRecord audioRecord;


    @SuppressLint("MissingPermission")
    public boolean startRecord(File saveFile) {
        if (isRecording) return false;
        final int minBufferSize = AudioRecord.getMinBufferSize(SAMPLE_RATE_INHZ, CHANNEL_CONFIG, AUDIO_FORMAT);
        Log.e(TAG, "minBufferSize : " + minBufferSize);

        audioRecord = new AudioRecord(MediaRecorder.AudioSource.MIC, SAMPLE_RATE_INHZ,
                CHANNEL_CONFIG, AUDIO_FORMAT, minBufferSize);

        final byte data[] = new byte[minBufferSize];
        Log.e(TAG, "Directory  created : " + saveFile.getAbsolutePath());
        if (saveFile.exists()) {
            saveFile.delete();
        }

        audioRecord.startRecording();
        isRecording = true;


        new Thread(() -> {

            FileOutputStream os = null;
            try {
                os = new FileOutputStream(saveFile);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }

            if (null != os) {
                while (isRecording) {
                    int read = audioRecord.read(data, 0, minBufferSize);
                    // 如果读取音频数据没有出现错误，就将数据写入到文件
                    if (AudioRecord.ERROR_INVALID_OPERATION != read) {
                        try {
                            os.write(data);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
                try {
                    Log.i(TAG, "run: close file output stream !");
                    os.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }).start();
        return true;
    }

    public boolean isRecording() {
        return isRecording;
    }

    public void stopRecord() {
        isRecording = false;
        // 释放资源
        if (null != audioRecord) {
            audioRecord.stop();
            audioRecord.release();
            audioRecord = null;
        }
    }
}
