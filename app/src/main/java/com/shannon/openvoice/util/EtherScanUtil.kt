package com.shannon.openvoice.util

import com.shannon.openvoice.BuildConfig
import timber.log.Timber

/**
 *
 * @Package:        com.shannon.openvoice.util
 * @ClassName:      EtherScanUtil
 * @Description:     作用描述
 * @Author:         czhen
 * @CreateDate:     2023/3/21 17:02
 */
object EtherScanUtil {

    fun getContractUrl(hash: String): String {
        return if (BuildConfig.DEBUG) {
            "https://goerli.etherscan.io/address/$hash"
        } else {
            "https://etherscan.io/address/$hash"
        }.also {
            Timber.d("ContractUrl: $it")
        }
    }

    fun getTransactionUrl(hash: String): String {
        return if (BuildConfig.DEBUG) {
            "https://goerli.etherscan.io/tx/$hash"
        } else {
            "https://etherscan.io/tx/$hash"
        }.also {
            Timber.d("TransactionUrl: $it")
        }
    }
}
