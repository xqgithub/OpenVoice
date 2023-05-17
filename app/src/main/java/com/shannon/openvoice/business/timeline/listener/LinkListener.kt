package com.shannon.openvoice.business.timeline.listener

/**
 *
 * @ProjectName:    OpenVoice
 * @Package:        com.shannon.openvoice.business.timeline
 * @ClassName:      LinkListener
 * @Description:     java类作用描述
 * @Author:         czhen
 * @CreateDate:     2022/8/2 11:50
 */
interface LinkListener {
    /**
     * 查看话题
     * @param tag String
     */
    fun onViewTag(tag: String)

    /**
     * 查看用户详情
     * @param id String
     */
    fun onViewAccount(id: String)

    /**
     * 打开链接
     * @param url String
     */
    fun onViewUrl(url: String)
}