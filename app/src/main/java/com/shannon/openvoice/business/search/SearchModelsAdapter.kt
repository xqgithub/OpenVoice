package com.shannon.openvoice.business.search

import android.content.Context
import android.graphics.Color
import android.text.Spanned
import android.text.TextUtils
import androidx.core.content.ContextCompat
import com.blankj.utilcode.util.FragmentUtils.setBackgroundColor
import com.bumptech.glide.Glide
import com.shannon.android.lib.base.adapter.BaseFunBindingRecyclerViewAdapter
import com.shannon.android.lib.extended.*
import com.shannon.android.lib.util.ToastUtil
import com.shannon.openvoice.FunApplication
import com.shannon.openvoice.FunApplication.Companion.appViewModel
import com.shannon.openvoice.R
import com.shannon.openvoice.business.creation.detail.ModelDetailActivity
import com.shannon.openvoice.business.main.mine.account.AccountManager
import com.shannon.openvoice.business.main.mine.account.AccountManager.Companion.accountManager
import com.shannon.openvoice.business.player.PlayerHelper
import com.shannon.openvoice.business.timeline.listener.AdapterLogicListener
import com.shannon.openvoice.business.timeline.listener.StatusActionListener
import com.shannon.openvoice.databinding.AdapterSearchModelsBinding
import com.shannon.openvoice.databinding.AdapterSearchTopicsBinding
import com.shannon.openvoice.extended.loadAvatar
import com.shannon.openvoice.model.*
import com.shannon.openvoice.util.TimestampUtils
import com.shannon.openvoice.util.UnitHelper
import com.shannon.openvoice.util.setClickableText
import me.jingbin.library.adapter.BaseByViewHolder
import timber.log.Timber
import java.util.*

/**
 * Date:2022/8/31
 * Time:10:51
 * author:dimple
 * 搜索模型
 */
