package com.shannon.openvoice.business.compose

import android.net.Uri
import android.os.Handler
import android.os.Looper
import android.text.TextUtils
import androidx.core.net.toUri
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.shannon.android.lib.base.viewmodel.BaseViewModel
import com.shannon.android.lib.extended.randomAlphanumericString
import com.shannon.openvoice.BuildConfig
import com.shannon.openvoice.business.draft.DraftHelper
import com.shannon.openvoice.business.main.mine.account.AccountManager
import com.shannon.openvoice.business.timeline.TimelineViewModel
import com.shannon.openvoice.db.DraftAttachment
import com.shannon.openvoice.extended.update
import com.shannon.openvoice.extended.updateAndGet
import com.shannon.openvoice.model.*
import com.shannon.openvoice.network.apiService
import com.shannon.openvoice.network.convertResponse
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.disposables.Disposable
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import org.greenrobot.eventbus.EventBus
import timber.log.Timber
import java.util.concurrent.TimeUnit

/**
 *
 * @Package:        com.shannon.openvoice.business.compose
 * @ClassName:      ComposeViewModel
 * @Description:     作用描述
 * @Author:         czhen
 * @CreateDate:     2022/8/10 10:13
 */
class ComposeViewModel : BaseViewModel() {

    private val mediaUploader by lazy { MediaUploader() }
    val voiceModels = arrayListOf<VoiceModelResult>()
    val mediaLive = MutableStateFlow<List<QueuedMedia>>(arrayListOf())
    val mediaButtonEnabled = MutableLiveData(true)
    private val mediaToJob = mutableMapOf<Int, Job>()

    private val draftHelper by lazy { DraftHelper() }
    private var draftId: Int = 0
    private var inReplyToId: String? = null
    private var inReplyAuthor: String? = null

    fun searchAutocompleteSuggestions(token: String): List<AutoCompleteResult> {
        return when (token[0]) {
            '@' -> {
                apiService.searchObservable(token.substring(1), "accounts", limit = 10)
                    .convertResponse()
                    .blockingSingle()
                    .accounts
                    .map { AccountResult(it) }
            }
            '#' -> {
                apiService.searchObservable(token.substring(1), "hashtags", limit = 10)
                    .convertResponse()
                    .blockingSingle()
                    .hashtags
                    .map { HashtagResult(it) }
            }
            else -> emptyList()
        }
    }


    fun fetchVoiceModels(onResult: (List<VoiceModelResult>) -> Unit) {
        if (voiceModels.isNotEmpty()) {
            onResult(voiceModels)
        } else {
            apiService.getVoiceModels(0, 1000)
                .convertResponse()
                .funSubscribeNotLoading {
                    val list = it.filter { bean ->
                        TextUtils.equals(bean.status, "unused") || TextUtils.equals(
                            bean.status,
                            "used"
                        )
                    }
                    onResult(list)
                    voiceModels.addAll(list)
                }
        }
    }

    fun activateVoiceModel(id: Long, onResult: (VoiceModelResult) -> Unit) {
        apiService.activateVoiceModel(id)
            .convertResponse()
            .funSubscribe {
                onResult(it)
            }
    }

    fun sendVoiceCover(
        content: String,
        voiceModelId: Long,
        visibility: StatusModel.Visibility,
        onResult: (String) -> Unit
    ) {
        val mediaIds = mutableListOf<String>()

        mediaLive.value.forEach { mediaItem ->
            if (mediaItem.uploadPercent == -1) {
                mediaIds.add(mediaItem.id!!)
            }
        }

        val sendVoiceCover = NewVoiceCover(
            content,
            inReplyToId,
            visibility.serverString(),
            mediaIds,
            voiceModelId
        )

        apiService.createStatus(
            BuildConfig.HOST_URL,
            randomAlphanumericString(16),
            sendVoiceCover
        )
            .convertResponse()
            .funSubscribe(onError = {
                saveDraft(content, voiceModelId, visibility)
                onResult("")
            }, onNext = {
//                intervalTime = System.currentTimeMillis()
//                queryTtsResult(it, onResult)
                deleteDraft()
                EventBus.getDefault().post(
                    EventComposeStatus(
                        TimelineViewModel.Kind.ANY,
                        if (inReplyToId == null) EventComposeStatus.Type.CREATE else EventComposeStatus.Type.REPLAY,
                        it
                    )
                )
                onResult(it.id)
            })
    }


