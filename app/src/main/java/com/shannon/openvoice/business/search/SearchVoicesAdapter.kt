package com.shannon.openvoice.business.search

import android.content.Context
import android.graphics.Color
import android.text.Spanned
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.ViewGroup
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import com.bumptech.glide.Glide
import com.shannon.android.lib.base.adapter.BaseFunBindingRecyclerViewAdapter
import com.shannon.android.lib.base.adapter.BaseFunBindingViewHolder
import com.shannon.android.lib.extended.*
import com.shannon.android.lib.util.ThemeUtil
import com.shannon.openvoice.FunApplication
import com.shannon.openvoice.R
import com.shannon.openvoice.business.main.mine.account.AccountManager
import com.shannon.openvoice.business.timeline.listener.AdapterLogicListener
import com.shannon.openvoice.business.timeline.listener.StatusActionListener
import com.shannon.openvoice.components.TextAndTextUi
import com.shannon.openvoice.databinding.AdapterSearchVoicesBinding
import com.shannon.openvoice.databinding.ItemStatusBinding
import com.shannon.openvoice.extended.loadAvatar
import com.shannon.openvoice.model.AccountBean
import com.shannon.openvoice.model.Attachment
import com.shannon.openvoice.model.HashTag
import com.shannon.openvoice.model.StatusModel
import com.shannon.openvoice.util.TimestampUtils
import com.shannon.openvoice.util.UnitHelper
import com.shannon.openvoice.util.setClickableText
import me.jingbin.library.adapter.BaseByViewHolder
import timber.log.Timber
import java.util.*

/**
 * Date:2023/3/3
 * Time:15:17
 * author:dimple
 * 搜索声文页面适配器
 */
