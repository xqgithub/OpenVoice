package com.shannon.openvoice.business.timeline

import android.app.Activity
import android.view.View
import androidx.core.app.ActivityOptionsCompat
import androidx.core.view.ViewCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import com.shannon.android.lib.extended.showConfirmDialog
import com.shannon.android.lib.extended.showDialogConfirmWithIcon
import com.shannon.android.lib.util.ToastUtil
import com.shannon.openvoice.R
import com.shannon.openvoice.business.compose.ComposeActivity
import com.shannon.openvoice.business.creation.detail.ModelDetailActivity
import com.shannon.openvoice.business.main.mine.account.AccountActivity
import com.shannon.openvoice.business.main.mine.account.AccountManager
import com.shannon.openvoice.business.main.viewmedia.ViewMediaActivity
import com.shannon.openvoice.business.pay.PaymentActivity
import com.shannon.openvoice.business.player.PlayerHelper
import com.shannon.openvoice.business.player.listener.OnPlaylistListener
import com.shannon.openvoice.business.report.ReportActivity
import com.shannon.openvoice.business.status.StatusListActivity
import com.shannon.openvoice.business.timeline.detail.StatusDetailActivity
import com.shannon.openvoice.business.timeline.listener.AdapterLogicListener
import com.shannon.openvoice.business.timeline.listener.FragmentListener
import com.shannon.openvoice.business.timeline.listener.StatusActionListener
import com.shannon.openvoice.business.timeline.menu.StatusMenuItem
import com.shannon.openvoice.dialog.ActionMenuDialog
import com.shannon.openvoice.extended.startActivityIntercept
import com.shannon.openvoice.model.*
import com.shannon.openvoice.util.looksLikeMastodonUrl
import com.shannon.openvoice.util.openLink
import org.greenrobot.eventbus.EventBus
import timber.log.Timber
import java.lang.ref.WeakReference

/**
 *
 * @Package:        com.shannon.openvoice.business.timeline
 * @ClassName:      StatusActionImpl
 * @Description:     作用描述
 * @Author:         czhen
 * @CreateDate:     2022/8/18 9:30
 */
class StatusActionImpl : StatusActionListener, DefaultLifecycleObserver {

    private lateinit var timelineKind: TimelineViewModel.Kind
    private lateinit var fragmentListener: FragmentListener
    private var playlistListener: OnPlaylistListener? = null
    private lateinit var viewModel: TimelineViewModel
    private lateinit var adapter: AdapterLogicListener
    private var lifecycleOwnerWeak: WeakReference<LifecycleOwner>? = null

    override fun bindHost(
        timelineKind: TimelineViewModel.Kind,
        fragmentListener: FragmentListener,
        viewModel: TimelineViewModel,
        adapter: AdapterLogicListener
    ) {
        this.timelineKind = timelineKind
        this.fragmentListener = fragmentListener
        this.playlistListener = fragmentListener as OnPlaylistListener
        this.viewModel = viewModel
        this.adapter = adapter
        PlayerHelper.instance.bindPlaylistCallback(
            fragmentListener.getTimelinePageId(),
            playlistListener!!
        )
        if (fragmentListener is Fragment) {
            Timber.d("bindHost is Fragment")
            lifecycleOwnerWeak = WeakReference(fragmentListener.viewLifecycleOwner)
            fragmentListener.viewLifecycleOwner.lifecycle.addObserver(this)
        }
    }

    private fun requireContext() = fragmentListener.requireAct()

    override fun openReblog(position: Int) {

    }

    override fun onViewMedia(position: Int, attachmentIndex: Int, view: View?) {
        val status = adapter.getLogicData()[position].actionableStatus
        when (status.attachments[attachmentIndex].type) {
            Attachment.Type.GIFV, Attachment.Type.VIDEO, Attachment.Type.IMAGE, Attachment.Type.AUDIO -> {
                val attachments = AttachmentViewData.list(status)
                val intent =
                    ViewMediaActivity.newIntent(requireContext(), attachments, attachmentIndex)
                if (view != null) {
                    val url = status.attachments[attachmentIndex].url
                    ViewCompat.setTransitionName(view, url)
                    val options = ActivityOptionsCompat.makeSceneTransitionAnimation(
                        requireContext() as Activity,
                        view,
                        url
                    )
                    requireContext().startActivity(intent, options.toBundle())
                } else {
                    requireContext().startActivity(intent)
                }
            }
            Attachment.Type.UNKNOWN -> {
            }
        }
    }

