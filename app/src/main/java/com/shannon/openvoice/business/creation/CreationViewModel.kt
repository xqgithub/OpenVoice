package com.shannon.openvoice.business.creation

import android.content.Context
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.MutableLiveData
import com.amazonaws.mobileconnectors.s3.transferutility.TransferListener
import com.amazonaws.mobileconnectors.s3.transferutility.TransferState
import com.blankj.utilcode.util.LogUtils
import com.shannon.android.lib.base.viewmodel.BaseViewModel
import com.shannon.android.lib.extended.isLessThanOrEqual
import com.shannon.android.lib.player.media.MediaData
import com.shannon.android.lib.util.FileUtil
import com.shannon.android.lib.util.ToastUtil
import com.shannon.openvoice.FunApplication
import com.shannon.openvoice.R
import com.shannon.openvoice.business.main.mine.account.AccountManager
import com.shannon.openvoice.business.player.MediaInfo
import com.shannon.openvoice.business.player.PlayerHelper
import com.shannon.openvoice.model.*
import com.shannon.openvoice.network.apiService
import com.shannon.openvoice.network.convertResponse
import com.shannon.openvoice.network.convertResponseCode
import com.shannon.openvoice.util.CloudStorageHelper
import com.shannon.openvoice.util.CosCloud
import com.shannon.openvoice.util.audio.GlobalConfig.*
import com.shannon.openvoice.util.audio.PcmToWavUtil
import com.shannon.openvoice.util.aws.AwsUtil
import com.tencent.cos.xml.exception.CosXmlClientException
import com.tencent.cos.xml.exception.CosXmlServiceException
import com.tencent.cos.xml.listener.CosXmlResultListener
import com.tencent.cos.xml.model.CosXmlRequest
import com.tencent.cos.xml.model.CosXmlResult
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.disposables.Disposable
import io.reactivex.rxjava3.schedulers.Schedulers
import org.greenrobot.eventbus.EventBus
import retrofit2.http.Field
import timber.log.Timber
import java.io.File
import java.lang.Exception
import java.util.*
import java.util.concurrent.TimeUnit

/**
 *
 * @ProjectName:    OpenVoice
 * @Package:        com.shannon.openvoice.business.creation
 * @ClassName:      CreationViewModel
 * @Description:     java类作用描述
 * @Author:         czhen
 * @CreateDate:     2022/7/28 11:29
 */
class CreationViewModel : BaseViewModel() {

    lateinit var saveVoiceDir: String
    val voicePathList = arrayListOf<String>()
    val wholeVoicePathList = arrayListOf<String>()

    val progressLive = MutableLiveData(0)
    val currentProgress: Int
        get() = progressLive.value!!

    val currencyLive = MutableLiveData("ETH")

    private var timerDispose: Disposable? = null

    fun nextProgress() {
        val progress = currentProgress + 1
        progressLive.value = progress
    }

    fun previousProgress() {
        val progress = currentProgress - 1
        progressLive.value = progress
    }

    private fun countdown(over: () -> Unit) {
        disposeCountdown()
        timerDispose = Observable.timer(40, TimeUnit.SECONDS, AndroidSchedulers.mainThread())
            .subscribe { over() }
        saveDisposable(timerDispose!!)
    }

    private fun disposeCountdown() {
        timerDispose?.run {
            if (!isDisposed) {
                dispose()
            }
        }
    }