class SearchModelsAdapter(
    val mContext: Context,
    private val actionListener: StatusActionListener,
    val onPlay: (Int, String) -> Unit
) :
    BaseFunBindingRecyclerViewAdapter<TradeList, AdapterSearchModelsBinding>(),
    AdapterLogicListener {

    private var playingPosition = -1


    private var statuses: MutableList<StatusModel> = mutableListOf()

    //是否是推荐模型列表
    private var isRecommendedTopicList: Boolean = true

    /**
     * 如果是推荐话题，需要设置声文数据
     */
    fun setStatuses(_statuses: List<StatusModel>, _isRecommendedTopicList: Boolean) {
        statuses.apply {
            clear()
            _statuses.forEach {
                this.add(it)
            }
        }
        isRecommendedTopicList = _isRecommendedTopicList
    }

    /**
     * 获取声文数据
     */
    fun getStatuses() = statuses

    /**
     * 获取playingPosition
     */
    fun getPlayingPosition() = playingPosition

    /**
     * 获取是否是推荐模型列表
     */
    fun getIsRecommendedTopicList() = isRecommendedTopicList


    fun playingStateChangeToModel(playingModelId: String, playStatus: Boolean) {
        try {
            val playingPosition = data.indexOfFirst { it.id.toString() == playingModelId }
            if (playingPosition < itemCount) {
                data[playingPosition] =
                    getItemData(playingPosition).copy(isPlaying = playStatus)
                refreshNotifyItemChanged(playingPosition)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }


    override fun bindView(
        holder: BaseByViewHolder<TradeList>,
        binding: AdapterSearchModelsBinding,
        bean: TradeList,
        position: Int
    ) {
        binding.apply {
            stModelAvatar.apply {
                text = bean.voiceModel.name
                setBackgroundColor(getModelAvatarBg(position))
            }
            tvName.text = bean.voiceModel.name
            tvPublishedNumber.text =
                "${addComma("${bean.voiceModel.usageCount}")} ${mContext.getString(R.string.openvoiceover)}"

            playButton2.apply {
                isSelected = bean.isPlaying == true
                singleClick {
                    playingPosition = holder.holderPosition()
                    onPlay(holder.holderPosition(), SearchActivity.model)
                }
            }

            if (isRecommendedTopicList) {
                if (position < statuses.size && !isBlankPlus(statuses[position])) {
                    sclPopularModules.visible()
                    val statuse = statuses[position]

                    sivAvatar.loadAvatar(statuse.account.avatar)
                    tvUserNameNickName.text = statuse.account.name
                    tvForwarderUsername.text =
                        mContext.getString(R.string.post_username_format, statuse.account.username)
                    tvTime.text = getTimestampInfo(statuse.createdAt)

                    if (statuse.voiceModel?.status == "deleted") {
                        val _modelName =
                            "${mContext.getString(R.string.model_name)}${statuse.voiceModel?.name}"
                        val deleteTagView =
                            "( ".plus(mContext.getString(R.string.deleted)).plus(" )")
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
                            "${mContext.getString(R.string.model_name)}${statuse.voiceModel?.name}"
                    }

                    setStatusContent(
                        statuse.actionableStatus.content.parseAsAppHtml(),
                        statuse.actionableStatus.mentions,
                        statuse.actionableStatus.tags,
                        this,
                        position
                    )

                    setMediaPreviews(statuse.attachments, this, position)

                    replayCountView.text = UnitHelper.quantityConvert(statuse.repliesCount)
                    reblogCountView.text = UnitHelper.quantityConvert(statuse.reblogCount)
                    favouriteCountView.text = UnitHelper.quantityConvert(statuse.favouriteCount)

                    statusReply.singleClick { actionListener.onReply(position) }


                    setReblogEnabled(
                        statuse.actionableStatus.reblogAllowed,
                        statuse.actionableStatus.reblogged,
                        statuse.actionableStatus.visibility,
                        this
                    )
                    binding.statusInset.setEventListener { _, buttonState ->
                        actionListener.onReblog(!buttonState, position)
                        return@setEventListener true
                    }

                    setFavourited(statuse.actionableStatus.favourited, this)
                    binding.statusFavourite.setEventListener { _, buttonState ->
                        actionListener.onFavourite(!buttonState, position)
                        return@setEventListener accountManager.isLogin()
                    }

                    ivMoreFeatures.singleClick { actionListener.onMore(it, position) }

                    setPlayStatus(
                        statuse.isPlaying,
                        statuse.actionableStatus.playingButtonDisappears,
                        statuse.actionableStatus.ttsGenerated,
                        this
                    )

                    binding.statusPlay.singleClick {
                        actionListener.onPlay(position)
//                        onPlay(holder.holderPosition(), SearchActivity.postes)
                    }

                    binding.statusPause.singleClick { actionListener.pausePlayer() }
                } else {
                    sclPopularModules.gone()
                }
            } else {
                sclPopularModules.gone()
            }

            holder.addOnClickListener(R.id.cl_main)
            holder.addOnClickListener(R.id.sivAvatar)
            holder.addOnClickListener(R.id.tvUserNameNickName)
            holder.addOnClickListener(R.id.tvForwarderUsername)
            holder.addOnClickListener(R.id.status_trade_layout)
        }
    }


    /**
     * 设置ModelAvatar背景色
     */
    private fun getModelAvatarBg(position: Int): Int {
        var bgColor = "#7FF7EC"
        val colors = arrayOf("#7FF7EC", "#7997FF", "#BF8CFF")
        if (position > 0) {
            when (position % 3) {
                0 -> bgColor = colors[0]
                1 -> bgColor = colors[1]
                2 -> bgColor = colors[2]
            }
        }
        return Color.parseColor(bgColor)
    }


    override fun bindView(binding: AdapterSearchModelsBinding, bean: TradeList, position: Int) {

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
        binding: AdapterSearchModelsBinding,
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
     * 设置预览图
     */
    private fun setMediaPreviews(
        attachments: List<Attachment>,
        binding: AdapterSearchModelsBinding,
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
     * 设置转发按钮
     */
    private fun setReblogEnabled(
        enabled: Boolean,
        reblogged: Boolean,
        visibility: StatusModel.Visibility,
        binding: AdapterSearchModelsBinding
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


    /**
     * 设置喜欢按钮
     */
    private fun setFavourited(favourited: Boolean, binding: AdapterSearchModelsBinding) {
        binding.statusFavourite.isChecked = favourited
    }


    /**
     * 设置播放按钮状态
     */
    private fun setPlayStatus(
        isPlaying: Boolean,
        playingButtonDisappears: Boolean,
        ttsGenerated: Boolean,
        binding: AdapterSearchModelsBinding
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

    override fun getLogicData(): List<StatusModel> {
        return statuses
    }

    override fun findStatusById(id: String): Int {
        return statuses.indexOfFirst { it.id == id }
    }

    override fun getDataActionableId(position: Int): String {
        return statuses[position].actionableId
    }

    override fun getDataId(position: Int): String {
        return statuses[position].id
    }

    override fun onReply(position: Int) {
        val item = statuses[position]
        statuses.forEachIndexed { index, statusModel ->
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
            statuses.forEachIndexed { index, statusModel ->
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
            statuses.forEachIndexed { index, statusModel ->
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
        statuses.forEachIndexed { index, statusModel ->
            if (status.actionableStatus.voiceModel?.id == statusModel.actionableStatus.voiceModel?.id) {
                updateActionable(index) {
                    it.copy(voiceModel = status.voiceModel)
                }
            }
        }
    }

    override fun onPurchasedVoiceModel(modelId: Long) {
        statuses.forEachIndexed { index, statusModel ->
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
        statuses.forEachIndexed { index, statusModel ->
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
            val status = statuses[index]
            if (status.account.id == accountId || status.actionableStatus.account.id == accountId) {
                val removed = statuses.removeAt(index)
                removedList.add(removed)
                refreshNotifyItemRemoved(index)
            }
        }
        return removedList
    }

    override fun playingStateChange(isPlaying: Boolean, position: Int) {
        statuses[position].copy(isPlaying = isPlaying).update(position)
    }

    override fun removeItem(status: StatusModel) {
        //如果删除的数据在本界面也存在就删除该列
        val position = statuses.indexOfFirst { it.id == status.id }
        if (position != -1)
            removeData(position)

        val positionAction = statuses.indexOfFirst { it.actionableId == status.actionableId }
        if (positionAction != -1)
            removeData(positionAction)

        //如果删除的是回复，就找到回复的原声文将repliesCount - 1，需要注意的是如果原声文是转发，需要就转发和原声文的repliesCount - 1
        if (!TextUtils.isEmpty(status.actionableStatus.inReplyToId)) {
            statuses.forEachIndexed { index, statusModel ->
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
            statuses.forEachIndexed { index, statusModel ->
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
        statuses.forEachIndexed { index, statusModel ->
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
        statuses.forEachIndexed { index, statusModel ->
            if (statusModel.id == id) {
                updateActionable(index) {
                    it.copy(ossId = ossId, ttsGenerated = true)
                }
                actionListener.addMediaItem(statuses[index])
            }
        }
    }


    private fun updateActionable(position: Int, updater: (StatusModel) -> StatusModel) {
        val status = statuses[position]
        if (status.reblog != null) {
            status.copy(reblog = updater(status.reblog)).update(position)
        } else {
            updater(status).update(position)
        }
    }

    private fun StatusModel.update(position: Int) {
        statuses[position] = this
        Timber.e("update : $position")
        refreshNotifyItemChanged(position)
    }
}