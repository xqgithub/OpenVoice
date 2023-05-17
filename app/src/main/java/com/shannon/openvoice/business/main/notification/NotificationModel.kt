package com.shannon.openvoice.business.main.notification

import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.MutableLiveData
import com.blankj.utilcode.util.LogUtils
import com.shannon.android.lib.base.viewmodel.BaseViewModel
import com.shannon.android.lib.exception.XException
import com.shannon.android.lib.extended.isBlankPlus
import com.shannon.android.lib.extended.observableToMain
import com.shannon.openvoice.model.ErrorResponse
import com.shannon.openvoice.model.Notification
import com.shannon.openvoice.network.apiService
import com.shannon.openvoice.network.convertResponse
import com.shannon.openvoice.network.gson

/**
 * Date:2022/9/1
 * Time:10:13
 * author:dimple
 */
class NotificationModel : BaseViewModel() {

    /**
     * 获取通知列表信息数据
     */

    val notificationsLive = MutableLiveData<List<Notification>>()

    fun notifications(
        maxId: String? = null,
        sinceId: String? = null,
        excludes: Set<Notification.Type>? = null,
        onCallBack: (isSuccess: Boolean, linkHeader: String?) -> Unit
    ) {
        apiService.notifications(maxId = maxId, sinceId = sinceId, excludes = excludes)
            .compose(observableToMain())
            .subscribe({
                if (it.isSuccessful && !isBlankPlus(it.body())) {
                    val linkHeader = it.headers()["Link"]
                    notificationsLive.postValue(it.body())
                    onCallBack(true, linkHeader)
                } else {
                    onCallBack(false, "")

                    val code = it.raw().code
                    val errorBody = it.errorBody()?.string() ?: ""
                    val errorMessage = gson.fromJson(errorBody, ErrorResponse::class.java).error
                    throw XException(code, errorMessage)
                }
            }, {
                LogUtils.e("onError =-=  $it")
            })
    }


    /**
     * 清空通知信息
     */
    fun clearNotifications(
        viewLifecycleOwner: LifecycleOwner,
        onCallback: (isSuccess: Boolean) -> Unit
    ) {
        apiService.clearNotifications().convertResponse()
            .funSubscribeRxLife(owner = viewLifecycleOwner, onNext = {
                LogUtils.i("subscribe =-=  $it")
                onCallback(true)
            }, onError = {
                LogUtils.e("onError =-=  $it")
                onCallback(false)
            })
    }


}