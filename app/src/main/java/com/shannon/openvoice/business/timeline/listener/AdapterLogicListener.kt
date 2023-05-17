package com.shannon.openvoice.business.timeline.listener

import com.shannon.openvoice.model.AccountBean
import com.shannon.openvoice.model.StatusModel

/**
 *
 * @Package:        com.shannon.openvoice.business.timeline.listener
 * @ClassName:      AdapterLogicListener
 * @Description:     作用描述
 * @Author:         czhen
 * @CreateDate:     2022/8/18 9:40
 */
interface AdapterLogicListener {

    fun getLogicData(): List<StatusModel>

    fun findStatusById(id: String): Int

    fun getDataActionableId(position: Int): String

    fun getDataId(position: Int): String

    fun onReply(position: Int)

    fun onReblog(status: StatusModel?, position: Int)

    fun onFavourite(status: StatusModel?)

    fun onUpdateVoiceModel(status: StatusModel)

    /**
     * 购买了模型
     * @param modelId: Long
     */
    fun onPurchasedVoiceModel(modelId: Long)

    fun onPin(status: StatusModel)

    fun removeAllByAccountId(accountId: String): List<StatusModel>

    fun playingStateChange(isPlaying: Boolean, position: Int)

    fun removeItem(status: StatusModel)

    fun updateAccountData(account: AccountBean)

    fun updateVoice(id: String, ossId: String)
}