    private val intervalHandler = Handler(Looper.getMainLooper())
    private var intervalTime = 0L
    private fun queryTtsResult(model: StatusModel, onResult: (String) -> Unit) {
        if (System.currentTimeMillis() - intervalTime > 10000) {
            onResult("")
        } else {
            apiService.ttsGenerated(model.id)
                .convertResponse()
                .funSubscribeNotLoading(onError = {
                    onResult("")
                }, onNext = { status ->
                    if (status.ttsGenerated) {
                        deleteDraft()
                        EventBus.getDefault().post(
                            EventComposeStatus(
                                TimelineViewModel.Kind.ANY,
                                if (inReplyToId == null) EventComposeStatus.Type.CREATE else EventComposeStatus.Type.REPLAY,
                                model
                            )
                        )
                        onResult(model.id)
                    } else {
                        intervalHandler.postDelayed({ queryTtsResult(model, onResult) }, 3000)
                    }
                })
        }
    }

    fun addMediaToQueue(
        type: QueuedMedia.Type,
        uri: Uri,
        mediaSize: Long
    ) {
        val mediaItem = mediaLive.updateAndGet { mediaValue ->
            val mediaItem = QueuedMedia(
                localId = (mediaLive.value.maxOfOrNull { it.localId } ?: 0) + 1,
                uri = uri,
                type = type,
                mediaSize = mediaSize,
            )
            mediaValue + mediaItem
        }.last()
        mediaEnabled()

        mediaUploader.compress(mediaItem, onSuccess = { compressUri, size ->
            mediaToJob[mediaItem.localId] = viewModelScope.launch {
                mediaUploader.uploadMedia(mediaItem.copy(uri = compressUri, mediaSize = size))
                    .catch {
                        Timber.e(it.message)
                    }
                    .collect { event ->
                        val item = mediaLive.value.find { it.localId == mediaItem.localId }
                            ?: return@collect
                        val newMediaItem = when (event) {
                            is UploadEvent.ProgressEvent ->
                                item.copy(uploadPercent = event.percentage)
                            is UploadEvent.FinishedEvent ->
                                item.copy(id = event.mediaId, uploadPercent = -1)
                        }
                        mediaLive.update {
                            it.map { mediaItem ->
                                if (mediaItem.localId == newMediaItem.localId) {
                                    newMediaItem
                                } else {
                                    mediaItem
                                }
                            }
                        }
                    }
            }
        })

    }

    fun removeMediaJob(localId: Int) {
        mediaToJob[localId]?.cancel()
        mediaLive.update { values ->
            values.filter { it.localId != localId }
        }
        mediaEnabled()
    }

    fun waitingContinueUpload(): Boolean {
        val job = mediaLive.value.find { it.id.isNullOrEmpty() }
        return job != null
    }


    private fun mediaEnabled() {
        mediaLive.value.run {
            mediaButtonEnabled.value = if (size == 0) true else
                !(first().type == QueuedMedia.Type.VIDEO || (first().type == QueuedMedia.Type.IMAGE && size == 4))
        }
    }

    fun saveDraft(content: String, voiceModelId: Long, visibility: StatusModel.Visibility) {
        viewModelScope.launch(Dispatchers.IO) {
//            val mediaUris: MutableList<String> = mutableListOf()
//            mediaLive.value.forEach { item ->
//                mediaUris.add(item.uri.toString())
//            }
            draftId = draftHelper.saveDraft(
                draftId = draftId,
                accountId = AccountManager.accountManager.getAccountId(),
                inReplyToId,
                inReplyAuthor,
                content, visibility, voiceModelId, mediaLive.value, false
            )
        }
    }

    fun deleteDraft() {
        viewModelScope.launch(Dispatchers.IO) {
            if (draftId > 0) {
                draftHelper.deleteDraft(draftId)
            }
        }

    }

    fun setup(options: ComposeOptions) {
        draftId = options.draftId ?: 0
        inReplyToId = options.inReplyToId
        inReplyAuthor = options.replyingStatusAuthor

        options.draftAttachments?.forEach {
            val mediaType =
                if (it.type == DraftAttachment.Type.IMAGE) QueuedMedia.Type.IMAGE else QueuedMedia.Type.VIDEO
            if (it.serverId == null) {
                addMediaToQueue(mediaType, it.uri, it.mediaSize)
            } else {
                addUploadedMedia(it.serverId, mediaType, it.uri)
            }
        }
        options.mediaAttachments?.forEach {
            val mediaType =
                if (it.type == Attachment.Type.IMAGE) QueuedMedia.Type.IMAGE else QueuedMedia.Type.VIDEO
            addUploadedMedia(it.id, mediaType, it.url.toUri())
        }
    }

    private fun addUploadedMedia(
        id: String?,
        type: QueuedMedia.Type,
        uri: Uri,
    ) {
        mediaLive.update { mediaValue ->
            val mediaItem = QueuedMedia(
                localId = (mediaValue.maxOfOrNull { it.localId } ?: 0) + 1,
                uri = uri,
                type = type,
                mediaSize = 0,
                uploadPercent = -1,
                id = id,
            )
            mediaValue + mediaItem
        }
    }
}