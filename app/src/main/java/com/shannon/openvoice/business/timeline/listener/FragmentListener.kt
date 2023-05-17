package com.shannon.openvoice.business.timeline.listener

import android.app.Activity

/**
 *
 * @Package:        com.shannon.openvoice.business.timeline.listener
 * @ClassName:      FragmentListener
 * @Description:     作用描述
 * @Author:         czhen
 * @CreateDate:     2022/9/26 19:31
 */
interface FragmentListener {

    fun requireAct(): Activity

    fun deleted()

    fun getTimelinePageId():String
}