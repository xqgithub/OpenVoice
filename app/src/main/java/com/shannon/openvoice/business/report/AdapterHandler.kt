package com.shannon.openvoice.business.report

import android.view.View
import com.shannon.openvoice.business.timeline.listener.LinkListener

/**
 *
 * @Package:        com.shannon.openvoice.business.report
 * @ClassName:      AdapterHandler
 * @Description:     作用描述
 * @Author:         czhen
 * @CreateDate:     2022/8/23 10:04
 */
interface AdapterHandler : LinkListener {
    fun showMedia(v: View?, imageIndex: Int, itemPosition: Int)
    fun setStatusChecked(itemPosition: Int, isChecked: Boolean)
    fun isStatusChecked(id: String): Boolean
}