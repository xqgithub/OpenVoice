package com.shannon.android.lib.util

import android.text.TextUtils
import android.util.Log
import com.shannon.android.lib.extended.gameel

/**
 * 描述　:
 */
object MLogUtils {
    private const val DEFAULT_TAG = "opv_haha"

    /**
     * 这里使用自己分节的方式来输出足够长度的 message
     *
     * @param tag 标签
     * @param msg 日志内容
     */
    fun debugLongInfo(tag: String = DEFAULT_TAG, msg: String) {
        var msg = msg
        if (!gameel || TextUtils.isEmpty(msg)) {
            return
        }
        msg = msg.trim { it <= ' ' }
        var index = 0
        val maxLength = 10000
        var sub: String
        while (index < msg.length) {
            sub = if (msg.length <= index + maxLength) {
                msg.substring(index)
            } else {
                msg.substring(index, index + maxLength)
            }
            index += maxLength
            Log.d(tag, sub.trim { it <= ' ' })
        }
    }


    /**
     * 自定义长度 输出message
     */
    fun longInfo(tag: String = DEFAULT_TAG, msg: String) {
        var msg = msg
        if (!gameel || TextUtils.isEmpty(msg)) {
            return
        }
        msg = msg.trim { it <= ' ' }
        var index = 0
        val maxLength = 3500
        var sub: String
        while (index < msg.length) {
            sub = if (msg.length <= index + maxLength) {
                msg.substring(index)
            } else {
                msg.substring(index, index + maxLength)
            }
            index += maxLength
            Log.i(tag, sub.trim { it <= ' ' })
        }
    }

}