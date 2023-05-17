package com.shannon.openvoice.business.player.listener

/**
 *
 * @Package:        com.shannon.openvoice.business.player.listener
 * @ClassName:      OnDataSourceListener
 * @Description:     作用描述
 * @Author:         czhen
 * @CreateDate:     2023/3/27 16:26
 */
interface OnDataSourceListener {

    fun bindPlaylist(type:Int):Boolean
}