package com.shannon.openvoice.business.creation.detail

import androidx.core.content.ContextCompat
import com.shannon.android.lib.base.adapter.BaseFunBindingRecyclerViewAdapter
import com.shannon.android.lib.extended.singleClick
import com.shannon.android.lib.extended.visibility
import com.shannon.openvoice.R
import com.shannon.openvoice.business.main.mine.account.AccountActivity
import com.shannon.openvoice.business.main.mine.account.AccountManager
import com.shannon.openvoice.databinding.ItemModelLikeRecordBinding
import com.shannon.openvoice.extended.loadAvatar
import com.shannon.openvoice.model.Likes
import com.shannon.openvoice.model.ModelLikes
import com.shannon.openvoice.util.TimestampUtils
import me.jingbin.library.adapter.BaseByViewHolder

/**
 *
 * @Package:        com.shannon.openvoice.business.creation.detail
 * @ClassName:      LikeRecordAdapter
 * @Description:     作用描述
 * @Author:         czhen
 * @CreateDate:     2022/8/27 9:45
 */
class LikeRecordAdapter : BaseFunBindingRecyclerViewAdapter<ModelLikes, ItemModelLikeRecordBinding>() {

    override fun bindView(binding: ItemModelLikeRecordBinding, bean: ModelLikes, position: Int) {
        val context = binding.root.context
        binding.run {
            userAvatarView.loadAvatar(bean.account.avatar)
            statusDisplayName.text = bean.account.name
            userNameView.text =
                context.getString(R.string.post_username_format, bean.account.username)
            followButton.isSelected = bean.isFollow
            followButton.text =
                context.getString(if (bean.isFollow) R.string.following else R.string.follow)
            followButton.visibility(bean.account.id != AccountManager.accountManager.getAccountId())
            root.singleClick {
                context.startActivity(
                    AccountActivity.newIntent(
                        context,
                        bean.account.id
                    )
                )
            }
        }
    }

    override fun loadHolder(holder: BaseByViewHolder<ModelLikes>) {
        holder.addOnClickListener(R.id.followButton)
    }
}