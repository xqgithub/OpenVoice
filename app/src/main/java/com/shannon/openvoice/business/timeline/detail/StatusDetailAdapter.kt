package com.shannon.openvoice.business.timeline.detail

import android.content.Context
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.shannon.openvoice.business.main.mine.account.AccountManager
import com.shannon.openvoice.business.timeline.TimelineViewHolder
import com.shannon.openvoice.business.timeline.listener.AdapterLogicListener
import com.shannon.openvoice.business.timeline.listener.StatusActionListener
import com.shannon.openvoice.databinding.ItemStatusBinding
import com.shannon.openvoice.databinding.ItemStatusDetailBinding
import com.shannon.openvoice.model.AccountBean
import com.shannon.openvoice.model.StatusModel
import me.jingbin.library.adapter.BaseByRecyclerViewAdapter
import me.jingbin.library.adapter.BaseByViewHolder
import timber.log.Timber

/**
 *
 * @ProjectName:    OpenVoice
 * @Package:        com.shannon.openvoice.business.timeline.detail
 * @ClassName:      StatusDetailAdapter
 * @Description:     java类作用描述
 * @Author:         czhen
 * @CreateDate:     2022/8/18 9:33
 */
class StatusDetailAdapter(
    private val mContext: Context,
    private val actionListener: StatusActionListener
) :
    BaseByRecyclerViewAdapter<StatusModel, BaseByViewHolder<StatusModel>>(), AdapterLogicListener {

    var detailedStatusPosition: Int = RecyclerView.NO_POSITION

    override fun getItemViewType(position: Int): Int {
        return if (position == detailedStatusPosition) {
            VIEW_TYPE_STATUS_DETAILED
        } else {
            VIEW_TYPE_STATUS
        }
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): BaseByViewHolder<StatusModel> {
        return if (viewType == VIEW_TYPE_STATUS) {
            val binding =
                ItemStatusDetailBinding.inflate(LayoutInflater.from(mContext), parent, false)
            StatusDetailViewHolder(binding, actionListener)
        } else {
//            val binding = ItemStatusDetailBinding.inflate(LayoutInflater.from(mContext), parent, false)
//            StatusDetailViewHolder(binding, actionListener)
            val binding =
                ItemStatusDetailBinding.inflate(LayoutInflater.from(mContext), parent, false)
            StatusDetailViewHolder(binding, actionListener)
        }
    }

    override fun findStatusById(id: String): Int {
        return data.indexOfFirst { it.id == id }
    }

    override fun getLogicData(): MutableList<StatusModel> {
        return data
    }

    override fun getDataActionableId(position: Int) = data[position].actionableId

    override fun getDataId(position: Int) = data[position].id

    override fun onReply(position: Int) {
        val item = data[position]
        data.forEachIndexed { index, statusModel ->
            if(statusModel.actionableId == item.actionableId){
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
                if(statusModel.actionableId == status.actionableId){
                    updateActionable(index) {
                        val reblogCount = if (status.reblogged) it.reblogCount + 1 else it.reblogCount - 1
                        it.copy(reblogged = status.reblogged, reblogCount = reblogCount.coerceAtLeast(0))
                    }
                }
            }
        } else refreshNotifyItemChanged(position)
    }

    override fun onFavourite(status: StatusModel?) {
        if (status != null) {
            data.forEachIndexed { index, statusModel ->
                if(statusModel.actionableId == status.actionableId){
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
            if (status.actionableStatus.voiceModel != null && status.actionableStatus.voiceModel?.id == statusModel.actionableStatus.voiceModel?.id) {
                updateActionable(index) {
                    it.copy(voiceModel = status.actionableStatus.voiceModel)
                }
            }
        }
    }

    override fun onPurchasedVoiceModel(modelId: Long) {
        data.forEachIndexed { index, statusModel ->
            if (statusModel.actionableStatus.voiceModel?.id == modelId && statusModel.actionableStatus.voiceModel?.ownerAccount != null) {
                val account = AccountManager.accountManager.getAccountData()
                updateActionable(index) {
                    it.copy(
                        voiceModel = it.voiceModel?.copy(
                            ownerAccount = it.voiceModel.ownerAccount.copy(
                                id = account.id
                            )
                        )
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
        if(status.reblogStatus != null){
            data.forEachIndexed { index, statusModel ->
                if (status.actionableStatus.id == statusModel.actionableStatus.id) {
                    updateActionable(index) {
                        val reblogCount = it.reblogCount - 1
                        it.copy(reblogCount = reblogCount.coerceAtLeast(0),reblogged = false)
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

    companion object {
        private const val VIEW_TYPE_STATUS = 0
        private const val VIEW_TYPE_STATUS_DETAILED = 1
    }

}