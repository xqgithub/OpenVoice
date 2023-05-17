package com.shannon.openvoice.db

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.shannon.openvoice.model.StatusModel

/**
 *
 * @Package:        com.shannon.openvoice.db
 * @ClassName:      Converters
 * @Description:     作用描述
 * @Author:         czhen
 * @CreateDate:     2022/8/12 16:09
 */
class Converters {
    private val gson = Gson()


    @TypeConverter
    fun visibilityToInt(visibility: StatusModel.Visibility?): Int {
        return visibility?.num ?: StatusModel.Visibility.UNKNOWN.num
    }

    @TypeConverter
    fun intToVisibility(visibility: Int): StatusModel.Visibility {
        return StatusModel.Visibility.byNum(visibility)
    }

    @TypeConverter
    fun draftAttachmentListToJson(draftAttachments: List<DraftAttachment>?): String? {
        return gson.toJson(draftAttachments)
    }

    @TypeConverter
    fun jsonToDraftAttachmentList(draftAttachmentListJson: String?): List<DraftAttachment>? {
        return gson.fromJson(
            draftAttachmentListJson,
            object : TypeToken<List<DraftAttachment>>() {}.type
        )
    }
}