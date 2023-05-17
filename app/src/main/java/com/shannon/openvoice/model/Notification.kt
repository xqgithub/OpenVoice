package com.shannon.openvoice.model

import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import com.google.gson.JsonParseException
import com.google.gson.annotations.JsonAdapter
import com.google.gson.annotations.SerializedName
import com.shannon.openvoice.business.main.notification.NotificationTypeAdapter
import com.shannon.openvoice.model.StatusModel.Visibility.Companion.byString
import java.util.*

/**
 * Date:2022/8/31
 * Time:15:55
 * author:dimple
 */
data class Notification(
    val type: Type,
    val id: String,
    @SerializedName("created_at") val createdAt: Date,
    val account: TimelineAccount,
    val status: StatusModel?,
    @SerializedName("voice_model") val voiceModel: StatusModel.VoiceModel?,//声音模型
) {
    @JsonAdapter(NotificationTypeAdapter::class)
    enum class Type(val presentation: String) {
        @SerializedName("unknown")
        UNKNOWN("unknown"),

        @SerializedName("mention")
        MENTION("mention"),

        @SerializedName("reblog")
        REBLOG("reblog"),

        @SerializedName("favourite")
        FAVOURITE("favourite"),

        @SerializedName("follow")
        FOLLOW("follow"),

        @SerializedName("voice_model.created")
        VOICEMODELCREATED("voice_model.created"),//模型创建成功

        @SerializedName("voice_model.generated")
        VOICEMODELGENERATED("voice_model.generated"),//模型生成成功

        @SerializedName("voice_model.generate_failed")
        VOICEMODELGENERATEFAILED("voice_model.generate_failed"),//模型生成失败

        @SerializedName("voice_model.boughted")
        VOICEMODELBOUGHTED("voice_model.boughted"),//模型购买

        @SerializedName("voice_model.solded")
        VOICEMODELSOLDED("voice_model.solded"),//模型售出

        /** 以下几种是不要的，要排除的 **/
        @SerializedName("follow_request")
        FOLLOW_REQUEST("follow_request"),

        @SerializedName("poll")
        POLL("poll"),

        @SerializedName("status")
        STATUS("status"),

        @SerializedName("admin.sign_up")
        SIGN_UP("admin.sign_up"),

        @SerializedName("update")
        UPDATE("update"),
        ;

        companion object {

            @JvmStatic
            fun byString(s: String): Type {
                values().forEach {
                    if (s == it.presentation)
                        return it
                }
                return UNKNOWN
            }

            val asList = listOf(
                MENTION,
                REBLOG,
                FAVOURITE,
                FOLLOW,
                VOICEMODELCREATED,
                VOICEMODELGENERATED,
                VOICEMODELGENERATEFAILED,
                VOICEMODELBOUGHTED,
                VOICEMODELSOLDED
                /** 以下是不要的，要排除的 **/
//                FOLLOW_REQUEST,
//                POLL,
//                STATUS,
//                SIGN_UP,
//                UPDATE
            )
        }

        override fun toString(): String {
            return presentation
        }
    }

    override fun hashCode(): Int {
        return id.hashCode()
    }

    override fun equals(other: Any?): Boolean {
        if (other !is Notification) {
            return false
        }
        val notification = other as Notification?
        return notification?.id == this.id
    }

    class NotificationTypeAdapter : JsonDeserializer<Type> {

        @Throws(JsonParseException::class)
        override fun deserialize(
            json: JsonElement,
            typeOfT: java.lang.reflect.Type,
            context: JsonDeserializationContext
        ): Type {
            return Type.byString(json.asString)
        }
    }

    /** Helper for Java */
    fun copyWithStatus(status: StatusModel?): Notification = copy(status = status)

    // for Pleroma compatibility that uses Mention type
    fun rewriteToStatusTypeIfNeeded(accountId: String): Notification {
        if (type == Type.MENTION && status != null) {
            return if (status.mentions.any {
                    it.id == accountId
                }
            ) this else copy(type = Type.MENTION)//Type.STATUS
        }
        return this
    }

}
