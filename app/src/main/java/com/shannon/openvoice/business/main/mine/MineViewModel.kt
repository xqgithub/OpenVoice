package com.shannon.openvoice.business.main.mine

import android.app.Activity
import android.content.Context
import android.net.Uri
import androidx.core.net.toUri
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.MutableLiveData
import com.blankj.utilcode.util.LogUtils
import com.google.gson.JsonObject
import com.shannon.android.lib.base.viewmodel.BaseViewModel
import com.shannon.android.lib.exception.XException
import com.shannon.android.lib.extended.isBlankPlus
import com.shannon.android.lib.extended.observableToMain
import com.shannon.android.lib.util.PreferencesUtil
import com.shannon.android.lib.util.ToastUtil
import com.shannon.openvoice.FunApplication
import com.shannon.openvoice.FunApplication.Companion.appViewModel
import com.shannon.openvoice.R
import com.shannon.openvoice.business.main.mine.account.FollowingAndFollowerActivity
import com.shannon.openvoice.model.*
import com.shannon.openvoice.network.apiService
import com.shannon.openvoice.network.convertResponse
import com.shannon.openvoice.network.gson
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.io.File

/**
 *
 * @ProjectName:    OpenVoice
 * @Package:        com.shannon.openvoice.business.main.mine
 * @ClassName:      MineViewModel
 * @Description:     java类作用描述
 * @Author:         czhen
 * @CreateDate:     2022/7/27 10:49
 */
class MineViewModel : BaseViewModel() {

    // 修改个人信息 头像
    val avatarData = MutableLiveData<String>()

    //修改个人信息  背景图
    val headerData = MutableLiveData<String>()

    /**
     * 公告列表
     */
    val announcementsLive = MutableLiveData<List<AnnouncementBean>>()
    fun listAnnouncements(with_dismissed: Boolean = true, owner: LifecycleOwner) {
        apiService.listAnnouncements(with_dismissed)
            .convertResponse()
            .funSubscribeRxLife(owner = owner,
                onNext = {
                    announcementsLive.postValue(it)
                }, onError = {
                    LogUtils.e("onError =-=  $it")
                })
    }

