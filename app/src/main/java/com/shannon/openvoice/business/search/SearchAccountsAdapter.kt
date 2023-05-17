package com.shannon.openvoice.business.search

import android.content.Context
import androidx.core.content.ContextCompat
import androidx.fragment.app.viewModels
import com.shannon.android.lib.base.adapter.BaseFunBindingRecyclerViewAdapter
import com.shannon.android.lib.extended.singleClick
import com.shannon.android.lib.extended.visibility
import com.shannon.openvoice.R
import com.shannon.openvoice.business.main.mine.MineViewModel
import com.shannon.openvoice.business.main.mine.account.AccountManager
import com.shannon.openvoice.business.main.mine.account.AccountManager.Companion.accountManager
import com.shannon.openvoice.business.timeline.TimelineViewModel
import com.shannon.openvoice.databinding.AdapterSearchAccountsBinding
import com.shannon.openvoice.extended.loadAvatar
import com.shannon.openvoice.model.TimelineAccount
import me.jingbin.library.adapter.BaseByViewHolder

/**
 * Date:2022/8/29
 * Time:15:09
 * author:dimple
 * 搜索用户适配器
 */
class SearchAccountsAdapter(val mContext: Context) :
    BaseFunBindingRecyclerViewAdapter<TimelineAccount, AdapterSearchAccountsBinding>() {

    override fun bindView(
        holder: BaseByViewHolder<TimelineAccount>,
        binding: AdapterSearchAccountsBinding,
        bean: TimelineAccount,
        position: Int
    ) {
        binding.apply {
            sivAvatar.loadAvatar(bean.avatar)
            tvName.text = "${bean.name}"
            tvUserName.text = "@${bean.localUsername}"

            sbFocusOnOperation.apply {
                visibility(!accountManager.accountIsMe(bean.id))
                
                background = ContextCompat.getDrawable(
                    mContext,
                    if (bean.following) R.drawable.shape_fourth_background2 else R.drawable.shape_fourth_background
                )
                text =
                    mContext.getString(if (bean.following) R.string.following else R.string.follow)
                setTextColor(
                    ContextCompat.getColor(
                        mContext,
                        if (bean.following) R.color.white else R.color.black
                    )
                )
            }

            holder.addOnClickListener(R.id.cl_main)
            holder.addOnClickListener(R.id.sbFocusOnOperation)
        }
    }


    override fun bindView(
        binding: AdapterSearchAccountsBinding,
        bean: TimelineAccount,
        position: Int
    ) {
    }


}