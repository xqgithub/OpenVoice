package com.shannon.openvoice.db

import android.net.Uri
import android.os.Parcelable
import androidx.core.net.toUri
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.google.gson.annotations.SerializedName
import com.shannon.openvoice.model.StatusModel
import kotlinx.parcelize.Parcelize

/**
 *
 * @Package:        com.shannon.openvoice.db
 * @ClassName:      DraftModel
 * @Description:     作用描述
 * @Author:         czhen
 * @CreateDate:     2022/8/12 16:09
 */
@Entity
@TypeConverters(Converters::class)
@Parcelize
data class DraftModel(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    val id: Int = 0,
    @ColumnInfo(name = "accountId")
    val accountId: String,
    @ColumnInfo(name = "inReplyToId")
    val inReplyToId: String?, //被回复的声文ID
    val inReplyAuthor: String?,//被回复的声文作者
    @ColumnInfo(name = "content")
    val content: String?,
    @ColumnInfo(name = "visibility")
    val visibility: StatusModel.Visibility,
    @ColumnInfo(name = "voiceModelId")
    val voiceModelId: Long,
    val attachments: List<DraftAttachment>,
    @ColumnInfo(name = "failedToSend")
    val failedToSend: Boolean
) : Parcelable

@Parcelize
data class DraftAttachment(
    @SerializedName(value = "uriString", alternate = ["e", "i"]) val uriString: String,
    @SerializedName(value = "type", alternate = ["g", "k"]) val type: Type,
    val mediaSize: Long,
    val serverId: String?
) : Parcelable {
    val uri: Uri
        get() = uriString.toUri()


    enum class Type {
        IMAGE, VIDEO,UNKNOW;
    }
}