    override fun onReply(position: Int) {
        requireContext().startActivityIntercept {
            val status = adapter.getLogicData()[position]
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

            requireContext().startActivity(
                ComposeActivity.newIntentWith(
                    requireContext(), ComposeOptions(
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

    override fun onReblog(reblog: Boolean, position: Int) {
        requireContext().startActivityIntercept {
            val statusId = adapter.getDataActionableId(position)
            viewModel.reblog(reblog, statusId) {
                adapter.onReblog(it, position)
                it?.run {
                    EventBus.getDefault()
                        .post(
                            EventComposeStatus(
                                timelineKind,
                                EventComposeStatus.Type.REBLOG,
                                this
                            )
                        )
                }
            }
        }
    }

    override fun onFavourite(favourite: Boolean, position: Int) {
        requireContext().startActivityIntercept {
            val statusId = adapter.getDataActionableId(position)
            viewModel.favourite(favourite, statusId) {
                adapter.onFavourite(it)
                it?.run {
                    EventBus.getDefault()
                        .post(
                            EventComposeStatus(
                                timelineKind,
                                EventComposeStatus.Type.FAVOURITES,
                                this
                            )
                        )
                }
            }
        }
    }

    override fun onPurchaseVoice(position: Int) {
        requireContext().startActivityIntercept {
            val status = adapter.getLogicData()[position].actionableStatus
            status.voiceModel?.run {
                viewModel.getStatus(status.id) {
                    val workVoiceModel = it.actionableStatus.voiceModel
//                if (workVoiceModel?.isModelOwner == true && workVoiceModel.payload != null) {
//                    ToastUtil.showCenter(requireContext().getString(R.string.tips_owned_model))
//                } else
                    if (workVoiceModel?.status == "deleted") {
                        ToastUtil.showCenter(requireContext().getString(R.string.tips_deleted_model))
                        EventBus.getDefault()
                            .post(
                                EventComposeStatus(
                                    TimelineViewModel.Kind.ANY,
                                    EventComposeStatus.Type.VOICE_MODEL,
                                    it
                                )
                            )
                    } else {// if (workVoiceModel?.isModelOwner == false)
                        requireContext().startActivity(
                            PaymentActivity.newIntent(
                                requireContext(),
                                id
                            )
                        )
                    }
                }
            }
        }
    }

    override fun onMore(view: View, position: Int) {
        requireContext().startActivityIntercept {
            val status = adapter.getLogicData()[position]
            val itemList = arrayListOf<StatusMenuItem>()
            if (AccountManager.accountManager.getAccountId() == status.actionableStatus.account.id) {
                if (status.pinAllowed) {
                    itemList.add(StatusMenuItem.pin(if (status.actionableStatus.isPinned) R.string.cancel_topping else R.string.top))
                }
                itemList.add(StatusMenuItem.delete())
                itemList.add(StatusMenuItem.republish())
                showMenuDialog(itemList, status)
            } else {
                itemList.add(StatusMenuItem.report())
                itemList.add(StatusMenuItem.blackList())
                viewModel.following(status.actionableStatus.account.id, {
                    showMenuDialog(itemList, status)
                }, {
                    itemList.add(
                        0,
                        StatusMenuItem.follow(
                            if (it) R.string.following else R.string.follow,
                            it
                        )
                    )
                    showMenuDialog(itemList, status)
                })
            }
        }
    }

    private fun showMenuDialog(itemList: List<StatusMenuItem>, status: StatusModel) {
        ActionMenuDialog(requireContext(), itemList) { item ->
            when (item.type) {
                StatusMenuItem.Type.DELETE -> {
                    requireContext().showConfirmDialog(message = requireContext().getString(R.string.sure_delete_voiceover)) {
                        viewModel.deleteStatus(status.actionableId) {
                            adapter.removeItem(status)
                            EventBus.getDefault()
                                .post(
                                    EventComposeStatus(
                                        timelineKind,
                                        EventComposeStatus.Type.DELETE,
                                        status
                                    )
                                )
                            PlayerHelper.instance.removeMediaItem(status.id)
                            status.reblog?.run {
                                PlayerHelper.instance.removeMediaItem(status.actionableId)
                            }
                            fragmentListener.deleted()
                        }
                    }
                }
                StatusMenuItem.Type.REPUBLISH -> {
                    requireContext().showConfirmDialog(message = requireContext().getString(R.string.delete_edit_voiceover)) {
                        viewModel.deleteStatus(status.actionableId) {
                            adapter.removeItem(status)
                            EventBus.getDefault()
                                .post(
                                    EventComposeStatus(
                                        timelineKind,
                                        EventComposeStatus.Type.DELETE,
                                        status
                                    )
                                )
                            PlayerHelper.instance.removeMediaItem(status.id)
                            status.reblog?.run {
                                PlayerHelper.instance.removeMediaItem(status.actionableId)
                            }
                            val inReplyToId = status.actionableStatus.inReplyToId
                            val voiceModelId = status.actionableStatus.voiceModel?.id ?: 0L
                            val visibility = status.actionableStatus.visibility
                            requireContext().startActivity(
                                ComposeActivity.newIntentWith(
                                    requireContext(), ComposeOptions(
                                        content = status.actionableStatus.getEditableText(),
                                        inReplyToId = inReplyToId,
                                        visibility = visibility,
                                        voiceModelId = voiceModelId,
                                        mediaAttachments = status.actionableStatus.attachments,
                                        modifiedInitialState = true
                                    )
                                )
                            )
                            fragmentListener.deleted()
                        }
                    }
                }
                StatusMenuItem.Type.PIN -> {
                    viewModel.pin(
                        !status.actionableStatus.isPinned,
                        status.actionableId
                    ) { result ->
                        result?.run {
                            adapter.onPin(this)
                            EventBus.getDefault()
                                .post(
                                    EventComposeStatus(
                                        timelineKind,
                                        EventComposeStatus.Type.PIN,
                                        status.copy(pinned = pinned)
                                    )
                                )
                        }
                    }
                }
                StatusMenuItem.Type.FOLLOW -> {
                    val follow = item.info as Boolean
                    if (follow) {
                        requireContext().showDialogConfirmWithIcon(
                            message = requireContext().getString(R.string.desc_sure_unfollow),
                            drawableRes = R.drawable.icon_dialog_mark,
                            doConfirm = {
                                viewModel.unfollowAccount(status.actionableStatus.account.id) {
                                    EventBus.getDefault()
                                        .post(
                                            EventFollow(status.actionableStatus.account.id, false)
                                        )
                                }
                            }
                        )
                    } else {
                        viewModel.followAccount(status.actionableStatus.account.id) {
                            EventBus.getDefault()
                                .post(
                                    EventFollow(status.actionableStatus.account.id, true)
                                )
                        }
                    }
                }
                StatusMenuItem.Type.BLACK_LIST -> {
                    val account = status.actionableStatus.account
                    requireContext().showConfirmDialog(
                        message = requireContext().getString(
                            R.string.remind_black_list,
                            account.username
                        )
                    ) {
                        viewModel.blockAccount(account.id) {
                            ToastUtil.showCenter(requireContext().getString(R.string.tips_succ_blocked))
                            EventBus.getDefault().post(EventBlackList(account.id))
                        }
                    }
                }
                StatusMenuItem.Type.REPORT -> {
                    requireContext().startActivity(
                        ReportActivity.newIntent(requireContext(), status.actionableStatus)
                    )
                }
            }
        }.show()
    }


    override fun onShowDetail(position: Int) {
        if (timelineKind != TimelineViewModel.Kind.DETAIL) {
            val status = adapter.getLogicData()[position]
            requireContext().startActivity(
                StatusDetailActivity.newIntent(
                    requireContext(),
                    status.copy(isPlaying = false)
                )
            )
        }
    }

    override fun onShowModelDetail(position: Int) {
        if (timelineKind != TimelineViewModel.Kind.MODEL_ASS) {
            val status = adapter.getLogicData()[position]
            status.voiceModel?.let {
                requireContext().startActivity(
                    ModelDetailActivity.newIntent(
                        requireContext(),
                        it.id
                    )
                )
            }
        }
    }

    override fun onPlay(position: Int) {
        val mediaList = PlayerHelper.convert2MediaInfo(adapter.getLogicData())
        if (mediaList.isNotEmpty()) {
            if (!PlayerHelper.instance.isCurrentPlaylist(getTimelinePageId())) {
                if (playlistListener != null)
                    PlayerHelper.instance.setPlaylist(
                        mediaList,
                        getTimelinePageId(),
                        playlistListener!!
                    )
            }
            PlayerHelper.instance.onPlay(adapter.getDataId(position))
        } else {
            ToastUtil.showCenter(requireContext().getString(R.string.tips_no_content))
        }
    }

    override fun pausePlayer() {
        PlayerHelper.instance.onPause()
    }

    override fun onViewTag(tag: String) {
        if (timelineKind == TimelineViewModel.Kind.TAG
            && viewModel.hashtags.size == 1
            && viewModel.hashtags.contains(tag)
        ) {
            return
        }
        requireContext().startActivity(
            StatusListActivity.newHashtagIntent(
                requireContext(),
                tag
            )
        )
    }

//    override fun addMediaItem(mediaId: String, ossId: String) {
//        PlayerHelper.instance.addMediaSource(
//            PlayerHelper.convert2MediaInfo(arrayListOf(item)),
//            getTimelinePageId(),
//            false
//        )
//    }

    override fun addMediaItem(item: StatusModel) {
        PlayerHelper.instance.addMediaSource(
            PlayerHelper.convert2MediaInfo(arrayListOf(item)),
            getTimelinePageId(),
            false
        )
    }

    override fun onViewAccount(id: String) {
        if (timelineKind == TimelineViewModel.Kind.USER ||
            timelineKind == TimelineViewModel.Kind.USER_PINNED ||
            timelineKind == TimelineViewModel.Kind.USER_WITH_REPLIES
            || id.isEmpty() ) {
            return
        }
        requireContext().startActivity(AccountActivity.newIntent(requireContext(), id))
    }

    override fun onViewUrl(url: String) {
        if (!looksLikeMastodonUrl(url)) {
            requireContext().openLink(url)
        }
    }

    override fun getTimelineKind(): TimelineViewModel.Kind {
        return timelineKind
    }

    override fun onDestroy(owner: LifecycleOwner) {
        super.onDestroy(owner)
        PlayerHelper.instance.unbindPlaylistCallback(getTimelinePageId())
        val lifecycleOwner = lifecycleOwnerWeak?.get()
        lifecycleOwner?.lifecycle?.removeObserver(this)
    }

    private fun getTimelinePageId() = fragmentListener.getTimelinePageId()

}