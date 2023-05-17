package com.shannon.openvoice.util

import android.content.Context
import android.util.Log
import com.tencent.cos.xml.CosXmlServiceConfig
import com.tencent.cos.xml.CosXmlSimpleService
import com.tencent.cos.xml.model.`object`.PutObjectRequest
import com.tencent.cos.xml.transfer.COSXMLUploadTask
import com.tencent.cos.xml.transfer.TransferConfig
import com.tencent.cos.xml.transfer.TransferManager
import com.tencent.qcloud.core.auth.ShortTimeCredentialProvider
import timber.log.Timber

/**
 *
 * @ProjectName:    mastodon
 * @Package:        com.keylesspalace.tusky.util
 * @ClassName:      CosCloud
 * @Description:     java类作用描述
 * @Author:         czhen
 * @CreateDate:     2022/6/15 15:08
 */
class CosCloud(context: Context) : CloudResourceInterface {

    companion object {
        //https://voice-project-1304083978.cos.ap-guangzhou.myqcloud.com/voice_sample_108509534303491119_1655888310224_1.pcm
        private const val QCLOUD_SECRET_ID = "AKIDQkpkZ82w0pYHm2BcbfbIn5C62ub3ZDuM"
        private const val QCLOUD_SECRET_KEY = "NWRBgwypWx5yf8UpxQ9ZlpeDXdT6bzHL"
        private const val QCLOUD_REGION = "ap-guangzhou"
        private const val QCLOUD_BUCKET_ID = "voice-project-1304083978"
    }

    private val transferManager: TransferManager
    private val serviceConfig: CosXmlServiceConfig
    private val cosService: CosXmlSimpleService

    init {
        val gredentialProvider =
            ShortTimeCredentialProvider(QCLOUD_SECRET_ID, QCLOUD_SECRET_KEY, 300)
        serviceConfig = CosXmlServiceConfig.Builder()
            .setRegion(QCLOUD_REGION)
            .isHttps(true)
            .builder()
        cosService = CosXmlSimpleService(context, serviceConfig, gredentialProvider)
        val transferConfig = TransferConfig.Builder().build()
        transferManager = TransferManager(cosService, transferConfig)
    }

    fun uploadVoice(srcPath: String, cosPath: String): COSXMLUploadTask {
        return transferManager.upload(QCLOUD_BUCKET_ID, cosPath, srcPath, null)
    }

    override fun getResourceUrl(key: String): String {
        val request = PutObjectRequest(QCLOUD_BUCKET_ID, key, "null")
        return cosService.getAccessUrl(request).also {
            Timber.d("resourceUrl: $it ")
        }
    }

}