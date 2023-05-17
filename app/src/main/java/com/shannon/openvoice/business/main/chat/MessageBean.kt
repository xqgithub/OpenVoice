package com.shannon.openvoice.business.main.chat

import com.google.gson.annotations.SerializedName

/**
 *
 * @Package:        com.shannon.openvoice.business.main.chat
 * @ClassName:      MessageBean
 * @Description:     作用描述
 * @Author:         czhen
 * @CreateDate:     2023/2/22 11:31
 */
data class MessageBean(
    val messageId: Int,
    val messageContent: String,
    val sendState: Int = MessageStatus.MSG_STATUS_DEFAULT,
    var readState: Int = MessageStatus.MSG_STATUS_NEGATIVE,
    val msgSendTime: Long = System.currentTimeMillis(),
    val isOut: Boolean = false //true 为发送出去的消息；false 为接收的回复
)
