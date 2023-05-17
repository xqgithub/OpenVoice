package com.shannon.android.lib.web3.dapp

/**
 *
 * @Package:        com.shannon.android.lib.web3.dapp
 * @ClassName:      OnCallbackData
 * @Description:     作用描述
 * @Author:         czhen
 * @CreateDate:     2022/10/22 16:56
 */
interface OnCallbackData {

    enum class SocketState {
        SOCKET_CONNECTING,
        SOCKET_CONNECTED,
        SOCKET_DISCONNECTED
    }

    fun onSessionStateUpdated(account: String?)

    fun onSocketState(state: SocketState)

    fun onSignature(signature: String) {}

    fun onMintTransactionTokenId(tokenId: String) {}

    fun setTokenUriSuccess() {}

    fun onTransactionHash(hash: String) {}

    fun onInterrupt() {}

    fun noWalletFound()
}