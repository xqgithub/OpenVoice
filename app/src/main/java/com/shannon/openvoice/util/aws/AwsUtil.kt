package com.shannon.openvoice.util.aws

import android.content.Context
import com.amazonaws.auth.AWSCredentialsProvider
import com.amazonaws.mobile.client.AWSMobileClient
import com.amazonaws.mobile.client.Callback
import com.amazonaws.mobile.client.UserStateDetails
import com.amazonaws.mobile.config.AWSConfiguration
import com.amazonaws.mobileconnectors.s3.transferutility.TransferObserver
import com.amazonaws.mobileconnectors.s3.transferutility.TransferUtility
import com.amazonaws.services.s3.AmazonS3Client
import com.blankj.utilcode.util.FileUtils
import com.blankj.utilcode.util.LogUtils
import com.shannon.android.lib.extended.isBlankPlus
import com.shannon.openvoice.util.CloudResourceInterface
import java.util.concurrent.CountDownLatch

/**
 * Date:2022/10/11
 * Time:14:32
 * author:dimple
 */
class AwsUtil(mContext: Context) : CloudResourceInterface {

    private var transferUtility: TransferUtility

    private var s3Client: AmazonS3Client? = null

    private var sMobileClient: AWSCredentialsProvider? = null

    companion object {
        const val QCLOUD_BUCKET_ID = "voice-project-1304083978"
    }

    init {
//        val credentials = object : AWSCredentials {
//            override fun getAWSAccessKeyId(): String {
//                return ID
//            }
//
//            override fun getAWSSecretKey(): String {
//                return key
//            }
//        }
//
//        val jsonConfig = JSONObject()
//        val s3TransferUtility = JSONObject()
//        jsonConfig.putOpt("S3TransferUtility", s3TransferUtility)
//        s3TransferUtility.put("Region", Region)
//        s3TransferUtility.put("Bucket", Bucket)
//        val configuration = AWSConfiguration(jsonConfig)
//
//        s3Client = AmazonS3Client(credentials)
//        transferUtility = TransferUtility.builder()
//            .context(context)
//            .s3Client(s3Client)
//            .awsConfiguration(configuration)
//            .build()

        transferUtility = getTransferUtility(mContext)
    }

    /**
     * 初始化 AWSMobileClient
     */
    private fun getCredProvider(context: Context): AWSCredentialsProvider? {
        if (isBlankPlus(sMobileClient)) {
            val latch = CountDownLatch(1)
            AWSMobileClient.getInstance().initialize(context, object : Callback<UserStateDetails> {

                override fun onResult(result: UserStateDetails) {
                    LogUtils.i("UserStateDetails =-= ${result.userState}")
                    latch.countDown()
                }

                override fun onError(e: Exception) {
                    LogUtils.i("Initialization error =-= $e")
                    latch.countDown()
                }
            })

            try {
                latch.await()
                sMobileClient = AWSMobileClient.getInstance()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        return sMobileClient
    }

    /**
     * 获取使用构造的 TransferUtility 的实例
     */
    private fun getTransferUtility(context: Context): TransferUtility {
        if (isBlankPlus(transferUtility)) {
            transferUtility = TransferUtility.builder()
                .context(context)
                .s3Client(getS3Client(context))
                .awsConfiguration(AWSConfiguration(context))
                .build()
        }
        return transferUtility
    }

    /**
     * 获取使用给定构造的 S3 客户端的实例
     */
    private fun getS3Client(context: Context): AmazonS3Client? {
        if (isBlankPlus(s3Client)) {
            try {
                val regionString = AWSConfiguration(context)
                    .optJsonObject("S3TransferUtility")
                    .getString("Region")
                val region = com.amazonaws.regions.Region.getRegion(regionString)
                s3Client = AmazonS3Client(getCredProvider(context), region)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        return s3Client
    }

    fun uploadVoice(srcPath: String, cosPath: String): TransferObserver {
        val file = FileUtils.getFileByPath(srcPath)
        return transferUtility.upload("${QCLOUD_BUCKET_ID}/${cosPath}", file)
    }

    override fun getResourceUrl(key: String): String {
        return "https://oss.openvoiceover.com/".plus(key)
    }
}