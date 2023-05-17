package com.shannon.openvoice.util

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper

/**
 *
 * @Package:        com.shannon.openvoice.util
 * @ClassName:      ActivityCompatHelper
 * @Description:     作用描述
 * @Author:         czhen
 * @CreateDate:     2023/3/23 15:38
 */
object ActivityCompatHelper {

      fun assertValidRequest(context: Context): Boolean {
        if (context is Activity) {
            return !isDestroy(context)
        } else if (context is ContextWrapper) {
            if (context.baseContext is Activity) {
                val activity = context.baseContext as Activity
                return !isDestroy(activity)
            }
        }
        return true
    }

      fun isDestroy(activity: Activity?): Boolean {
        return if (activity == null) {
            true
        } else activity.isFinishing || activity.isDestroyed
    }
}