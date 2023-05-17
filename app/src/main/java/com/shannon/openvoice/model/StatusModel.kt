package com.shannon.openvoice.model

import android.os.Parcelable
import android.text.SpannableStringBuilder
import android.text.style.URLSpan
import com.google.gson.annotations.SerializedName
import com.shannon.android.lib.extended.parseAsMastodonHtml
import kotlinx.parcelize.Parcelize
import timber.log.Timber
import java.util.*

/**
 *
 * @ProjectName:    OpenVoice
 * @Package:        com.shannon.openvoice.model
 * @ClassName:      StatusModel
 * @Description:     java类作用描述
 * @Author:         czhen
 * @CreateDate:     2022/8/1 10:33
 */
@Parcelize
data class StatusModel(
    val id: String,
    val url: String,
    val account: TimelineAccount,//用户简要信息
    @SerializedName("in_reply_to_id") var inReplyToId: String?, //正在回复的状态的 ID。
    @SerializedName("in_reply_to_account_id") val inReplyToAccountId: String?,//被回复的账号ID
    val reblog: StatusModel?,
    val content: String,
    @SerializedName("created_at") val createdAt: Date,
    @SerializedName("replies_count") val repliesCount: Long,//回复数量
    @SerializedName("reblogs_count") val reblogCount: Long,//转发次数
    @SerializedName("favourites_count") val favouriteCount: Long,//收藏次数
    var reblogged: Boolean,//是否被转发
    var favourited: Boolean,//是否被收藏
    var bookmarked: Boolean,//是否标记书签
    var sensitive: Boolean,//是否标记为敏感内容
    var featured: Boolean,
    @SerializedName("spoiler_text")
    val spoilerText: String,//主题或摘要(发推中的警告内容)，推文的内容在其下方默认折叠
    val visibility: Visibility,//显示状态，public：所有人可见；unlisted：所有人可见，但不展示在公共时间轴中；private：仅关注者可见；direct：私信，只有被@的用户可见
    @SerializedName("media_attachments")
    var attachments: ArrayList<Attachment>,//推文关联的媒体内容
    val mentions: List<Mention>,//推文中被@的用户
    val tags: List<HashTag>?,//推文中使用的话题(#)
    val application: Application?,//发布此推文的站点
    val pinned: Boolean?,//是否置顶
    val muted: Boolean?,//是否静音此推文的通知

//    val poll: Poll?,//投票
//    val card: Card?,//显示链接预览
    var isPlaying: Boolean,
    var playingButtonDisappears: Boolean,//播放按钮消失
    @SerializedName("voice_model")
    val voiceModel: VoiceModel?,//声音模型
    @SerializedName("oss_id")
    val ossId: String?,//声音文件ID
    val price: String?,//声音模型价格
    val currency: String?,//价格单位
    @SerializedName("tts_generated")
    val ttsGenerated: Boolean
) : Parcelable {

    val actionableId: String
        get() = reblog?.id ?: id

    val actionableStatus: StatusModel
        get() = reblog ?: this

    val reblogStatus: StatusModel?
        get() = reblog

    val reblogAvatar: String
        get() = if (reblog != null) {
            account.avatar
        } else {
            ""
        }

    val reblogAllowed: Boolean
        get() = (visibility != Visibility.DIRECT && visibility != Visibility.UNKNOWN)

    val pinAllowed: Boolean
        get() = (visibility == Visibility.PUBLIC || visibility == Visibility.UNLISTED)

    val isPinned: Boolean
        get() = pinned ?: false

    enum class Visibility(val num: Int) {
        UNKNOWN(0),

        @SerializedName("public")
        PUBLIC(1),

        @SerializedName("unlisted")
        UNLISTED(2),

        @SerializedName("private")
        PRIVATE(3),

        @SerializedName("direct")
        DIRECT(4);

        fun serverString(): String {
            return when (this) {
                PUBLIC -> "public"
                UNLISTED -> "unlisted"
                PRIVATE -> "private"
                DIRECT -> "direct"
                UNKNOWN -> "unknown"
            }
        }

        companion object {

            @JvmStatic
            fun byNum(num: Int): Visibility {
                return when (num) {
                    4 -> DIRECT
                    3 -> PRIVATE
                    2 -> UNLISTED
                    1 -> PUBLIC
                    0 -> UNKNOWN
                    else -> UNKNOWN
                }
            }

            @JvmStatic
            fun byString(s: String): Visibility {
                return when (s) {
                    "public" -> PUBLIC
                    "unlisted" -> UNLISTED
                    "private" -> PRIVATE
                    "direct" -> DIRECT
                    "unknown" -> UNKNOWN
                    else -> UNKNOWN
                }
            }
        }
    }

    @Parcelize
    data class Mention(
        val id: String,
        val url: String,
        @SerializedName("acct") val username: String,
        @SerializedName("username") val localUsername: String
    ) : Parcelable

    @Parcelize
    data class Application(
        val name: String,
        val website: String?
    ) : Parcelable

    @Parcelize
    data class VoiceModel @JvmOverloads constructor(
        val id: Long,
        val name: String,
        @SerializedName("owner_account") val ownerAccount: TimelineAccount,
        val status: String?,
        @SerializedName("is_official") val isOfficial: Boolean,
        @SerializedName("is_liked") val isLiked: Boolean,
        val activated: Boolean,
        @SerializedName("is_model_owner") val isModelOwner: Boolean,
        val price: String?,
        val currency: String?,
        @SerializedName("payload") val payload: String?,
        @SerializedName("audition_file") val auditionFile: String?,//官方模型的试听文件
        @SerializedName("like_count") val likeCount: Long,
        @SerializedName("usage_count") val usageCount: Long,
        @SerializedName("trading_count") val tradingCount: Long,
    ) : Parcelable

    fun getEditableText(): String {
        val contentSpanned = content.parseAsMastodonHtml()
        val builder = SpannableStringBuilder(content.parseAsMastodonHtml())
        Timber.e("contentSpanned = $contentSpanned")
        for (span in contentSpanned.getSpans(0, content.length, URLSpan::class.java)) {
            val url = span.url
            for ((_, url1, username) in mentions) {
                if (url == url1) {
                    val start = builder.getSpanStart(span)
                    val end = builder.getSpanEnd(span)
                    if (start != -1 && end != -1) builder.replace(start, end, "@$username")
                    break
                }
            }
        }
        return builder.toString()
    }

    override fun equals(other: Any?): Boolean {
        if (other !is StatusModel) {
            return false
        }
        return other.id == this.id
    }

    override fun hashCode(): Int {
        return this.id.hashCode()
    }

    companion object {
        const val MAX_MEDIA_ATTACHMENTS = 4
        const val MAX_POLL_OPTIONS = 4
    }
}
