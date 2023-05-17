package com.shannon.openvoice.util

import android.content.Context
import android.util.DisplayMetrics
import androidx.recyclerview.widget.LinearSmoothScroller
import androidx.recyclerview.widget.RecyclerView
import timber.log.Timber

/**
 *
 * @ClassName:      SmoothTopScroller
 * @Description:     java类作用描述
 * @Author:         czhen
 */
class SmoothTopScroller(context: Context) : LinearSmoothScroller(context) {
    override fun getVerticalSnapPreference(): Int {
        return SNAP_TO_START
    }

    override fun calculateSpeedPerPixel(displayMetrics: DisplayMetrics?): Float {
        return super.calculateSpeedPerPixel(displayMetrics) / 2
    }

//    override fun calculateTimeForScrolling(dx: Int): Int {
//        val time = super.calculateTimeForScrolling(dx)
//        Timber.d("distance = $dx ; time = $time")
//        return time.coerceAtMost(163)
//    }
}