    fun uploadVoice(
        price: String,
        address: String,
        royaltiesFee: String,
        inviteId: Int,
        onResult: (VoiceModelResult) -> Unit
    ) {
        val cos = CosCloud(FunApplication.getInstance())
        val resultList = arrayListOf<String>()
        return Observable.create<List<String>> { emitter ->
            val pcmToWavUtil = PcmToWavUtil(SAMPLE_RATE_INHZ, CHANNEL_CONFIG, AUDIO_FORMAT)
            voicePathList.forEach { srcPath ->
                val need2Wav = srcPath.contains("pcm")
                val wavPath = srcPath.replace("pcm", "wav")
                if (need2Wav)
                    pcmToWavUtil.pcmToWav(srcPath, wavPath)
                cos.uploadVoice(wavPath, FileUtil.getName(wavPath))
                    .setCosXmlResultListener(object : CosXmlResultListener {
                        override fun onSuccess(p0: CosXmlRequest?, p1: CosXmlResult?) {
                            var accessUrl = p1?.accessUrl ?: ""
                            accessUrl = accessUrl.substring(accessUrl.lastIndexOf("/") + 1)
                            Timber.e("onSuccess: $accessUrl ")
                            resultList.add(accessUrl)
                            if (resultList.size == voicePathList.size) {
                                resultList.sortWith { o1, o2 -> if (o2.isLessThanOrEqual(o1)) 1 else -1 }
                                emitter.onNext(resultList)
                                emitter.onComplete()
                            }
                        }

                        override fun onFail(
                            p0: CosXmlRequest?,
                            p1: CosXmlClientException?,
                            p2: CosXmlServiceException?
                        ) {
                            if (p2 != null)
                                emitter.onError(p2)
                            else if (p1 != null)
                                emitter.onError(p1)
                        }
                    })
            }
        }
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .funSubscribeNotLoading {
//                onResult(VoiceModelResult(0L, price, "", "original", "in_progress", false, "", ""))
                apiService.postVoiceModel(
                    VoiceModelRequest(
                        it,
                        price,
                        currencyLive.value,
                        address,
                        royaltiesFee,
                        inviteId
                    )
                )
                    .convertResponse()
                    .funSubscribeNotLoading { result ->
                        EventBus.getDefault().post(EventRecorder(result))
                        onResult(result)
                    }
            }
    }

    fun uploadVoiceToS3(
        price: String,
        address: String,
        royaltiesFee: String,
        inviteId: Int,
        onResult: (VoiceModelResult) -> Unit
    ) {
        val awsUtil = AwsUtil(FunApplication.getInstance().applicationContext)
        val resultList = arrayListOf<String>()
        return Observable.create<List<String>> { emitter ->
            val pcmToWavUtil = PcmToWavUtil(SAMPLE_RATE_INHZ, CHANNEL_CONFIG, AUDIO_FORMAT)
            voicePathList.forEach { srcPath ->
                val need2Wav = srcPath.contains("pcm")
                val wavPath = srcPath.replace("pcm", "wav")
                if (need2Wav)
                    pcmToWavUtil.pcmToWav(srcPath, wavPath)
                awsUtil.uploadVoice(wavPath, FileUtil.getName(wavPath))
                    .setTransferListener(object : TransferListener {
                        override fun onStateChanged(id: Int, state: TransferState) {
                            if (state == TransferState.COMPLETED) {
                                val key = "${AwsUtil.QCLOUD_BUCKET_ID}/${FileUtil.getName(wavPath)}"
                                LogUtils.i("=-= 文件上传完成 = $key")
                                resultList.add(key)
                                if (resultList.size == voicePathList.size) {
                                    resultList.sortWith { o1, o2 -> if (o2.isLessThanOrEqual(o1)) 1 else -1 }
                                    emitter.onNext(resultList)
                                    emitter.onComplete()
                                }
                            }
                        }

                        override fun onProgressChanged(
                            id: Int,
                            bytesCurrent: Long,
                            bytesTotal: Long
                        ) {

                        }

                        override fun onError(id: Int, ex: Exception) {
                            //                            LogUtils.e("=_= id---$id,ex---$ex")
                            emitter.onError(ex)
                        }
                    })
            }
        }
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .funSubscribeNotLoading {
//                onResult(VoiceModelResult(0L, price, "", "original", "in_progress", false, "", ""))
                apiService.postVoiceModel(
                    VoiceModelRequest(
                        it,
                        price,
                        currencyLive.value,
                        address,
                        royaltiesFee,
                        inviteId
                    )
                )
                    .convertResponse()
                    .funSubscribeNotLoading { result ->
                        EventBus.getDefault().post(EventRecorder(result))
                        onResult(result)
                    }
            }
    }

