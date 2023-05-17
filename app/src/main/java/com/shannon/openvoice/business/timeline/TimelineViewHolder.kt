package com.shannon.openvoice.business.timeline

import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.text.Spanned
import android.text.TextUtils
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.DrawableRes
import androidx.recyclerview.widget.GridLayoutManager
import com.bumptech.glide.Glide
import com.shannon.android.lib.base.adapter.BaseFunBindingViewHolder
import com.shannon.android.lib.extended.*
import com.shannon.android.lib.util.ThemeUtil
import com.shannon.openvoice.R
import com.shannon.openvoice.business.main.mine.account.AccountManager
import com.shannon.openvoice.business.timeline.listener.StatusActionListener
import com.shannon.openvoice.components.MediaPreviewImageView
import com.shannon.openvoice.databinding.ItemStatusBinding
import com.shannon.openvoice.extended.loadAvatar
import com.shannon.openvoice.model.Attachment
import com.shannon.openvoice.model.HashTag
import com.shannon.openvoice.model.StatusModel
import com.shannon.openvoice.util.*
import me.jingbin.library.adapter.BaseByViewHolder
import me.jingbin.library.decoration.GridSpaceItemDecoration
import timber.log.Timber
import java.util.*

/**
 *
 * @ProjectName:    OpenVoice
 * @Package:        com.shannon.openvoice.business.timeline
 * @ClassName:      TimelineViewHolder
 * @Description:     java类作用描述
 * @Author:         czhen
 * @CreateDate:     2022/8/2 11:37
 */
