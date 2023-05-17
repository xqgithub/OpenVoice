package com.shannon.openvoice.business.timeline

import android.os.Handler
import android.os.Looper
import android.text.TextUtils
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.SavedStateHandle
import com.shannon.android.lib.base.viewmodel.BaseViewModel
import com.shannon.android.lib.player.media.MediaData
import com.shannon.openvoice.business.timeline.listener.AdapterLogicListener
import com.shannon.openvoice.model.DeletedStatus
import com.shannon.openvoice.model.StatusContext
import com.shannon.openvoice.model.StatusModel
import com.shannon.openvoice.network.apiService
import com.shannon.openvoice.network.convertLink
import com.shannon.openvoice.network.convertResponse
import com.shannon.openvoice.util.CloudStorageHelper
import io.reactivex.rxjava3.core.Observable
import retrofit2.Response
import timber.log.Timber
import java.util.*
import kotlin.collections.ArrayList

/**
 *
 * @ProjectName:    OpenVoice
 * @Package:        com.shannon.openvoice.business.timeline
 * @ClassName:      TimelineViewModel
 * @Description:     java类作用描述
 * @Author:         czhen
 * @CreateDate:     2022/8/1 10:00
 */
class TimelineViewModel(private val savedStateHandle: SavedStateHandle) : BaseViewModel() {

    companion object {
        const val KEY_DATA = "status"
        const val LIMIT = 30
    }

    private val cloudStorageHelper by lazy { CloudStorageHelper() }

    private lateinit var timelineKind: Kind
    var hashtags = emptyList<String>()
        private set
    var accountId: String? = null
        private set
    var modelId: Long = 0L
        private set
    private lateinit var viewLifecycleOwner: LifecycleOwner
    private lateinit var dataAdapter: AdapterLogicListener
    private val emptyVoice = arrayListOf<String>()
    fun init(
        viewLifecycleOwner: LifecycleOwner,
        kind: Kind,
        dataAdapter: AdapterLogicListener,
        tags: List<String>,
        accountId: String?,
        modelId: Long
    ) {
        this.viewLifecycleOwner = viewLifecycleOwner
        timelineKind = kind
        this.dataAdapter = dataAdapter
        hashtags = tags
        this.accountId = accountId
        this.modelId = modelId
    }

    private fun innerData(): List<StatusModel> {
        return dataAdapter.getLogicData()
    }


    private var netMaxId: String? = null
    fun fetchTimeline(
        isRefresh: Boolean,
        isUserInput: Boolean = true,
        onResult: (List<StatusModel>) -> Unit
    ) {
        if (savedStateHandle.contains(KEY_DATA) && !isUserInput) {
            Timber.e(" ==================== SaveInstanceState ========================= ")
            val data = savedStateHandle.get<ArrayList<StatusModel>>(KEY_DATA)
            emptyVoice.clear()
            data?.run {
                val playingIndex = indexOfFirst { it.isPlaying }
                if (playingIndex >= 0)
                    this[playingIndex] = this[playingIndex].copy(isPlaying = false)
                onResult(this)
            }
            savedStateHandle.remove<List<StatusModel>>(KEY_DATA)
        } else {
            val maxId =
                if (innerData().isNotEmpty() && !isRefresh) netMaxId else null
            if (maxId == null) {
                emptyVoice.clear()
                stopTimer()
            }
//            val sinceId =
//                if (innerData().isNotEmpty() && isRefresh) innerData().first().id else null
            fetchStatusesForKind(maxId = maxId, null)
                .convertLink { netMaxId = it }
                .convertResponse()
                .funSubscribeNotLoading {
                    onResult(it)
                    findNonGenerated(it)
                    if (isRefresh && emptyVoice.isNotEmpty() && viewLifecycleOwner.lifecycle.currentState == Lifecycle.State.RESUMED) {
                        scheduleTimer()
                    }
                }
        }
    }

    fun scheduleTtsTimer() {
        if (timer == null && emptyVoice.isNotEmpty()) {
            scheduleTimer()
        }
    }

    private fun fetchStatusesForKind(
        maxId: String?,
        sinceId: String?
    ): Observable<Response<List<StatusModel>>> {
        return when (timelineKind) {
            Kind.HOME -> apiService.homeTimeline(maxId, sinceId, LIMIT)
            Kind.PUBLIC_LOCAL -> apiService.publicTimeline(
                local = true,
                maxId = maxId,
                sinceId = sinceId,
                limit = LIMIT
            )
            Kind.TAG -> {
                val firstHashtag = if (hashtags.isNotEmpty()) hashtags[0] else ""
                val additionalHashtags =
                    if (hashtags.isNotEmpty()) hashtags.subList(1, hashtags.size) else hashtags
                apiService.hashtagTimeline(
                    firstHashtag,
                    null,
                    maxId,
                    sinceId,
                    LIMIT
                )
            }
            Kind.FAVOURITES -> {
                apiService.favourites(maxId, sinceId, LIMIT)
            }
            Kind.USER -> {
                apiService.accountStatuses(
                    accountId!!,
                    maxId,
                    sinceId,
                    LIMIT,
                    true,
                    null,
                    null,
                    null
                )
            }
            Kind.USER_WITH_REPLIES -> {
                apiService.accountStatuses(
                    accountId!!,
                    maxId,
                    sinceId,
                    LIMIT,
                    false,
                    null,
                    null,
                    excludeReblogs = true
                )
            }
            Kind.USER_PINNED -> {
                apiService.accountStatuses(
                    accountId!!,
                    maxId,
                    sinceId,
                    LIMIT,
                    true,
                    null,
                    true,
                    excludeReblogs = null
                )
            }
            Kind.MODEL_ASS -> {
                apiService.modelStatuses(modelId, maxId, sinceId, null, LIMIT)
            }
            else -> {
                apiService.homeTimeline(maxId = maxId, limit = LIMIT)
            }
        }
    }

