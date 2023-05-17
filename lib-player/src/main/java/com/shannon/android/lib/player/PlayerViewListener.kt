package com.shannon.android.lib.player

/**
 *
 * @Package:        com.shannon.android.lib.player
 * @ClassName:      PlayerViewListener
 * @Description:     作用描述
 * @Author:         czhen
 * @CreateDate:     2022/9/19 17:51
 */
interface PlayerViewListener {
    fun isSkipToPreviousEnabled(hasPreviousItem: Boolean)

    fun isSkipToNextEnabled(hasNextItem: Boolean)
}