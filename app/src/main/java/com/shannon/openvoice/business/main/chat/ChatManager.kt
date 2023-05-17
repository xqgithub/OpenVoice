package com.shannon.openvoice.business.main.chat

import com.shannon.openvoice.network.NetworkModule
import com.shannon.openvoice.network.apiService
import com.shannon.openvoice.network.convertResponse
import io.reactivex.rxjava3.disposables.Disposable
import okhttp3.WebSocketListener
import timber.log.Timber
import java.util.concurrent.atomic.AtomicInteger
import java.util.regex.Pattern

/**
 *
 * @Package:        com.shannon.openvoice.business.main.chat
 * @ClassName:      ChatManager
 * @Description:     作用描述
 * @Author:         czhen
 * @CreateDate:     2023/2/22 17:24
 */
class ChatManager private constructor() : WebSocketListener() {
    private var defaultMessageId = 1000
    private val webSocket by lazy {
        NetworkModule.instance.buildWebSocket(this)
    }
    private val callbackList = arrayListOf<OnMessageCallback>()
    private val messageList = arrayListOf<MessageBean>()
    private val nonRepliesNumber = AtomicInteger(0)
    private var chatDisposable: Disposable? = null

    fun onDestroy() {
        defaultMessageId = 1000
        callbackList.clear()
        messageList.clear()
        nonRepliesNumber.set(0)
        if (chatDisposable?.isDisposed == false) {
            chatDisposable?.dispose()
            chatDisposable = null
        }
    }

    companion object {
        val instance by lazy(LazyThreadSafetyMode.SYNCHRONIZED) {
            ChatManager()
        }
    }

    private val pattern: Pattern = Pattern.compile("\\p{P}") //匹配标点符号
    private val chinaPattern: Pattern = Pattern.compile("[\\u4e00-\\u9fa5]") //中文

    fun send(content: String, onError: () -> Unit) {
        addMessage(content)
        val lastChar = content.last().toString()
        Timber.d("content last: $lastChar")
        val match = pattern.matcher(lastChar).find()
        val isChina = chinaPattern.matcher(content).find()
        Timber.d("content match: $match")
        val submit = if (!match) {
            content.plus(if (isChina) "。" else ".")
        } else {
            content
        }
        Timber.d("content: $submit")

        chatDisposable = apiService.chatGPT(submit)
            .convertResponse()
            .subscribe({
//                val res = it.text.split("\n\n")
//                val message = if (res.size == 2) {
//                    res[1]
//                } else {
//                    it.text
//                }
                addMessage(it.text.trim(), false)
            }, {
                nonRepliesNumber.decrementAndGet()
                distributeInputStatus()
                onError()
            })
    }

    private fun addMessage(content: String, isOut: Boolean = true) {
        if (isOut) nonRepliesNumber.incrementAndGet() else nonRepliesNumber.decrementAndGet()
        val message = MessageBean(
            generateMessageId(),
            content,
            readState = if (isOut) MessageStatus.MSG_STATUS_POSITIVE else MessageStatus.MSG_STATUS_NEGATIVE,
            isOut = isOut
        )
        messageList.add(message)
        for (callback in callbackList) {
            callback.onMessage(message)
        }
        distributeInputStatus()
    }

    fun getAllMessage() = messageList

    private fun distributeInputStatus() {
        for (callback in callbackList) {
            callback.onInputStatus(nonRepliesNumber.get() > 0)
        }
    }

    /**
     * 最小间隔时间内不允许发送消息
     * @return Boolean
     */
    fun isMinInterval(): Boolean {
        return if (messageList.isNotEmpty()) {
            val messageBean = messageList.lastOrNull { it.isOut }
            if (messageBean == null) {
                false
            } else {
                System.currentTimeMillis() - messageBean.msgSendTime < 3000
            }
        } else {
            false
        }
    }

    /**
     * 达到最大未回复数量不允许发送消息
     * @return Boolean
     */
    fun isMaxNonReplies(): Boolean {
        return nonRepliesNumber.get() >= 3
    }

    fun registerMessageCallback(onMessageCallback: OnMessageCallback) {
        if (!callbackList.contains(onMessageCallback)) {
            callbackList.add(onMessageCallback)
        }
        distributeInputStatus()
    }

    fun unregisterMessageCallback(onMessageCallback: OnMessageCallback) {
        if (callbackList.contains(onMessageCallback)) {
            callbackList.remove(onMessageCallback)
        }
    }

    fun updateReadStatusById(messageBean: MessageBean) {
        if (messageBean.isOut) return
        messageList.forEach {
            if (it.messageId == messageBean.messageId && it.readState == MessageStatus.MSG_STATUS_NEGATIVE) {
                it.readState = MessageStatus.MSG_STATUS_POSITIVE
            }
        }
    }

    fun statisticsUnreadCount(): Int {
        var count = 0
        messageList.forEach {
            if (it.readState == MessageStatus.MSG_STATUS_NEGATIVE) {
                count++
            }
        }
        return count
    }

    fun clearUnreadFlag() {
        messageList.forEach {
            if (it.readState == MessageStatus.MSG_STATUS_NEGATIVE) {
                it.readState = MessageStatus.MSG_STATUS_POSITIVE
            }
        }
    }


    private fun generateMessageId() = defaultMessageId++
}