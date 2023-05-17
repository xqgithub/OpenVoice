package com.shannon.openvoice.model

/**
 * Date:2022/9/2
 * Time:15:00
 * author:dimple
 */
data class NotificationTypeSelectedBean(
    var notificationtype: Notification.Type,
    var typeDisplay: String,
    var isTypeSelected: Boolean
) {

}