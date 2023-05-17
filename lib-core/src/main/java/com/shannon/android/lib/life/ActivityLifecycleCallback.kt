package com.shannon.android.lib.life

import android.app.Activity
import android.app.Application
import android.os.Bundle
import android.util.Log
import timber.log.Timber

/**
 *
 * @Package:        com.shannon.android.lib.life
 * @ClassName:      ActivityLifecycleCallback
 * @Description:     作用描述
 * @Author:         czhen
 * @CreateDate:     2022/8/11 15:49
 */
class ActivityLifecycleCallback : Application.ActivityLifecycleCallbacks {

    companion object {

        fun register(application: Application) {
            application.registerActivityLifecycleCallbacks(ActivityLifecycleCallback())
        }
    }

    private var isLaunch = true
    private var activityCount = 0

    override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {
    }

    override fun onActivityStarted(activity: Activity) {
        if (!isLaunch && activityCount == 0 && activity is LifecycleCallback) {
            activity.onForeground()
        }
        isLaunch = false
        activityCount++
    }

    override fun onActivityResumed(activity: Activity) {
    }

    override fun onActivityPaused(activity: Activity) {
    }

    override fun onActivityStopped(activity: Activity) {
        activityCount--
        if (activityCount == 0 && activity is LifecycleCallback) {
            activity.onBackground()
        }
    }

    override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {
    }

    override fun onActivityDestroyed(activity: Activity) {
    }
}