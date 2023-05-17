package com.shannon.openvoice.business.main.mine.announcement

import android.content.Context
import com.shannon.android.lib.base.adapter.BaseFunBindingRecyclerViewAdapter
import com.shannon.android.lib.extended.parseAsMastodonHtml
import com.shannon.openvoice.business.timeline.listener.LinkListener
import com.shannon.openvoice.databinding.AdapterAnnouncementsBinding
import com.shannon.openvoice.model.AnnouncementBean
import com.shannon.openvoice.util.setClickableText

/**
 * Date:2022/8/12
 * Time:15:47
 * author:dimple
 * 公告页面内容
 */
class AnnouncementsAdapter(
    val mContext: Context,
    val linkListener: LinkListener
) :
    BaseFunBindingRecyclerViewAdapter<AnnouncementBean, AdapterAnnouncementsBinding>() {
    override fun bindView(
        binding: AdapterAnnouncementsBinding,
        bean: AnnouncementBean,
        position: Int
    ) {
        binding.apply {
            tvContent.text = bean.content.parseAsMastodonHtml()
            setClickableText(
                tvContent,
                bean.content.parseAsMastodonHtml(),
                bean.mentions,
                bean.tags,
                linkListener
            )
        }
    }
}