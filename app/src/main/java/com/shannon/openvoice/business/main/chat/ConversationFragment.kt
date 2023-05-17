package com.shannon.openvoice.business.main.chat

import android.text.SpannedString
import androidx.core.text.bold
import androidx.core.text.buildSpannedString
import androidx.core.text.color
import androidx.core.text.scale
import com.shannon.android.lib.base.activity.KBaseFragment
import com.shannon.android.lib.base.viewmodel.EmptyViewModel
import com.shannon.android.lib.extended.singleClick
import com.shannon.android.lib.extended.visibility
import com.shannon.android.lib.util.ThemeUtil
import com.shannon.openvoice.R
import com.shannon.openvoice.business.timeline.listener.RefreshableFragment
import com.shannon.openvoice.databinding.FragmentConversationBinding
import com.shannon.openvoice.util.TimestampUtils

/**
 *
 * @Package:        com.shannon.openvoice.business.main.chat
 * @ClassName:      ConversationFragment
 * @Description:     作用描述
 * @Author:         czhen
 * @CreateDate:     2023/2/22 10:49
 */
class ConversationFragment : KBaseFragment<FragmentConversationBinding, EmptyViewModel>(),
    RefreshableFragment, OnMessageCallback {

    private val mChatManager by lazy { ChatManager.instance }

    companion object {
        fun newInstance(): ConversationFragment {
            return ConversationFragment()
        }
    }

    override fun onInit() {
        mChatManager.registerMessageCallback(this)
        binding.run {
            displayNameView.text = buildDisplayName()
            conversationLayout.singleClick { startActivity(ChatActivity.newIntent(requireContext())) }
        }
        if (mChatManager.getAllMessage().isNotEmpty()) {
            onMessage(mChatManager.getAllMessage().last())
        }
    }

    override fun refreshContent() {
    }

    private fun buildDisplayName(): SpannedString {
        return buildSpannedString() {
            bold {
                color(ThemeUtil.getColor(requireContext(), android.R.attr.textColorPrimary)) {
                    append("VOVO")
                }
            }
            append(" ")
            color(ThemeUtil.getColor(requireContext(), android.R.attr.textColorSecondary)) {
                scale(0.9f) {
                    append(
                        requireContext().getString(
                            R.string.post_username_format, "vovo2023"
                        )
                    )
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        if (mChatManager.getAllMessage().isNotEmpty()) {
            updateTime(mChatManager.getAllMessage().last().msgSendTime)
            updateUnreadCount()
        }
    }

    override fun onMessage(messageBean: MessageBean) {
        binding.run {
            updateTime(messageBean.msgSendTime)
            messageContentView.text = messageBean.messageContent
            if (!messageBean.isOut) updateUnreadCount()
        }
    }

    private fun updateTime(msgSendTime: Long) {
        binding.timestampView.text = TimestampUtils.getRelativeTimeSpanString(
            requireContext(),
            msgSendTime,
            System.currentTimeMillis()
        )
    }

    private fun updateUnreadCount() {
        val count = mChatManager.statisticsUnreadCount()
        binding.badgeView.visibility(count > 0)
    }


    override fun onDestroy() {
        super.onDestroy()
        mChatManager.unregisterMessageCallback(this)
    }
}