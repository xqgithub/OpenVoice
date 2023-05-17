package com.shannon.android.lib.util

import android.content.Context
import android.os.Handler
import android.os.Looper
import android.os.Message
import com.shannon.android.lib.extended.isBlankPlus
import java.lang.ref.WeakReference

/**
 * Date:2022/7/29
 * Time:17:08
 * author:dimple
 * Handler 工具类
 */
class HandlerUtils(looper: Looper) : Handler(looper) {

    private var mContext: WeakReference<Context>? = null

    private lateinit var onReceiveMessageListener: (msg: Message) -> Unit

    constructor(
        looper: Looper,
        context: Context,
        onReceiveMessageListener: (msg: Message) -> Unit
    ) : this(
        looper
    ) {
        mContext = WeakReference(context)
        this.onReceiveMessageListener = onReceiveMessageListener
    }

    override fun handleMessage(msg: Message) {
//        super.handleMessage(msg)
        if (isBlankPlus(mContext?.get())) return
        onReceiveMessageListener.invoke(msg)
    }

    object HandleWhatCode {
//        val testone = 0x9999
//        val testtwo = 0x9998
    }


}