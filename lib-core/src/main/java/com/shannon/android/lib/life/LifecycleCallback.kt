package com.shannon.android.lib.life

/**
 *
 * @Package:        com.shannon.android.lib.life
 * @ClassName:      LifecycleCallback
 * @Description:     作用描述
 * @Author:         czhen
 * @CreateDate:     2022/8/11 15:47
 */
interface LifecycleCallback {

    fun onForeground()

    fun onBackground()
}