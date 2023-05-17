package com.shannon.openvoice.business.timeline.listener

import android.view.View
import com.shannon.openvoice.business.timeline.TimelineViewModel
import com.shannon.openvoice.model.StatusModel

/**
 *
 * @ProjectName:    OpenVoice
 * @Package:        com.shannon.openvoice.business.timeline
 * @ClassName:      StatusActionListener
 * @Description:     java类作用描述
 * @Author:         czhen
 * @CreateDate:     2022/8/2 11:44
 */
interface StatusActionListener : LinkListener {


    fun bindHost(
        timelineKind: TimelineViewModel.Kind,
        fragmentListener: FragmentListener,
        viewModel: TimelineViewModel,
        adapter: AdapterLogicListener
    )

    /**
     * 打开转发的内容
     * @param position Int
     */
    fun openReblog(position: Int)

    /**
     * 查看多媒体内容
     * @param position Int
     * @param attachmentIndex Int
     * @param view View?
     */
    fun onViewMedia(position: Int, attachmentIndex: Int, view: View?)

    /**
     * 回复
     * @param position Int
     */
    fun onReply(position: Int)

    /**
     * 转发
     * @param reblog Boolean
     * @param position Int
     */
    fun onReblog(reblog: Boolean, position: Int)

    /**
     * 收藏
     * @param favourite Boolean
     * @param position Int
     */
    fun onFavourite(favourite: Boolean, position: Int)


    /**
     * 购买声音模型
     * @param position Int
     */
    fun onPurchaseVoice(position: Int)

    /**
     * 更多
     * @param view View
     * @param position Int
     */
    fun onMore(view: View, position: Int)

    /**
     * 查看详情
     * @param position Int
     */
    fun onShowDetail(position: Int)

    /**
     * 查看模型详情
     * @param position Int
     */
    fun onShowModelDetail(position: Int)

    /**
     * 播放
     * @param position Int
     */
    fun onPlay(position: Int)

    fun pausePlayer()

//    /**
//     * 添加媒体
//     * @param mediaId String
//     * @param ossId String
//     */
//    fun addMediaItem(mediaId: String, ossId: String)

    fun addMediaItem(item: StatusModel)

    fun getTimelineKind(): TimelineViewModel.Kind

}