package com.shannon.openvoice.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

/**
 * Date:2022/8/4
 * Time:9:39
 * author:dimple
 * 多语言实体类
 */
@Parcelize
data class LanguageBean(
    val LanguageEntries: String,//语言key
    val LanguageValues: String//语言的value
) : Parcelable
