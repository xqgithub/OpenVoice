package com.shannon.openvoice.business.main.chat

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.text.Editable
import android.view.MotionEvent
import android.view.View
import android.view.ViewTreeObserver
import android.view.WindowManager
import androidx.core.widget.doAfterTextChanged
import androidx.recyclerview.widget.LinearLayoutManager
import com.blankj.utilcode.util.KeyboardUtils
import com.gyf.immersionbar.ImmersionBar
import com.shannon.android.lib.base.activity.KBaseActivity
import com.shannon.android.lib.base.viewmodel.EmptyViewModel
import com.shannon.android.lib.extended.gone
import com.shannon.android.lib.extended.singleClick
import com.shannon.android.lib.extended.visible
import com.shannon.android.lib.util.ThemeUtil
import com.shannon.android.lib.util.ToastUtil
import com.shannon.openvoice.R
import com.shannon.openvoice.databinding.ActivityChatBinding
import timber.log.Timber

/**
 *
 * @Package:        com.shannon.openvoice.business.main.chat
 * @ClassName:      ChatActivity
 * @Description:     作用描述
 * @Author:         czhen
 * @CreateDate:     2023/2/22 14:59
 */
class ChatActivity : KBaseActivity<ActivityChatBinding, EmptyViewModel>(), OnMessageCallback,
    ViewTreeObserver.OnGlobalLayoutListener {
    private val keyboardMode =
        WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN or WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE

    private val mChatManager by lazy { ChatManager.instance }

    private val mChatLayoutManager by lazy { LinearLayoutManager(context) }
    private val mChatAdapter by lazy { ChatAdapter() }
    @SuppressLint("ClickableViewAccessibility")
    override fun onInit() {
        setNavigationBarColor(ThemeUtil.getColor(this, R.attr.navigationBarBackground))
        ImmersionBar.with(this)
            .keyboardEnable(true, keyboardMode)
            .init()
        mChatManager.registerMessageCallback(this)
        mChatManager.clearUnreadFlag()
        binding.run {
            sendButton.singleClick(this@ChatActivity::sendMessage)
            contentView.doAfterTextChanged {
                val isEnabled = ((it?.length) ?: 0) > 0
                sendButton.isEnabled = isEnabled
            }

            chatListView.layoutManager = mChatLayoutManager
            chatListView.adapter = mChatAdapter
            val messageList = mChatManager.getAllMessage()
            if (messageList.isNotEmpty()) {
                mChatAdapter.setNewData(messageList.toMutableList())
                chatListView.visible()
                tipsLayout.gone()
                scrollToPositionBottom(mChatAdapter.itemCount)
            }
            chatListView.viewTreeObserver.addOnGlobalLayoutListener(this@ChatActivity)
            chatListView.setOnTouchListener { _, event ->
                if (event.action == MotionEvent.ACTION_DOWN || event.action == MotionEvent.ACTION_MOVE) {
                    if (KeyboardUtils.isSoftInputVisible(this@ChatActivity)) {
                        KeyboardUtils.hideSoftInput(this@ChatActivity)
                    }
                }
                return@setOnTouchListener false
            }
        }
    }

    private fun sendMessage(view: View) {
        binding.run {
            if (mChatManager.isMinInterval()) {
                ToastUtil.showToast(getString(R.string.tips_send_frequently))
                return@run
            }
            if (mChatManager.isMaxNonReplies()) {
                ToastUtil.showToast(getString(R.string.tips_send_frequently))
                return@run
            }
            chatListView.visible()
            tipsLayout.gone()
            mChatManager.send(contentView.text.toString()) {
                ToastUtil.showToast(getString(R.string.unable_reply))
            }
            contentView.text = Editable.Factory().newEditable("")
        }
    }


    override fun onMessage(messageBean: MessageBean) {
        Timber.d(messageBean.messageContent)
        mChatAdapter.addData(messageBean)
        mChatManager.updateReadStatusById(messageBean)
        scrollToPositionBottom(mChatAdapter.itemCount)
    }

    override fun onInputStatus(isEntering: Boolean) {
        if (isEntering) {
            setTitleText(getString(R.string.vovo).plus(" ").plus(getString(R.string.send_typing)))
        } else {
            setTitleText(R.string.vovo)
        }
    }

    private fun scrollToPositionBottom(lastSize: Int) {
        if (lastSize < 1) return
        if (mChatLayoutManager.findViewByPosition(lastSize - 1) != null) {
            mChatLayoutManager.scrollToPositionWithOffset(
                lastSize - 1,
                -mChatLayoutManager.findViewByPosition(lastSize - 1)!!.bottom
            )
        } else {
            binding.chatListView.scrollToPosition(lastSize - 1)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        mChatManager.unregisterMessageCallback(this)
        binding.chatListView.viewTreeObserver.removeOnGlobalLayoutListener(this)
    }

    companion object {

        fun newIntent(context: Context) = Intent(context, ChatActivity::class.java)
    }

    private var chatListHeight = 0
    override fun onGlobalLayout() {
        binding.chatListView.run {
            if (chatListHeight == height) return@run
            chatListHeight = height
            //当键盘推起的时候 将列表滚动到底部
            if (chatListHeight in 1..999) {
                scrollToPositionBottom(mChatAdapter.itemCount)
            }
        }
    }
}