package com.shannon.openvoice.util.tabs

import android.view.View
import android.widget.FrameLayout

/**
 *
 * @ProjectName:    AIFUN-ANDROID
 * @Package:        com.shannon.openvoice.util.tabs.tabs
 * @ClassName:      OnTabPageChangeCallback
 * @Description:     java类作用描述
 * @Author:         czhen
 * @CreateDate:     2021/10/4 11:24
 */
interface OnTabPageChangeCallback {

    fun onTabSelected(view: View)

    fun onTabUnselected(view: View)

    fun onPageScrolled(selectedChild: View, nextTabView: View, positionOffset: Float)
}