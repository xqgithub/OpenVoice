package com.shannon.openvoice.business.main.mine.blacklist

import android.content.Context
import com.shannon.android.lib.base.adapter.BaseFunBindingRecyclerViewAdapter
import com.shannon.openvoice.R
import com.shannon.openvoice.business.main.mine.announcement.AnnouncementsAdapter
import com.shannon.openvoice.databinding.AdapterBlacklistBinding
import com.shannon.openvoice.extended.loadAvatar
import com.shannon.openvoice.model.TimelineAccount
import me.jingbin.library.adapter.BaseByViewHolder

/**
 * Date:2022/8/15
 * Time:10:05
 * author:dimple
 * 黑名单列表 适配器
 */
class BlackBlistAdapter(val mContext: Context) :
    BaseFunBindingRecyclerViewAdapter<TimelineAccount, AdapterBlacklistBinding>() {

    override fun bindView(
        holder: BaseByViewHolder<TimelineAccount>,
        binding: AdapterBlacklistBinding,
        bean: TimelineAccount,
        position: Int
    ) {
        binding.apply {
            sivAvatar.loadAvatar(bean.avatar)
            tvName.text = bean.name
            tvUserName.text = "@${bean.localUsername}"
            tvDate.text = bean.lastStatusAt
        }

        holder.addOnClickListener(R.id.ibDelete)
    }

    override fun bindView(binding: AdapterBlacklistBinding, bean: TimelineAccount, position: Int) {

    }

}