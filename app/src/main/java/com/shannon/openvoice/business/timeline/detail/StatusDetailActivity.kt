package com.shannon.openvoice.business.timeline.detail

import android.content.Context
import android.content.Intent
import androidx.fragment.app.commit
import com.shannon.android.lib.base.activity.KBaseActivity
import com.shannon.android.lib.extended.gone
import com.shannon.android.lib.extended.showDialogConfirmWithIcon
import com.shannon.android.lib.extended.singleClick
import com.shannon.android.lib.extended.visible
import com.shannon.openvoice.R
import com.shannon.openvoice.business.compose.ComposeActivity
import com.shannon.openvoice.business.main.mine.account.AccountManager
import com.shannon.openvoice.business.timeline.TimelineViewModel
import com.shannon.openvoice.databinding.ActivityStatusDetailBinding
import com.shannon.openvoice.event.UniversalEvent
import com.shannon.openvoice.extended.startActivityIntercept
import com.shannon.openvoice.model.ComposeOptions
import com.shannon.openvoice.model.EventFollow
import com.shannon.openvoice.model.StatusModel
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode

/**
 *
 * @Package:        com.shannon.openvoice.business.timeline.detail
 * @ClassName:      StatusDetailActivity
 * @Description:     作用描述
 * @Author:         czhen
 * @CreateDate:     2022/8/18 9:26
 */
class StatusDetailActivity : KBaseActivity<ActivityStatusDetailBinding, TimelineViewModel>() {

    private lateinit var status: StatusModel
    private var following = false
    override fun onInit() {
        setTitleText(R.string.voiceover_details)
        EventBus.getDefault().register(this)
        status = intent.getParcelableExtra<StatusModel>(EXTRA_MODEL)!!
        if (AccountManager.accountManager.isLogin()) {
            updateFollowing()
        } else if (!AccountManager.accountManager.isLogin()) {
            binding.followButton.visible()
        }

        supportFragmentManager.commit {
            replace(R.id.containerView, StatusDetailFragment.newIntent(status))
        }
        binding.followButton.singleClick {
            startActivityIntercept {
                if (following) {
                    showDialogConfirmWithIcon(
                        message = getString(R.string.desc_sure_unfollow),
                        drawableRes = R.drawable.icon_dialog_mark,
                        doConfirm = {
                            viewModel.unfollowAccount(status.actionableStatus.account.id) {
                                setFollowButtonText(false)
                            }
                        }
                    )
                } else {
                    viewModel.followAccount(status.actionableStatus.account.id) {
                        setFollowButtonText(true)
                    }
                }
            }
        }
        binding.addCommentView.singleClick {
            onReply()
        }
    }

    private fun updateFollowing() {
        if (AccountManager.accountManager.getAccountId() == status.actionableStatus.account.id) {
            binding.followButton.gone()
            return
        }
        viewModel.following(status.actionableStatus.account.id) {
            setFollowButtonText(it)
            binding.followButton.visible()
        }
    }

    private fun setFollowButtonText(follow: Boolean) {
        following = follow
        binding.followButton.isSelected = follow
        binding.followButton.text =
            getString(if (following) R.string.following else R.string.follow)
    }

    fun onReply() {
        startActivityIntercept {
            val inReplyToId = status.actionableId
            val replyingStatusAuthor = status.actionableStatus.account.localUsername
            val voiceModelId = status.actionableStatus.voiceModel?.id ?: 0L
            val visibility = status.actionableStatus.visibility
            val mentions = status.actionableStatus.mentions
            val mentionedUser = linkedSetOf<String>()
            mentionedUser.add(status.actionableStatus.account.username)
            mentions.forEach {
                mentionedUser.add(it.username)
            }
            mentionedUser.remove(AccountManager.accountManager.getAccountData().name)//删除当前登录的用户

            startActivity(
                ComposeActivity.newIntentWith(
                    context, ComposeOptions(
                        inReplyToId = inReplyToId,
                        replyingStatusAuthor = replyingStatusAuthor,
                        visibility = visibility,
                        mentionedUsernames = mentionedUser,
                        voiceModelId = voiceModelId.toLong()
                    )
                )
            )
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun subscribeStatus(event: EventFollow) {
        if (event.accountId == status.actionableStatus.account.id) {
            setFollowButtonText(event.following)
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun eventBusLogin(event: UniversalEvent) {
        if (event.actionType == UniversalEvent.loginSuccess) {
            updateFollowing()
        }
    }

    companion object {
        private const val EXTRA_MODEL = "model"
        fun newIntent(context: Context, model: StatusModel) =
            Intent(context, StatusDetailActivity::class.java).apply {
                putExtra(EXTRA_MODEL, model)
            }
    }
}