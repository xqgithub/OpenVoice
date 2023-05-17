package com.shannon.openvoice.business.main.trade

import androidx.lifecycle.MutableLiveData
import com.shannon.android.lib.base.viewmodel.BaseViewModel
import com.shannon.android.lib.player.media.MediaData
import com.shannon.android.lib.util.ToastUtil
import com.shannon.openvoice.FunApplication
import com.shannon.openvoice.R
import com.shannon.openvoice.business.player.MediaInfo
import com.shannon.openvoice.business.player.PlayerHelper
import com.shannon.openvoice.model.EventPurchasedModel
import com.shannon.openvoice.model.ModelDetail
import com.shannon.openvoice.model.StatusModel
import com.shannon.openvoice.model.TradeList
import com.shannon.openvoice.network.apiService
import com.shannon.openvoice.network.convertResponse
import com.shannon.openvoice.util.CloudStorageHelper
import org.greenrobot.eventbus.EventBus
import timber.log.Timber

/**
 *
 * @Package:        com.shannon.openvoice.business.main.trade
 * @ClassName:      TradeViewModel
 * @Description:     作用描述
 * @Author:         czhen
 * @CreateDate:     2022/8/31 15:39
 */
class TradeViewModel : BaseViewModel() {

    val timeSelectedPosition = MutableLiveData(0)

    fun getTradeList(
        offset: Int = 0,
        showLoading: Boolean = false,
        type: Int,
        onResult: (List<TradeList>) -> Unit
    ) {
        val value = timeSelectedPosition.value ?: 0
        //由于调整了顺序(全部移到了第一个)
        val timeRang = if (value == 0) 4 else value - 1 // 查询时间范围 0/24小时 1/7天 2/14天 3/30天 4/全部
        Timber.d("Trade: value = $value ; timeRang = $timeRang")
        apiService.getTradeList(
            timeRang,
            type,
            offset = offset * 10,
            limit = 10
        )
            .convertResponse()
            .funSubscribe(showLoading) {
                onResult(it)
            }
    }

    fun buyModel(id: Long, onResult: () -> Unit) {
        apiService.purchaseVoiceModel(id)
            .convertResponse()
            .funSubscribe {
                onResult()
                ToastUtil.showCenter(
                    FunApplication.getInstance().getString(R.string.model_purchase_succ)
                )
                EventBus.getDefault().post(EventPurchasedModel(id))
            }
    }

    fun modelDetail(modelId: Long, onResult: (ModelDetail) -> Unit) {
        apiService.modelDetail(modelId)
            .convertResponse()
            .funSubscribe {
                onResult(it)
            }
    }

    fun following(id: String, onResult: (Boolean) -> Unit) {
        apiService.relationships(arrayListOf(id))
            .convertResponse()
            .funSubscribeNotLoading {
                if (it.isNotEmpty()) onResult(it[0].following)
            }
    }

    fun followAccount(accountId: String, onResult: () -> Unit) {
        apiService.followAccount(accountId, showReblogs = true, true)
            .convertResponse()
            .funSubscribe {
                if (it.following) onResult()
            }
    }

    fun unfollowAccount(accountId: String, onResult: () -> Unit) {
        apiService.unfollowAccount(accountId)
            .convertResponse()
            .funSubscribe {
                if (!it.following) onResult()
            }
    }

    fun modelStatus(
        itemId: String,
        modelId: Long,
        maxId: String?,
        onResult: (List<MediaInfo>) -> Unit
    ) {
        apiService.modelStatuses(modelId, maxId, null, null, 30)
            .convertResponse().funSubscribe {
                if (it.isNotEmpty()) {
                    val mediaList = PlayerHelper.convert2MediaInfo(it)
                    if (mediaList.isNotEmpty()) {
                        onResult(mediaList)
                    } else {
                        modelStatus(itemId, modelId, it.last().id, onResult)
                    }
                } else {
                    onResult(arrayListOf())
                }
            }
    }

    private val cloudStorageHelper by lazy { CloudStorageHelper() }
    private fun convertMediaItem(data: List<StatusModel>): List<MediaData> {
        val mediaSources = arrayListOf<MediaData>()
        data.forEach {
            val statusOssId = it.actionableStatus.ossId
            if (statusOssId?.isNotEmpty() == true) {
                mediaSources.add(
                    MediaData(
                        it.id,
                        cloudStorageHelper.getResourceUrl(statusOssId)
                    )
                )
            }
        }
        return mediaSources
    }

    fun likeVoiceModel(id: Long, onResult: () -> Unit) {
        apiService.likeVoiceModel(id)
            .convertResponse()
            .funSubscribe {
                onResult()
            }
    }

    fun unlikeVoiceModel(id: Long, onResult: () -> Unit) {
        apiService.unlikeVoiceModel(id)
            .convertResponse()
            .funSubscribe {
                onResult()
            }
    }
}