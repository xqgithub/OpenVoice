package com.shannon.android.lib.player.media

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

/**
 *
 * @ClassName:      MediaData
 * @Description:     java类作用描述
 * @Author:         czhen
 */
@Parcelize
data class MediaData(val mediaId: String, val mediaUri: String) : Parcelable