    fun reblog(reblog: Boolean, statusId: String, onResult: (StatusModel?) -> Unit) {
        val response =
            if (reblog) apiService.reblogStatus(statusId) else apiService.unreblogStatus(statusId)
        actionResult(response, onResult)
    }

    fun favourite(favourite: Boolean, statusId: String, onResult: (StatusModel?) -> Unit) {
        val response =
            if (favourite) apiService.favouriteStatus(statusId) else apiService.unfavouriteStatus(
                statusId
            )
        actionResult(response, onResult)
    }

    fun pin(pin: Boolean, statusId: String, onResult: (StatusModel?) -> Unit) {
        val response =
            if (pin) apiService.pinStatus(statusId) else apiService.unpinStatus(statusId)
        actionResult(response, onResult)
    }

    fun deleteStatus(statusId: String, onResult: (DeletedStatus) -> Unit) {
        apiService.deleteStatus(statusId)
            .convertResponse()
            .funSubscribe {
                onResult(it)
            }
    }

    fun blockAccount(accountId: String, onResult: () -> Unit) {
        apiService.blockAccount(accountId)
            .convertResponse()
            .funSubscribe { onResult() }
    }

    private fun actionResult(
        response: Observable<Response<StatusModel>>,
        onResult: (StatusModel?) -> Unit
    ) {
        response
            .convertResponse()
            .funSubscribe(onNext = {
                onResult(it)
            }, onError = {
                onResult(null)
            })
    }

    fun buyModel(id: Long, onResult: () -> Unit) {
        apiService.purchaseVoiceModel(id)
            .convertResponse()
            .funSubscribe {
                onResult()
            }
    }


    fun getStatus(id: String, onResult: (StatusModel) -> Unit) {
        apiService.getStatus(id)
            .convertResponse()
            .funSubscribe {
                onResult(it)
            }
    }

    fun fetchStatusContext(id: String, onResult: (StatusContext) -> Unit) {
        apiService.statusContext(id)
            .convertResponse()
            .funSubscribe {
                onResult(it)
            }
    }

    fun findNonGenerated(data: List<StatusModel>) {
        data.forEach {
            if (!it.actionableStatus.ttsGenerated && !emptyVoice.contains(it.id)) {
                emptyVoice.add(it.id)
            }
        }
    }

    fun getCloudPath(ossId: String) = cloudStorageHelper.getResourceUrl(ossId)

    fun following(id: String, onError: () -> Unit = {}, onResult: (Boolean) -> Unit) {
        apiService.relationships(arrayListOf(id))
            .convertResponse()
            .funSubscribeNotLoading(onError = { onError() }, {
                if (it.isNotEmpty()) onResult(it[0].following)
            })
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

    private var timer: Timer? = null

    fun fetchVoice() {
        if (emptyVoice.isEmpty()) {
            stopTimer()
        } else {
            apiService.ttsGenerated(emptyVoice)
                .convertResponse()
                .funSubscribe(showLoading = false,
                    onNext = {
                        it.forEach { result ->
                            if (result.ttsGenerated) {
                                emptyVoice.remove(result.id)
                                dataAdapter.updateVoice(result.id, result.ossId)
                            }
                        }

                        if (emptyVoice.isEmpty() || it.isEmpty()) stopTimer()
                    })
        }
    }

    fun scheduleTimer() {
        stopTimer()
        timer = Timer()
        timer?.schedule(object : TimerTask() {
            override fun run() {
                fetchVoice()
            }
        }, 2000L, 2000L)
    }

    private fun stopTimer() {
        timer?.cancel()
        timer = null
    }

    override fun onCleared() {
        super.onCleared()
        stopTimer()
    }

    /**
     * 保存数据，在应用重启(暗黑模式切换、语言环境切换)后能够恢复状态
     * @param data List<StatusModel>
     */
    fun onSaveInstanceState(data: List<StatusModel>) {
        savedStateHandle.set(KEY_DATA, data)
    }


    enum class Kind {
        DEFAULT,
        ANY,
        HOME,//关注
        PUBLIC_LOCAL,//推荐
        TAG,//话题
        FAVOURITES,//点赞
        USER,//用户
        USER_WITH_REPLIES,//用户-回复
        USER_PINNED, //用户-置顶
        MODEL_ASS, //模型关联
        SEARCH,//搜索
        DETAIL,


        MODEL_AUDITION,//我的模型
        MODEL_AUDITION_OFFICIAL,//官方模型
        MODEL_SELL,//出售
        MODEL_USED,//使用
        SEARCH_TOPIC,//搜索话题
        SEARCH_TOPIC_VOICEOVER,//搜索话题-推荐声文
        SEARCH_VOICEOVER,//搜索声文
        SEARCH_MODEL,//搜索模型
        SEARCH_MODEL_VOICEOVER,//搜索模型-推荐声文

    }

}