class TimelineViewHolder(
    val binding: ItemStatusBinding,
    private val actionListener: StatusActionListener
) :
    BaseFunBindingViewHolder<StatusModel, ItemStatusBinding>(
        binding
    ) {

    private val accountId = AccountManager.accountManager.getAccountId()

    private val mContext = binding.root.context

    private val mediaAdapter: MediaAdapter
    private val mediaItemDecoration: GridSpaceItemDecoration

    init {
        binding.run {
            mediaAdapter = MediaAdapter(mContext, actionListener)
            mediaItemDecoration = GridSpaceItemDecoration(8.dp, false).setNoShowSpace(0, 0)
            mediaListView.addItemDecoration(mediaItemDecoration)
        }
    }

    override fun onBindView(binding: ItemStatusBinding, bean: StatusModel, position: Int) {
    }

    override fun onBaseBindView(
        holder: BaseByViewHolder<StatusModel>?,
        bean: StatusModel,
        position: Int
    ) {
        holderBindView(bean, position)
    }

    private fun holderBindView(bean: StatusModel, position: Int) {
        val actionableStatus = bean.actionableStatus
        binding.run {
            setReblogInfo(bean)
            binding.statusAvatar.loadAvatar(bean.account.avatar)
            setNameInfo(bean)
            setTimestampInfo(bean.createdAt)
            setPlayStatus(
                bean.isPlaying,
                actionableStatus.playingButtonDisappears, actionableStatus.ttsGenerated
            )
            setVoiceModelInfo(actionableStatus.voiceModel)
            val attachments = actionableStatus.attachments
            if (hasPreviewAttachment(attachments)) {
                binding.statusMediaPreviewContainer.visible()
                setMediaPreviews(attachments)
            } else {
                binding.statusMediaPreviewContainer.gone()
            }
            setStatusContent(
                actionableStatus.content.parseAsAppHtml(),
                actionableStatus.mentions,
                actionableStatus.tags
            )
            setIsReply(actionableStatus.inReplyToId != null)
            setReblogged(actionableStatus.reblogged)
            setFavourited(actionableStatus.favourited)
            setupButton(bean.account.id)
            setActionCount(actionableStatus)
            setReblogEnabled(actionableStatus.reblogAllowed, actionableStatus.visibility)
            Timber.d("Kind = ${actionListener.getTimelineKind()} ; featured = ${bean.featured}")
            val featured =
                actionListener.getTimelineKind() == TimelineViewModel.Kind.PUBLIC_LOCAL && bean.featured
            binding.topHolderView.visibility(featured)
        }
    }

    private val ellipsisFilter by lazy { EllipsisFilter(8) }
    private fun setReblogInfo(bean: StatusModel) {
        val reblogStatus = bean.reblogStatus
        if (reblogStatus != null) {
            binding.reblogLayout.visible()
            binding.statusReblogUser.text =
                mContext.getString(R.string.sign_reposted, reblogStatus.account.username)
//            binding.reblogLayout.singleClick { actionListener.onViewAccount(reblogStatus.account.id) }
        } else {
            binding.reblogLayout.gone()
        }
    }


    private fun setNameInfo(statusModel: StatusModel) {
        binding.statusDisplayName.text = statusModel.account.name
        binding.statusUsername.text =
            mContext.getString(R.string.post_username_format, statusModel.account.username)
    }

    private fun setTimestampInfo(createAt: Date) {
        val then = createAt.time
        val now = System.currentTimeMillis()
        binding.statusTimestampInfo.text =
            TimestampUtils.getRelativeTimeSpanString(mContext, then, now)
    }

    private fun setPlayStatus(
        isPlaying: Boolean,
        playingButtonDisappears: Boolean,
        ttsGenerated: Boolean
    ) {
        if (playingButtonDisappears) {
            binding.statusPlay.invisible()
            return
        }

        if (isPlaying) {
            binding.playingAnimationView.visible()
            Glide.with(mContext).load(R.raw.animation_player).into(binding.playingAnimationView)
            binding.statusTradeLayout.invisible()
            binding.statusPlay.invisible()
            binding.statusPause.visible()
        } else {
            binding.playingAnimationView.invisible()
            binding.statusPlay.isEnabled = ttsGenerated
            binding.statusPlay.setImageResource(if (ttsGenerated) R.drawable.icon_play else R.drawable.icon_tts_generating)
            binding.statusPlay.visible()
            binding.statusTradeLayout.visible()
            binding.statusPause.gone()
        }
        Timber.e("ttsGenerated = $ttsGenerated")
    }

    private fun setVoiceModelInfo(model: StatusModel.VoiceModel?) {
        model?.run {
            binding.modelNameView.text = model.name
            binding.deleteTagView.text = "( ".plus(mContext.getString(R.string.deleted)).plus(" )")
            binding.deleteTagView.visibility(model.status == "deleted")
        }
    }

    private fun setStatusContent(
        content: Spanned,
        mentions: List<StatusModel.Mention>?,
        tags: List<HashTag>?
    ) {
        setClickableText(binding.statusContent, content, mentions, tags, actionListener)
        if (TextUtils.isEmpty(binding.statusContent.text)) {
            binding.statusContent.gone()
        } else {
            binding.statusContent.visible()
        }
    }

    protected fun hasPreviewAttachment(attachments: List<Attachment>): Boolean {
        for (attachment in attachments) {
            if (attachment.type === Attachment.Type.AUDIO || attachment.type === Attachment.Type.UNKNOWN) {
                return false
            }
        }
        return true
    }


    private fun setMediaPreviews(attachments: List<Attachment>) {
        val attachmentSize = Math.min(attachments.size, StatusModel.MAX_MEDIA_ATTACHMENTS)
        mediaAdapter.setNewData(attachments)
        binding.mediaListView.adapter = mediaAdapter
        mediaAdapter.setHolderPosition(holderPosition)
        binding.mediaListView.layoutManager =
            GridLayoutManager(mContext, if (attachmentSize % 2 == 0) 2 else attachmentSize)
    }

    @DrawableRes
    private fun getLabelIcon(type: Attachment.Type): Int {
        return when (type) {
            Attachment.Type.IMAGE -> R.drawable.icon_photo_24dp
            Attachment.Type.GIFV, Attachment.Type.VIDEO -> R.drawable.icon_video_24dp
            Attachment.Type.AUDIO -> R.drawable.icon_music_box_24dp
            else -> R.drawable.icon_attach_file_24dp
        }
    }

    private fun setIsReply(isReply: Boolean) {
        if (isReply) {
            binding.statusReply.setImageResource(R.drawable.icon_reply)
        } else {
            binding.statusReply.setImageResource(R.drawable.icon_reply)
        }
    }


    private fun setReblogged(reblogged: Boolean) {
        binding.statusInset.isChecked = reblogged
    }

    private fun setFavourited(favourited: Boolean) {
        binding.statusFavourite.isChecked = favourited
    }


    private fun setupButton(accountId: String) {
        binding.statusAvatar.singleClick { actionListener.onViewAccount(accountId) }
        binding.statusDisplayName.singleClick { actionListener.onViewAccount(accountId) }
        binding.statusUsername.singleClick { actionListener.onViewAccount(accountId) }
        binding.statusReply.singleClick { actionListener.onReply(holderPosition) }
        binding.statusInset.setEventListener { _, buttonState ->
            actionListener.onReblog(!buttonState, holderPosition)
            return@setEventListener AccountManager.accountManager.isLogin()
        }
        binding.statusFavourite.setEventListener { _, buttonState ->
            actionListener.onFavourite(!buttonState, holderPosition)
            return@setEventListener AccountManager.accountManager.isLogin()
        }
        binding.statusPlay.singleClick { actionListener.onPlay(holderPosition) }
        binding.statusPause.singleClick { actionListener.pausePlayer() }
        binding.statusMore.singleClick { actionListener.onMore(it, holderPosition) }
        binding.root.singleClick { actionListener.onShowDetail(holderPosition) }
        binding.statusContent.singleClick { actionListener.onShowDetail(holderPosition) }
        binding.statusTradeLayout.singleClick { actionListener.onShowModelDetail(holderPosition) }
    }

    private fun setActionCount(statusModel: StatusModel) {
        binding.replayCountView.text = UnitHelper.quantityConvert(statusModel.repliesCount)
        binding.reblogCountView.text = UnitHelper.quantityConvert(statusModel.reblogCount)
        binding.favouriteCountView.text = UnitHelper.quantityConvert(statusModel.favouriteCount)
    }

    private fun setReblogEnabled(enabled: Boolean, visibility: StatusModel.Visibility) {
        binding.statusInset.isEnabled = enabled && visibility != StatusModel.Visibility.PRIVATE
        if (binding.statusInset.isEnabled) {
            binding.statusInset.setInactiveImage(R.drawable.icon_reblog_normal)
            binding.statusInset.setActiveImage(R.drawable.icon_reblog_active)
        } else {
            val resDrawable =
                if (visibility == StatusModel.Visibility.DIRECT) R.drawable.icon_reblog_direct else R.drawable.icon_reblog_private_normal
            binding.statusInset.setInactiveImage(resDrawable)
            binding.statusInset.setActiveImage(resDrawable)
        }
    }

    private val holderPosition: Int
        get() {
            return if (layoutPosition >= byRecyclerView.customTopItemViewCount) {
                layoutPosition - byRecyclerView.customTopItemViewCount
            } else {
                layoutPosition
            }.also {
                Timber.e("holderPosition = $it")
            }
        }

}