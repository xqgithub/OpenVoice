package com.shannon.openvoice.business.search

import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.MutableLiveData
import com.blankj.utilcode.util.LogUtils
import com.shannon.android.lib.base.viewmodel.BaseViewModel
import com.shannon.android.lib.extended.isBlankPlus
import com.shannon.android.lib.util.FileUtil.copy
import com.shannon.openvoice.business.search.SearchActivity.Companion.account
import com.shannon.openvoice.business.search.SearchActivity.Companion.model
import com.shannon.openvoice.business.search.SearchActivity.Companion.postes
import com.shannon.openvoice.business.search.SearchActivity.Companion.topics
import com.shannon.openvoice.model.*
import com.shannon.openvoice.network.apiService
import com.shannon.openvoice.network.convertResponse

/**
 * Date:2022/8/29
 * Time:14:10
 * author:dimple
 */
class SearchModel : BaseViewModel() {

    /**
     * 搜索
     */
    val accountsLive = MutableLiveData<List<TimelineAccount>>()
    val topicsLive = MutableLiveData<List<HashTag>>()
    val voiceModelsLive = MutableLiveData<List<SearchVoiceModels>>()

    //    val postesLive = MutableLiveData<List<StatusModel>>()
    val postesLive = MutableLiveData<SearchRecommendedStatuesBean>()

    /**
     * 推荐
     */
    val recommendTopicsLive = MutableLiveData<SearchRecommendedTopicsBean>()
    val recommendVoiceModelsLive = MutableLiveData<SearchRecommendedModelsBean>()
    val recommendAccountsLive = MutableLiveData<List<TimelineAccount>>()

    fun searchObservable(
        q: String,
        type: String,
        limit: Int = 20,
        offset: Int = 0,
        viewLifecycleOwner: LifecycleOwner
    ) {
        //搜索内容为空，调用获取搜索推荐
        if (isBlankPlus(q)) {
            searchRecommend(
                type,
                limit = if (type == account || type == model) 50 else 20,
                viewLifecycleOwner = viewLifecycleOwner
            )
            return
        }

        apiService.searchObservable(
            q, type, true, limit, offset, false
        ).convertResponse()
            .funSubscribeRxLife(owner = viewLifecycleOwner,
                onNext = {
                    when (type) {
                        account -> {
                            accountsLive.postValue(it.accounts)
                        }
                        topics -> {
                            topicsLive.postValue(it.hashtags)
                        }
                        model -> {
                            voiceModelsLive.postValue(it.voiceModels)
                        }
                        else -> {
                            postesLive.postValue(
                                SearchRecommendedStatuesBean(
                                    isRecommended = false,
                                    it.statuses
                                )
                            )
                        }
                    }
                }, onError = {
                    LogUtils.e("onError =-=  $it")
                })
    }


    /**
     * 获取搜索推荐
     */
    fun searchRecommend(type: String, limit: Int = 20, viewLifecycleOwner: LifecycleOwner) {
        apiService.searchRecommend(type, limit)
            .convertResponse()
            .funSubscribeRxLife(owner = viewLifecycleOwner,
                onNext = {
                    when (type) {
                        topics -> {
                            recommendTopicsLive.postValue(
                                SearchRecommendedTopicsBean(
                                    it.hashtags,
                                    it.statuses
                                )
                            )
                        }
                        postes -> {
                            postesLive.postValue(
                                SearchRecommendedStatuesBean(
                                    isRecommended = true,
                                    it.statuses
                                )
                            )
                        }
                        model -> {
                            recommendVoiceModelsLive.postValue(
                                SearchRecommendedModelsBean(
                                    it.voiceModels,
                                    it.statuses
                                )
                            )
                        }
                        account -> {
                            recommendAccountsLive.postValue(it.accounts)
                        }
                    }
                }, onError = {
                    LogUtils.e("onError =-=  $it")
                })
    }


    /**
     * 取消关注用户
     */
    fun unfollowAccount(
        accountId: String,
        owner: LifecycleOwner,
        callback: (isSuccess: Boolean) -> Unit
    ) {
        apiService.unfollowAccount(accountId)
            .convertResponse()
            .funSubscribeRxLife(owner = owner, onNext = {
                callback(true)
            }, onError = {
                LogUtils.e("onError =-=  $it")
            })
    }

    /**
     * 关注用户
     */
    fun followAccount(
        accountId: String,
        showReblogs: Boolean = true,
        notify: Boolean = false,
        owner: LifecycleOwner,
        callback: (isSuccess: Boolean) -> Unit
    ) {
        apiService.followAccount(accountId, showReblogs, notify)
            .convertResponse()
            .funSubscribeRxLife(owner = owner, onNext = {
                callback(true)
            }, onError = {
                LogUtils.e("onError =-=  $it")
            })
    }


}