package com.shannon.openvoice.model

import com.google.gson.annotations.SerializedName
import java.util.*

/**
 * Date:2022/8/12
 * Time:15:55
 * author:dimple
 * 公告实体类
 */
data class AnnouncementBean(
    val id: String,//公告 ID
    val content: String,//公告的内容
    @SerializedName("starts_at")
    val startsAt: Date?,
    @SerializedName("ends_at")
    val endsAt: Date?,
    @SerializedName("all_day")
    val allDay: Boolean,//公告是否有开始/结束时间。
    @SerializedName("published_at")
    val publishedAt: Date,
    @SerializedName("updated_at")
    val updatedAt: Date,
    val read: Boolean,//用户是否已阅读公告。
    val tags: List<HashTag>,
    val emojis: List<Emoji>,
    val reactions: List<Reaction>,
    val mentions: List<StatusModel.Mention>,
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || javaClass != other.javaClass) return false

        val announcement = other as AnnouncementBean?
        return id == announcement?.id
    }

    override fun hashCode(): Int {
        return id.hashCode()
    }

    data class Reaction(
        val name: String,
        var count: Int,
        var me: Boolean,
        val url: String?,
        @SerializedName("static_url") val staticUrl: String?
    )
}

