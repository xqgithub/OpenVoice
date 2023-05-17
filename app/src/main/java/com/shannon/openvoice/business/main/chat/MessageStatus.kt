package com.shannon.openvoice.business.main.chat

/**
 *
 * @Package:        com.shannon.openvoice.business.main.chat
 * @ClassName:      MessageStatus
 * @Description:     作用描述
 * @Author:         czhen
 * @CreateDate:     2023/2/22 11:38
 */
object MessageStatus {

    const val MSG_STATUS_DEFAULT = 0  //发送默认状态
    const val MSG_STATUS_POSITIVE = 1 //发送成功 & 已读
    const val MSG_STATUS_NEGATIVE = 2 //发送失败 & 未读
    const val MSG_STATUS_WORKING = 3  //发送中
}