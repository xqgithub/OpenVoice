package com.shannon.openvoice.business.main.mine.account

import android.content.Context
import com.shannon.android.lib.base.adapter.BaseFunBindingRecyclerViewAdapter
import com.shannon.openvoice.R
import com.shannon.openvoice.databinding.AdapterFollowingFollowerBinding
import com.shannon.openvoice.extended.loadAvatar
import com.shannon.openvoice.model.TimelineAccount
import me.jingbin.library.adapter.BaseByViewHolder

/**
 * Date:2022/8/18
 * Time:15:36
 * author:dimple
 * 关注者和追随者列表适配器
 */
class FollowingAndFollowerAdapter(val mContext: Context) :
    BaseFunBindingRecyclerViewAdapter<TimelineAccount, AdapterFollowingFollowerBinding>() {

    override fun bindView(
        holder: BaseByViewHolder<TimelineAccount>,
        binding: AdapterFollowingFollowerBinding,
        bean: TimelineAccount,
        position: Int
    ) {
        binding.apply {
            sivAvatar.loadAvatar(bean.avatar)
            tvNickname.text = bean.name
            tvUserAccount.text = "@${bean.localUsername}"
        }
        holder.addOnClickListener(R.id.cl_main)
    }

    override fun bindView(
        binding: AdapterFollowingFollowerBinding,
        bean: TimelineAccount,
        position: Int
    ) {
    }
}