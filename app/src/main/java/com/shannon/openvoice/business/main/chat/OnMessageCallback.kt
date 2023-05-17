package com.shannon.openvoice.business.main.chat

/**
 *
 * @Package:        com.shannon.openvoice.business.main.chat
 * @ClassName:      OnMessageCallback
 * @Description:     作用描述
 * @Author:         czhen
 * @CreateDate:     2023/2/23 10:22
 */
interface OnMessageCallback {

    fun onMessage(messageBean: MessageBean)

    fun onInputStatus(isEntering: Boolean) {}
}