class SearchVoicesAdapter(
    private val mContext: Context,
    private val actionListener: StatusActionListener
) : BaseFunBindingRecyclerViewAdapter<StatusModel, AdapterSearchVoicesBinding>(),
    AdapterLogicListener {


    //转发点击事件
    private lateinit var toReblogListener: () -> Unit

    private lateinit var mBinding: AdapterSearchVoicesBinding

    override fun bindView(
        holder: BaseByViewHolder<StatusModel>,
        binding: AdapterSearchVoicesBinding,
        bean: StatusModel,
        position: Int
    ) {
        binding.apply {
            mBinding = this

            sivAvatar.loadAvatar(bean.account.avatar)
            ivPopularLogo.visibility(isRecommended)
            tvUserNameNickName.text = bean.account.name
            tvForwarderUsername.text = contentHighlight(
                content = "${
                    mContext.getString(
                        R.string.post_username_format,
                        bean.account.username
                    )
                }  ${getTimestampInfo(bean.createdAt)}",
                arrayOf(getTimestampInfo(bean.createdAt)),
                arrayOf(Color.parseColor("#40A4ACAC"))
            )

            if (bean.voiceModel?.status == "deleted") {
                val _modelName =
                    "${mContext.getString(R.string.model_name)}${bean.voiceModel?.name}"
                val deleteTagView = "( ".plus(mContext.getString(R.string.deleted)).plus(" )")
                tvModelName.text = contentHighlight(
                    "$_modelName  $deleteTagView",
                    arrayOf(deleteTagView),
                    arrayOf(
                        ContextCompat.getColor(
                            mContext,
                            R.color.color_EC5143
                        )
                    )
                )
            } else {
                tvModelName.text =
                    "${mContext.getString(R.string.model_name)}${bean.voiceModel?.name}"
            }

            setStatusContent(
                bean.actionableStatus.content.parseAsAppHtml(),
                bean.actionableStatus.mentions,
                bean.actionableStatus.tags,
                this,
                position
            )

            setMediaPreviews(bean.attachments, this, position)

            replayCountView.text = UnitHelper.quantityConvert(bean.repliesCount)
            reblogCountView.text = UnitHelper.quantityConvert(bean.reblogCount)
            favouriteCountView.text = UnitHelper.quantityConvert(bean.favouriteCount)

            statusReply.singleClick { actionListener.onReply(position) }

            setReblogEnabled(
                bean.actionableStatus.reblogAllowed,
                bean.actionableStatus.reblogged,
                bean.actionableStatus.visibility,
                this
            )
            binding.statusInset.setEventListener { _, buttonState ->
                actionListener.onReblog(!buttonState, position)
                return@setEventListener true
            }

            setFavourited(bean.actionableStatus.favourited, this)
            binding.statusFavourite.setEventListener { _, buttonState ->
                actionListener.onFavourite(!buttonState, position)
                return@setEventListener AccountManager.accountManager.isLogin()
            }

            ivMoreFeatures.singleClick { actionListener.onMore(it, position) }

            setPlayStatus(
                bean.isPlaying,
                bean.actionableStatus.playingButtonDisappears,
                bean.actionableStatus.ttsGenerated,
                this
            )
            binding.statusPlay.singleClick { actionListener.onPlay(position) }
            binding.statusPause.singleClick { actionListener.pausePlayer() }


            holder.addOnClickListener(R.id.sivAvatar)
            holder.addOnClickListener(R.id.tvUserNameNickName)
            holder.addOnClickListener(R.id.tvForwarderUsername)
            holder.addOnClickListener(R.id.status_trade_layout)
        }
    }


    override fun bindView(binding: AdapterSearchVoicesBinding, bean: StatusModel, position: Int) {


    }

    override fun getLogicData(): List<StatusModel> {
        return data
    }

    override fun findStatusById(id: String): Int {
        return data.indexOfFirst { it.id == id }
    }

    override fun getDataActionableId(position: Int): String {
        return data[position].actionableId
    }

    override fun getDataId(position: Int): String {
        return data[position].id
    }

    override fun onReply(position: Int) {
        val item = data[position]
        data.forEachIndexed { index, statusModel ->
            if (statusModel.actionableId == item.actionableId) {
                updateActionable(index) {
                    val repliesCount = it.repliesCount + 1
                    it.copy(repliesCount = repliesCount.coerceAtLeast(0))
                }
            }
        }
    }

    override fun onReblog(status: StatusModel?, position: Int) {
        if (status != null) {
            data.forEachIndexed { index, statusModel ->
                if (statusModel.actionableId == status.actionableId) {
                    updateActionable(index) {
                        val reblogCount =
                            if (status.reblogged) it.reblogCount + 1 else it.reblogCount - 1
                        it.copy(
                            reblogged = status.reblogged,
                            reblogCount = reblogCount.coerceAtLeast(0)
                        )
                    }
                }
            }
        } else refreshNotifyItemChanged(position)
    }

    override fun onFavourite(status: StatusModel?) {
        if (status != null) {
            data.forEachIndexed { index, statusModel ->
                if (statusModel.actionableId == status.actionableId) {
                    updateActionable(index) {
                        val favouriteCount =
                            if (status.favourited) it.favouriteCount + 1 else it.favouriteCount - 1
                        it.copy(
                            favourited = status.favourited,
                            favouriteCount = favouriteCount.coerceAtLeast(0)
                        )
                    }
                }
            }
        }
    }

    override fun onUpdateVoiceModel(status: StatusModel) {
        data.forEachIndexed { index, statusModel ->
            if (status.actionableStatus.voiceModel?.id == statusModel.actionableStatus.voiceModel?.id) {
                updateActionable(index) {
                    it.copy(voiceModel = status.voiceModel)
                }
            }
        }
    }

    override fun onPurchasedVoiceModel(modelId: Long) {
        data.forEachIndexed { index, statusModel ->
            if (statusModel.actionableStatus.voiceModel?.id == modelId) {
                updateActionable(index) {
                    it.copy(
                        voiceModel = it.voiceModel?.copy(isModelOwner = true)
                    )
                }
            }
        }
    }

    override fun onPin(status: StatusModel) {
        data.forEachIndexed { index, statusModel ->
            if (statusModel.actionableId == status.actionableId) {
                updateActionable(index) {
                    it.copy(pinned = status.pinned)
                }
            }
        }
    }

    override fun removeAllByAccountId(accountId: String): List<StatusModel> {
        val removedList = arrayListOf<StatusModel>()
        for (index in (itemCount - 1) downTo 0) {
            val status = data[index]
            if (status.account.id == accountId || status.actionableStatus.account.id == accountId) {
                val removed = data.removeAt(index)
                removedList.add(removed)
                refreshNotifyItemRemoved(index)
            }
        }
        return removedList
    }

    override fun playingStateChange(isPlaying: Boolean, position: Int) {
        data[position].copy(isPlaying = isPlaying).update(position)
    }

    override fun removeItem(status: StatusModel) {
        //如果删除的数据在本界面也存在就删除该列
        val position = data.indexOfFirst { it.id == status.id }
        if (position != -1)
            removeData(position)

        val positionAction = data.indexOfFirst { it.actionableId == status.actionableId }
        if (positionAction != -1)
            removeData(positionAction)

        //如果删除的是回复，就找到回复的原声文将repliesCount - 1，需要注意的是如果原声文是转发，需要就转发和原声文的repliesCount - 1
        if (!TextUtils.isEmpty(status.actionableStatus.inReplyToId)) {
            data.forEachIndexed { index, statusModel ->
                if (status.actionableStatus.inReplyToId == statusModel.actionableStatus.id) {
                    updateActionable(index) {
                        val repliesCount = it.repliesCount - 1
                        it.copy(repliesCount = repliesCount.coerceAtLeast(0))
                    }
                }
            }
        }
        //如果删除的是转发，需要将原声文的reblogs_count - 1
        if (status.reblogStatus != null) {
            data.forEachIndexed { index, statusModel ->
                if (status.actionableStatus.id == statusModel.actionableStatus.id) {
                    updateActionable(index) {
                        val reblogCount = it.reblogCount - 1
                        it.copy(reblogCount = reblogCount.coerceAtLeast(0), reblogged = false)
                    }
                }
            }
        }
    }

    override fun updateAccountData(account: AccountBean) {
        data.forEachIndexed { index, statusModel ->
            if (statusModel.actionableStatus.account.id == account.id) {
                updateActionable(index) {
                    it.copy(
                        account = it.account.copy(
                            displayName = account.displayName,
                            username = account.username,
                            avatar = account.avatar
                        )
                    )
                }
            }
        }


    }

    override fun updateVoice(id: String, ossId: String) {
        data.forEachIndexed { index, statusModel ->
            if (statusModel.id == id) {
                updateActionable(index) {
                    it.copy(ossId = ossId, ttsGenerated = true)
                }
                actionListener.addMediaItem(data[index])
            }
        }


    }


    /**
     * 时间显示
     */
    private fun getTimestampInfo(createAt: Date): String {
        val then = createAt.time
        val now = System.currentTimeMillis()
        return TimestampUtils.getRelativeTimeSpanString(mContext, then, now)
    }


    /**
     * 设置声文内容
     */
    private fun setStatusContent(
        content: Spanned,
        mentions: List<StatusModel.Mention>?,
        tags: List<HashTag>?,
        binding: AdapterSearchVoicesBinding,
        position: Int
    ) {
        setClickableText(binding.tvStatusContent, content, mentions, tags, actionListener)
        if (TextUtils.isEmpty(binding.tvStatusContent.text)) {
            binding.tvStatusContent.gone()
        } else {
            binding.tvStatusContent.apply {
                visible()
                singleClick {
                    actionListener.onShowDetail(position)
                }
            }
        }
    }

    /**
     * 判断是否有媒体数据
     */
    private fun hasPreviewAttachment(attachments: List<Attachment>): Boolean {
        for (attachment in attachments) {
            if (attachment.type === Attachment.Type.AUDIO || attachment.type === Attachment.Type.UNKNOWN) {
                return false
            }
        }
        return true
    }

    /**
     * 设置预览图
     */
    private fun setMediaPreviews(
        attachments: List<Attachment>,
        binding: AdapterSearchVoicesBinding,
        position: Int
    ) {
        if (attachments.isNotEmpty() && hasPreviewAttachment(attachments)) {
            binding.sivPreview.apply {
                visible()
                singleClick {
                    actionListener.onShowDetail(position)
                }
            }

            binding.statusMediaOverlay0.apply {
                visibility(attachments[0].type == Attachment.Type.VIDEO)
                singleClick {
                    actionListener.onShowDetail(position)
                }
            }
            binding.sivPreview.loadAvatar(attachments[0].previewUrl!!)
        } else {
            binding.sivPreview.gone()
            binding.statusMediaOverlay0.gone()
        }
    }

    /**
     * 设置转发按钮
     */
    private fun setReblogEnabled(
        enabled: Boolean,
        reblogged: Boolean,
        visibility: StatusModel.Visibility,
        binding: AdapterSearchVoicesBinding
    ) {


        binding.statusInset.isChecked = reblogged
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

    private fun updateActionable(position: Int, updater: (StatusModel) -> StatusModel) {
        val status = data[position]
        if (status.reblog != null) {
            status.copy(reblog = updater(status.reblog)).update(position)
        } else {
            updater(status).update(position)
        }
    }

    private fun StatusModel.update(position: Int) {
        data[position] = this
        Timber.e("update : $position")
        refreshNotifyItemChanged(position)
    }


    /**
     * 设置喜欢按钮
     */
    private fun setFavourited(favourited: Boolean, binding: AdapterSearchVoicesBinding) {
        binding.statusFavourite.isChecked = favourited
    }


    /**
     * 设置播放按钮状态
     */
    private fun setPlayStatus(
        isPlaying: Boolean,
        playingButtonDisappears: Boolean,
        ttsGenerated: Boolean,
        binding: AdapterSearchVoicesBinding
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

    /**
     * 设置是否是推荐状态
     */
    private var isRecommended: Boolean = false
    fun setRecommendedState(_isRecommended: Boolean) {
        isRecommended = _isRecommended
    }
}