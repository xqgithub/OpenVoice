package com.shannon.openvoice.business.pay

import com.shannon.android.lib.base.viewmodel.BaseViewModel
import com.shannon.android.lib.util.ToastUtil
import com.shannon.openvoice.FunApplication
import com.shannon.openvoice.R
import com.shannon.openvoice.model.EventPurchasedModel
import com.shannon.openvoice.model.ModelDetail
import com.shannon.openvoice.network.apiService
import com.shannon.openvoice.network.convertResponse
import org.greenrobot.eventbus.EventBus
import retrofit2.http.Field

/**
 *
 * @Package:        com.shannon.openvoice.business.pay
 * @ClassName:      PaymentViewModel
 * @Description:     作用描述
 * @Author:         czhen
 * @CreateDate:     2022/10/25 17:27
 */
class PaymentViewModel : BaseViewModel() {

    fun modelDetail(modelId: Long, showLoading: Boolean = true, onResult: (ModelDetail) -> Unit) {
        apiService.modelDetail(modelId)
            .convertResponse()
            .funSubscribe(showLoading) {
                onResult(it)
            }
    }

    fun buyModel(id: Long, address: String, onResult: () -> Unit) {
        apiService.buyVoiceModel(id, address)
            .convertResponse()
            .funSubscribeNotLoading {
                onResult()
            }
    }

    fun buyModelDone(
        id: Long,
        hash: String,
        address: String,
        onFailed: () -> Unit,
        onResult: () -> Unit
    ) {
        apiService.buyVoiceModelDone(id, hash, address)
            .convertResponse()
            .funSubscribeNotLoading({ onFailed() }, {
                onResult()
                EventBus.getDefault().post(EventPurchasedModel(id))
            })
    }

    fun following(id: String, onResult: (Boolean) -> Unit) {
        apiService.relationships(arrayListOf(id))
            .convertResponse()
            .funSubscribeNotLoading {
                if (it.isNotEmpty()) onResult(it[0].following)
            }
    }

    fun follow(isFollowing: Boolean, accountId: String, onResult: (Boolean) -> Unit) {
        val observable =
            if (isFollowing) apiService.unfollowAccount(accountId) else apiService.followAccount(
                accountId,
                showReblogs = true,
                true
            )
        observable.convertResponse()
            .funSubscribe {
                onResult(it.following)
            }
    }

    fun likeVoiceModel(isLiked: Boolean, id: Long, onResult: (Boolean) -> Unit) {
        val observable =
            if (isLiked) apiService.unlikeVoiceModel(id) else apiService.likeVoiceModel(id)

        observable.convertResponse()
            .funSubscribe {
                onResult(!isLiked)
            }
    }


    fun updateVoiceModel(
        showLoading: Boolean = true,
        modelId: Long,
        price: String? = null,
        currency: String? = null,
        royaltiesFee: String? = null,
        payload: String? = null,
        saleCycle: String? = null,
        onResult: () -> Unit
    ) {
        apiService.updateVoiceModel(modelId, price, currency, royaltiesFee, payload, saleCycle)
            .convertResponse()
            .funSubscribe(showLoading) {
                onResult()
            }
    }

}