    /**
     * 黑名单列表
     */
    val blocksLive = MutableLiveData<List<TimelineAccount>>()
    fun listblocks(
        maxId: String? = null,
        onCallBack: (isSuccess: Boolean, linkHeader: String?) -> Unit
    ) {
        apiService.listblocks(maxId)
            .compose(observableToMain())
            .subscribe({
                if (it.isSuccessful && !isBlankPlus(it.body())) {
                    val linkHeader = it.headers()["Link"]
                    blocksLive.postValue(it.body())
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
     * 解锁黑名单
     */

    val unblockAccountLive: MutableLiveData<Pair<RelationshipBean, Int>> = MutableLiveData()

    fun unblockAccount(accountId: String, position: Int, owner: LifecycleOwner) {
        apiService.unblockAccount(accountId)
            .convertResponse()
            .funSubscribeRxLife(owner = owner, onNext = {
                unblockAccountLive.value = Pair(it, position)
            }, onError = {
                LogUtils.e("onError =-=  $it")
            })

    }

    /**
     * 通过ID获取Account数据
     */
    val accountLive = MutableLiveData<AccountBean>()
    fun getAccountData(accountId: String, owner: LifecycleOwner) {
        apiService.account(accountId)
            .convertResponse()
            .funSubscribeRxLife(owner = owner, onNext = {
                accountLive.postValue(it)
            }, onError = {
                LogUtils.e("onError =-=  $it")
            })
    }

    /**
     * 检查与给定账号的关系
     */
    val relationshipsLive = MutableLiveData<List<RelationshipBean>>()
    fun relationships(ids: List<String>, owner: LifecycleOwner) {
        apiService.relationships(ids)
            .convertResponse()
            .funSubscribeRxLife(owner = owner, onNext = {
                if (it.isNotEmpty())
                    relationshipsLive.postValue(it)
            }, onError = {
                LogUtils.e("onError =-=  $it")
            })
    }

    /**
     * 取消关注用户
     */
    val unfollowAccountLive = MutableLiveData<RelationshipBean>()
    fun unfollowAccount(accountId: String, owner: LifecycleOwner) {
        apiService.unfollowAccount(accountId)
            .convertResponse()
            .funSubscribeRxLife(owner = owner, onNext = {
                unfollowAccountLive.postValue(it)
            }, onError = {
                LogUtils.e("onError =-=  $it")
            })
    }

    /**
     * 关注用户
     */
    val followAccountLive = MutableLiveData<RelationshipBean>()
    fun followAccount(
        accountId: String,
        showReblogs: Boolean = true,
        notify: Boolean = false,
        owner: LifecycleOwner
    ) {
        apiService.followAccount(accountId, showReblogs, notify)
            .convertResponse()
            .funSubscribeRxLife(owner = owner, onNext = {
                followAccountLive.postValue(it)
            }, onError = {
                LogUtils.e("onError =-=  $it")
            })
    }


    /**
     * 获取用户关注的账号列表/用户粉丝列表
     */
    val accountFollowingFollowerLive = MutableLiveData<List<TimelineAccount>>()

    fun accountFollowingFollower(
        accountId: String,
        maxId: String? = null,
        userIdentitType: FollowingAndFollowerActivity.UserIdentitType,
        onCallBack: (isSuccess: Boolean, linkHeader: String?) -> Unit
    ) {

        if (userIdentitType == FollowingAndFollowerActivity.UserIdentitType.Following) {
            apiService.accountFollowing(accountId, maxId)
        } else {
            apiService.accountFollowers(accountId, maxId)
        }.compose(observableToMain())
            .subscribe({
                if (it.isSuccessful && !isBlankPlus(it.body())) {
                    val linkHeader = it.headers()["Link"]
                    accountFollowingFollowerLive.postValue(it.body())
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
     * 获取用户媒体数据
     */
    val accountStatusesOnlyMediaLive = MutableLiveData<List<StatusModel>>()
    fun accountStatusesOnlyMedia(accountId: String, maxId: String? = null, owner: LifecycleOwner) {
        apiService.accountStatuses(accountId, maxId, null, null, null, true, null, null)
            .convertResponse()
            .funSubscribeRxLife(owner = owner, onNext = {
                accountStatusesOnlyMediaLive.postValue(it)
            }, onError = {
                LogUtils.e("onError =-=  $it")
            })
    }

    /**
     * 修改用户信息
     */

    val accountUpdateCredentialsLive = MutableLiveData<AccountBean>()
    fun accountUpdateCredentials(
        displayName: RequestBody?,
        birthday: RequestBody?,
        note: RequestBody?,
        avatar: MultipartBody.Part?,
        header: MultipartBody.Part?,
        owner: LifecycleOwner
    ) {
        apiService.accountUpdateCredentials(displayName, avatar, header, note, birthday)
            .convertResponse()
            .funSubscribeRxLife(owner = owner, onNext = {
                accountUpdateCredentialsLive.postValue(it)
            }, onError = {
                LogUtils.e("onError =-=  $it")
            })
    }

    /**
     * 修改密码
     */
    fun updatePassword(
        old_password: String,
        password: String,
        password_confirmation: String,
        viewLifecycleOwner: LifecycleOwner,
        onCallback: (isSuccess: Boolean) -> Unit
    ) {
        val requestBody = JsonObject().let {
            it.add("user", JsonObject().let { node ->
                node.addProperty("old_password", old_password)
                node.addProperty("password", password)
                node.addProperty("password_confirmation", password_confirmation)
                node
            })
            it.toString().toRequestBody("application/json".toMediaTypeOrNull())
        }
        apiService.updatePassword(requestBody)
            .convertResponse()
            .funSubscribeRxLife(owner = viewLifecycleOwner, onNext = {
                LogUtils.i("subscribe =-=  $it")
//                appViewModel.backgroundPrompt(it)
                onCallback(true)
            }, onError = {
                LogUtils.e("onError =-=  $it")
                onCallback(false)
            })
    }

    /**
     * 用户注销
     */
    fun accountsSuspend(activity: Activity) {
        apiService.accountsSuspend()
            .convertResponse()
            .funSubscribe(showLoading = false,
                onNext = {
                    appViewModel.jump2MainPage(activity)
                }, onError = {
                    LogUtils.e("onError =-=  $it")
                })
    }
}