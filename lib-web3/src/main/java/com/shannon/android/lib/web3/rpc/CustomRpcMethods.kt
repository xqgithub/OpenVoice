package com.shannon.android.lib.web3.rpc

import walletconnect.core.session.model.json_rpc.CustomRpcMethod
import walletconnect.core.session.model.json_rpc.EthRpcMethod

/**
 *
 * @Package:        com.shannon.android.lib.web3.rpc
 * @ClassName:      CustomRpcMethods
 * @Description:     作用描述
 * @Author:         czhen
 * @CreateDate:     2022/10/28 13:43
 */
object CustomRpcMethods {

    val AddEthereumChain: CustomRpcMethod
        get() = CustomRpcMethod("wallet_addEthereumChain")

    val signTypeData: CustomRpcMethod
        get() = CustomRpcMethod("eth_signTypedData_v4")

}
