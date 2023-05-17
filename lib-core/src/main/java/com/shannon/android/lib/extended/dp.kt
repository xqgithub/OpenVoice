package com.shannon.android.lib.extended

import android.content.res.Resources
import com.shannon.android.lib.constant.ConfigConstants

/**
 *
 * @ClassName:      dp
 * @Description:     java类作用描述
 * @Author:         czhen
 */
val Float.dp: Float
    get() = android.util.TypedValue.applyDimension(
        android.util.TypedValue.COMPLEX_UNIT_DIP, this,
        ConfigConstants.VARIABLE.globalApplication?.applicationContext?.resources?.displayMetrics
//        Resources.getSystem().displayMetrics
    )

val Int.dp: Int
    get() = android.util.TypedValue.applyDimension(
        android.util.TypedValue.COMPLEX_UNIT_DIP,
        this.toFloat(),
//        Resources.getSystem().displayMetrics
        ConfigConstants.VARIABLE.globalApplication?.applicationContext?.resources?.displayMetrics
    ).toInt()

val Int.pt: Int
    get() = android.util.TypedValue.applyDimension(
        android.util.TypedValue.COMPLEX_UNIT_PT,
        this.toFloat(),
        ConfigConstants.VARIABLE.globalApplication?.applicationContext?.resources?.displayMetrics
//        Resources.getSystem().displayMetrics
    ).toInt()

val Float.sp: Float
    get() = android.util.TypedValue.applyDimension(
        android.util.TypedValue.COMPLEX_UNIT_SP, this,
        ConfigConstants.VARIABLE.globalApplication?.applicationContext?.resources?.displayMetrics
//        Resources.getSystem().displayMetrics
    )

val Int.sp: Int
    get() = android.util.TypedValue.applyDimension(
        android.util.TypedValue.COMPLEX_UNIT_SP,
        this.toFloat(),
        ConfigConstants.VARIABLE.globalApplication?.applicationContext?.resources?.displayMetrics
//        Resources.getSystem().displayMetrics
    ).toInt()