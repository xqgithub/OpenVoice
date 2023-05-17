package com.shannon.openvoice.util

import com.shannon.android.lib.BuildConfig
import com.shannon.android.lib.constant.ConfigConstants
import com.shannon.openvoice.FunApplication
import com.shannon.openvoice.util.aws.AwsUtil

/**
 *
 * @Package:        com.shannon.openvoice.util
 * @ClassName:      CloudStorageHelper
 * @Description:     作用描述
 * @Author:         czhen
 * @CreateDate:     2022/10/11 19:32
 */
class CloudStorageHelper() : CloudResourceInterface {

    private val cloudResourceInterface: CloudResourceInterface =
        if (BuildConfig.BUILD_TYPE == ConfigConstants.BuildType.RELEASE) {
            AwsUtil(FunApplication.getInstance())
        } else {
            CosCloud(FunApplication.getInstance())
        }

    override fun getResourceUrl(key: String): String {
        return cloudResourceInterface.getResourceUrl(key)
    }

}