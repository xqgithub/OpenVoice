package com.shannon.openvoice.initializer

import com.shannon.openvoice.FunApplication
import com.shannon.openvoice.business.player.PlayerHelper

/**
 *
 * @Package:        com.shannon.openvoice.initializer
 * @ClassName:      PlayerTask
 * @Description:     作用描述
 * @Author:         czhen
 * @CreateDate:     2023/3/17 15:08
 */
class PlayerTask : FunTask() {
    override fun run() {
        PlayerHelper.instance.connectMusicService(FunApplication.getInstance().applicationContext)
    }
}