    fun fetchVoiceModels(
        pageNum: Int,
        pageSize: Int = 0,
        status: String = "",
        onResult: (List<VoiceModelResult>) -> Unit
    ) {
        apiService.getVoiceModels(offset = pageSize.coerceAtMost(pageNum * 10), status = status)
            .convertResponse()
            .funSubscribeNotLoading {
                onResult(it)
            }
    }

    fun activateVoiceModel(id: Long, onResult: (VoiceModelResult) -> Unit) {
        apiService.activateVoiceModel(id)
            .convertResponse()
            .funSubscribe {
                onResult(it)
            }
    }

    fun deleteVoiceModel(id: Long, onResult: () -> Unit) {
        apiService.deleteVoiceModel(id)
            .convertResponseCode()
            .funSubscribe {
                onResult()

            }
    }

    fun createSaveVoiceDir(context: Context) {
        ///data/user/0/xxx.xxx.xxx/files/voice
        // val dir = FileUtil.getAppInternalFilesDir(this).path.plus(File.separator).plus("voice")
        val dir = context.getExternalFilesDir(null)?.path.plus(File.separator).plus("voice")
        Timber.e("createHistoryMediaDir == $dir")
        FileUtil.createDir(dir, false)
        saveVoiceDir = dir
    }

    fun fetchLikes(
        otherUser: Boolean,
        accountId: String,
        pageNum: Int,
        onResult: (List<Likes>) -> Unit
    ) {
        if (otherUser) {
            apiService.accountVoiceModel(accountId, pageNum * 10)
                .convertResponse()
                .funSubscribeNotLoading {
                    onResult(it.map { m ->
                        Likes(
                            0L,
                            m.name,
                            m.isLiked,
                            m.ownerAccount,
                            Date(),
                            m.activated,
                            m.isModelOwner,
                            m.id,
                            m.price,
                            m.currency,
                            m.isOfficial,
                            usageCount = m.usageCount
                        )
                    })
                }
        } else {
            apiService.accountLikes(accountId, pageNum * 10)
                .convertResponse()
                .funSubscribeNotLoading {
                    val result = arrayListOf<Likes>()
                    it.forEach { work ->
                        work.model?.let { m ->
                            result.add(
                                Likes(
                                    m.id,
                                    m.name,
                                    m.isLiked,
                                    m.ownerAccount,
                                    work.likeAt,
                                    m.activated,
                                    m.isModelOwner,
                                    m.id,
                                    m.price,
                                    m.currency,
                                    m.isOfficial,
                                    usageCount = m.usageCount
                                )
                            )
                        }
                    }
                    onResult(result)
                }
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

    fun updateVoiceModel(
        modelId: Long,
        price: String? = null,
        currency: String? = null,
        royaltiesFee: String? = null,
        payload: String? = null,
        saleCycle: String? = null,
        onResult: (VoiceModelResult) -> Unit
    ) {
        apiService.updateVoiceModel(modelId, price, currency, royaltiesFee, payload, saleCycle)
            .convertResponse()
            .funSubscribeNotLoading {
                onResult(it)
            }
    }

    val modelDetailLive = MutableLiveData<ModelDetail>()
    fun modelDetail(modelId: Long) {
        apiService.modelDetail(modelId)
            .convertResponse()
            .funSubscribe {
                modelDetailLive.value = it
            }
    }

    fun modelDetail(modelId: Long, onResult: (ModelDetail) -> Unit) {
        apiService.modelDetail(modelId)
            .convertResponse()
            .funSubscribeNotLoading {
                onResult(it)
            }
    }


    fun changeModelDetail(mode: ModelDetail) {
        modelDetailLive.value = mode
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

    fun voiceModelLikes(modelId: Long, pageNum: Int, onResult: (List<ModelLikes>) -> Unit) {
        apiService.voiceModelLikes(modelId, pageNum * 10)
            .convertResponse()
            .funSubscribeNotLoading {
                onResult(it)
//                val idArrays = it.map { like -> like.account.id }
//                apiService.relationships(idArrays)
//                    .convertResponse()
//                    .funSubscribeNotLoading { relationships ->
//                        val likes = it.toMutableList()
//                        if (relationships.isNotEmpty()) {
//                            relationships.forEach { relationship ->
//                                val likeIndex = likes.indexOfFirst { like ->
//                                    like.account.id == relationship.id
//                                }
//                                if (likeIndex != -1) {
//                                    val like = likes[likeIndex]
//                                    likes[likeIndex] = like.copy(isFollow = relationship.following)
//                                }
//                            }
//                        }
//                        onResult(likes)
//                    }
            }
    }

    fun accountTradings(pageNum: Int, onResult: (List<Tradings>) -> Unit) {
        apiService.accountTradings(AccountManager.accountManager.getAccountId(), pageNum * 10)
            .convertResponse()
            .funSubscribeNotLoading {
                onResult(it)
            }
    }

    fun voiceModelTradings(modelId: Long, pageNum: Int, onResult: (List<Tradings>) -> Unit) {
        apiService.voiceModelTradings(modelId, pageNum * 10)
            .convertResponse()
            .funSubscribeNotLoading {
                onResult(it)
            }
    }

    fun modelStatus(modelId: Long, maxId: String?, onResult: (List<MediaInfo>) -> Unit) {
        apiService.modelStatuses(modelId, maxId, null, null, 30)
            .convertResponse().funSubscribe {
                if (it.isNotEmpty()) {
                    val mediaList = PlayerHelper.convert2MediaInfo(it)
                    if (mediaList.isNotEmpty()) {
                        onResult(mediaList)
                    } else {
                        modelStatus(modelId, it.last().id, onResult)
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

    /**
     *  录音文件无效
     * @return Boolean
     */
    fun invalidFile(): Boolean {
        val length = File(voicePathList[0]).length()
        Timber.e("invalidFile: size = ${voicePathList.size} ; length = $length")
        return voicePathList.isEmpty() || File(voicePathList[0]).length() == 0L
    }

    fun chainCurrency() {
        apiService.chainCurrency()
            .convertResponse()
            .funSubscribe {

            }
    }

    /**
     * 获取官方语音模型分类列表
     */
    fun voiceModelsOfficialCategoires(
        viewLifecycleOwner: LifecycleOwner,
        onCallback: (beans: List<OfficalModelLableBean>) -> Unit
    ) {
        apiService.voiceModelsOfficialCategoires()
            .convertResponse()
            .funSubscribeRxLife(
                showLoading = false,
                owner = viewLifecycleOwner,
                onNext = {
                    onCallback(it)
                }, onError = {
                    LogUtils.e("onError =-=  $it")
                })
    }

    /**
     * 获取官方语音模型列表
     */
    fun voiceModelsOfficialList(
        offset: Int = 0,
        categoryId: Int,
        onCallback: (beans: List<VoiceModelResult>) -> Unit
    ) {
        apiService.voiceModelsOfficialList(offset = offset * 10)
            .convertResponse()
            .funSubscribe(
                showLoading = false,
                onNext = {
                    onCallback(it)
                },
                onError = {
                    LogUtils.e("onError =-=  $it")
                }
            )
    }

    /**
     * 获取当前使用中语音模型
     */
    fun voiceModelsActivited(
        onCallback: (bean: VoiceModelResult) -> Unit
    ) {
        apiService.voiceModelsActivited()
            .convertResponse()
            .funSubscribe(
                showLoading = false,
                onNext = {
                    onCallback(it)
                },
                onError = {
                    LogUtils.e("onError =-=  $it")
                }
            )


//            .funSubscribeRxLife(
//                showLoading = false,
//                owner = viewLifecycleOwner,
//                onNext = {
//                    onCallback(it)
//                },
//                onError = {
//                    LogUtils.e("onError =-=  $it")
//                }
//            )
    }

    fun voiceHasContent(): Boolean {
        return if (voicePathList.isNotEmpty()) {
            val path = voicePathList.last()
            FileUtil.getFileSize(path) != -1L
        } else {
            false
        }
    }
}