package com.shannon.openvoice.util.tabs

import com.google.android.material.tabs.TabLayout

/**
 *
 * @Package:        com.shannon.openvoice.util.tabs
 * @ClassName:      ITabConfigurationStrategy
 * @Description:     作用描述
 * @Author:         czhen
 * @CreateDate:     2023/3/18 11:15
 */
interface ITabConfigurationStrategy {

    fun onConfigureTab(tab: TabLayout.Tab, position: Int)
}