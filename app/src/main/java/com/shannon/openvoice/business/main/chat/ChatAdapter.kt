package com.shannon.openvoice.business.main.chat

import android.view.ViewGroup
import android.widget.TextView
import com.shannon.android.lib.base.adapter.BaseFunBindingViewHolder
import com.shannon.android.lib.extended.gone
import com.shannon.android.lib.extended.inflate
import com.shannon.android.lib.extended.visible
import com.shannon.openvoice.business.main.mine.account.AccountManager
import com.shannon.openvoice.databinding.ChatItemInBinding
import com.shannon.openvoice.databinding.ChatItemOutBinding
import com.shannon.openvoice.extended.loadAvatar
import com.shannon.openvoice.util.TimestampUtils
import me.jingbin.library.adapter.BaseByRecyclerViewAdapter
import me.jingbin.library.adapter.BaseByViewHolder

/**
 *
 * @Package:        com.shannon.openvoice.business.main.chat
 * @ClassName:      ChatAdapter
 * @Description:     作用描述
 * @Author:         czhen
 * @CreateDate:     2023/2/22 17:46
 */
class ChatAdapter : BaseByRecyclerViewAdapter<MessageBean, BaseByViewHolder<MessageBean>>() {
    private val TIME_INTERVAL = 300 * 1000

    private val accountManager by lazy { AccountManager() }

    override fun getItemViewType(position: Int): Int {
        return if (getItemData(position).isOut) 0 else 1
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): BaseByViewHolder<MessageBean> {
        return if (viewType == 0) {
            val binding = inflate<ChatItemOutBinding>(parent)
            ChatOutViewHolder(binding)
        } else {
            val binding = inflate<ChatItemInBinding>(parent)
            ChatInViewHolder(binding)
        }
    }


    inner class ChatOutViewHolder(binding: ChatItemOutBinding) :
        BaseFunBindingViewHolder<MessageBean, ChatItemOutBinding>(binding) {

        override fun onBindView(binding: ChatItemOutBinding, bean: MessageBean, position: Int) {
            binding.run {
                avatarView.loadAvatar(accountManager.getAccountData().avatar)
                userNameView.text = accountManager.getAccountData().displayName
                updateTime(
                    if (position == 0) null else getItemData(position - 1),
                    bean,
                    timestampView
                )

                messageView.text = bean.messageContent
            }
        }
    }

    inner class ChatInViewHolder(binding: ChatItemInBinding) :
        BaseFunBindingViewHolder<MessageBean, ChatItemInBinding>(binding) {
        override fun onBindView(binding: ChatItemInBinding, bean: MessageBean, position: Int) {
            binding.run {
                updateTime(
                    if (position == 0) null else getItemData(position - 1),
                    bean,
                    timestampView
                )
                messageView.text = bean.messageContent
            }
        }
    }

    private fun updateTime(
        preMessage: MessageBean?,
        currentMessage: MessageBean,
        targetView: TextView
    ) {
        val pre = ((preMessage?.msgSendTime) ?: 0) / TIME_INTERVAL
        val cur = currentMessage.msgSendTime / TIME_INTERVAL
        if (preMessage == null || pre < cur) {
            targetView.visible()
            targetView.text =
                TimestampUtils.formatMessageDate(targetView.context, currentMessage.msgSendTime)
        } else {
            targetView.gone()
        }
    }

}