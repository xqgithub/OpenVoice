package com.shannon.android.lib.web3.rpc

/**
 * <描述当前功能>
 * @author: czhen
 * @date: 2022/11/29
 */
data class EthereumChain(
    val chainId: String,
    val chainName: String,
    val rpcUrls: List<String>,
    val blockExplorerUrls: List<String>?,
    val nativeCurrency:String?
)
