package com.shannon.android.lib.web3.dapp

import android.content.ActivityNotFoundException
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Log
import androidx.lifecycle.*
import com.google.gson.Gson
import com.shannon.android.lib.web3.BuildConfig
import com.shannon.android.lib.web3.rpc.CustomRpcMethods
import com.shannon.android.lib.web3.rpc.EthereumChain
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.bouncycastle.util.encoders.Hex
import org.web3j.abi.Utils
import org.web3j.crypto.ECDSASignature
import org.web3j.crypto.Hash
import org.web3j.crypto.Keys
import org.web3j.crypto.Sign
import org.web3j.utils.Numeric
import walletconnect.core.requests.eth.EthTransaction
import walletconnect.core.session.SessionLifecycle
import walletconnect.core.session.callback.*
import walletconnect.core.session.model.json_rpc.EthRpcMethod
import walletconnect.core.session_state.SessionStore
import walletconnect.core.session_state.model.SessionState
import walletconnect.core.session_state.model.toInitialSessionState
import java.lang.ref.WeakReference
import java.math.BigInteger
import java.util.*

/**
 *
 * @Package:        com.shannon.android.lib.web3.dapp
 * @ClassName:      DappProxy
 * @Description:     作用描述
 * @Author:         czhen
 * @CreateDate:     2022/10/22 14:19
 */
class DappProxy private constructor() : LifecycleObserver {

    private val parameters by lazy { Parameters() }

    private var contextWeak: WeakReference<Context>? = null
    private var lifecycleScopeWeak: WeakReference<Lifecycle>? = null
    private var onCallbackDataWeak: WeakReference<OnCallbackData>? = null

    private lateinit var mDapp: OpenDAppManager
    private lateinit var sessionLifecycle: SessionLifecycle
    private lateinit var sessionStore: SessionStore

    private var socketConnected: Boolean = false
    private var walletAddress: String? = null
    private var walletChainId: Int? = null

    companion object {
        private const val TAG = "DappProxy"

        val instance by lazy(LazyThreadSafetyMode.SYNCHRONIZED) {
            DappProxy()
        }
    }


    fun initialize(
        context: Context,
        lifecycleScope: Lifecycle,
        callbackData: OnCallbackData
    ) {
        contextWeak = WeakReference(context)
        lifecycleScopeWeak = WeakReference(lifecycleScope)
        lifecycleScope.addObserver(this)
        onCallbackDataWeak = WeakReference(callbackData)
        if (!::mDapp.isInitialized) {
            mDapp = createDapp(context)
            sessionLifecycle = mDapp
        }
    }

    private fun createDapp(context: Context): OpenDAppManager {
        return parameters.let {
            sessionStore = it.createSessionStore(context)
            OpenDAppManager(
                socket = it.createSocket(),
                sessionStore = sessionStore,
                jsonAdapter = it.createJsonAdapter(),
                dispatcherProvider = it.dispatcherProvider,
                logger = it.logger
            )
        }
    }

    fun getWalletAddress() = walletAddress

    fun getWalletChainId() = walletChainId

    fun openLoginConnect() {
        if (mDapp.socketInitialized()) {
            Log.d(TAG, "openConnect socketInitialized")
            return
        }

        mDapp.openSocketAsync(
            parameters.initialSessionState(),
            this@DappProxy::onSessionCallback
        ) {
            lifecycleScopeWeak?.get()?.coroutineScope?.launch(parameters.dispatcherProvider.ui()) {
                onCallbackDataWeak?.get()
                    ?.onSessionStateUpdated(null)
            }
        }
    }


    fun removeSessions() {
        lifecycleScopeWeak?.get()?.coroutineScope?.launch {
            sessionStore.removeAll()
        }
    }

    /**
     * 打开Socket连接
     *  -- 如果本地已保存授权用户则直接连接已授权的会话
     *  -- 否则就先打开Socket，等待用户打开钱包去请求授权
     */
    fun openConnect() {
        if (mDapp.socketInitialized()) {
            Log.d(TAG, "openConnect socketInitialized")
            lifecycleScopeWeak?.get()?.coroutineScope?.launch(parameters.dispatcherProvider.ui()) {
                Log.d(TAG, "openConnect socketInitialized walletAddress: $walletAddress")
                onCallbackDataWeak?.get()
                    ?.onSessionStateUpdated(walletAddress)
            }
            return
        }
        lifecycleScopeWeak?.get()?.coroutineScope?.launch {
            val sessionStore = sessionStore.getAll()
            Log.d(TAG, "openConnect Session store size: ${sessionStore?.size}")
            val sessionSate = if (sessionStore.isNullOrEmpty()) null else sessionStore.last()
            if (sessionSate != null) {
                if (!mDapp.openSocket(
                        sessionSate.toInitialSessionState(),
                        this@DappProxy::onSessionCallback
                    )
                ) {
                    lifecycleScopeWeak?.get()?.coroutineScope?.launch(parameters.dispatcherProvider.ui()) {
                        Log.d(TAG, "openConnect Socket Fresh: $walletAddress")
                        onCallbackDataWeak?.get()
                            ?.onSessionStateUpdated(walletAddress)
                    }
                }
            } else {
                mDapp.openSocketAsync(
                    parameters.initialSessionState(),
                    this@DappProxy::onSessionCallback
                ) {
                    lifecycleScopeWeak?.get()?.coroutineScope?.launch(parameters.dispatcherProvider.ui()) {
                        onCallbackDataWeak?.get()
                            ?.onSessionStateUpdated(null)
                    }
                }
            }
        }
    }

    /**
     * 请求和钱包创建会话，请求发出后打开钱包应用
     */
    fun requestSession() {
        mDapp.sendSessionRequest(null)
    }

    fun switchWallet() {
        Log.d(TAG, "switchWallet sessionApproved = ${mDapp.sessionApproved()}")
        if (mDapp.sessionApproved()) {
            mDapp.closeAsync(
                deleteLocal = true,
                deleteRemote = true,
                delayMs = 0,
                onClosed = { closed ->
                    if (closed) openSocketAndSession()
                })
        } else {
            openSocketAndSession()
        }
    }

    private fun openSocketAndSession() {
        mDapp.openSocketAsync(
            parameters.initialSessionState(),
            this@DappProxy::onSessionCallback,
            onOpened = {
                requestSession()
            }
        )
    }

    /**
     * 发起交易 (根据data格式不同可以创建不同类型的交易)
     * @param toAddress String 合约地址
     * @param data String 要发送的数据
     */
    fun sendBindTokenUri(toAddress: String, data: String) {
        walletAddress?.let { address ->
            mDapp.sendRequestAsync(
                EthRpcMethod.SendTransaction,
                data = listOf(
                    parameters.createMintTransaction(
                        address, //购买人钱包地址
                        toAddress,//合约地址
                        data,
                        walletChainId!!, null
                    )
                ),
                itemType = EthTransaction::class.java,
                onCallback = {
                    if (it is RequestCallback.EthSendTxResponse) {
                        it.transactionHash?.run {
                            Log.d(TAG, "sendBindTokenUri transactionHash = $this")
//                            fetchTransactionReceipt(toAddress, this)
                            onCallbackDataWeak?.get()
                                ?.setTokenUriSuccess()
                        }
                    }
                })
        }
    }

    /**
     * 发起交易 (根据data格式不同可以创建不同类型的交易)
     * @param toAddress String 合约地址
     * @param data String 要发送的数据
     */
    fun sendMintTransaction(toAddress: String, data: String, price: String) {
        walletAddress?.let { address ->
            mDapp.sendRequestAsync(
                EthRpcMethod.SendTransaction,
                data = listOf(
                    parameters.createMintTransaction(
                        address, //购买人钱包地址
                        toAddress,//合约地址
                        data,
                        walletChainId!!,
                        price
                    )
                ),
                itemType = EthTransaction::class.java,
                onCallback = {
                    if (it is RequestCallback.EthSendTxResponse) {
                        it.transactionHash?.run {
                            Log.d(TAG, "sendMintTransaction transactionHash = $this")
                            onCallbackDataWeak?.get()
                                ?.onTransactionHash(this)
                        }
                    } else if (it is RequestCallback.RequestRejected) {
                        onCallbackDataWeak?.get()?.onInterrupt()
                    }
                })
        }
    }


    /**
     * 发起交易 (根据data格式不同可以创建不同类型的交易)
     * @param toAddress String 合约地址、钱包地址
     * @param data String 要发送的数据
     */
    fun sendTransaction(toAddress: String, data: String, price: String?) {
        walletAddress?.let { address ->
            mDapp.sendRequestAsync(
                EthRpcMethod.SendTransaction,
                data = listOf(
                    parameters.createTokenTransaction(
                        address,
                        toAddress,
                        data,
                        walletChainId!!,
                        price
                    )
                ),
                itemType = EthTransaction::class.java,
                onCallback = {
                    if (it is RequestCallback.EthSendTxResponse) {
                        it.transactionHash?.run {
                            Log.d(TAG, "SendTransaction transactionHash = $this")
                            onCallbackDataWeak?.get()
                                ?.onTransactionHash(this)
                        }
                    } else if (it is RequestCallback.RequestRejected) {
                        onCallbackDataWeak?.get()?.onInterrupt()
                    }
                }
            )
        }
    }

    fun sendSignTypedData(signMessage: SignMessage) {
        walletAddress?.let {
            mDapp.sendRequestAsync(
                EthRpcMethod.SignTypedData,
                data = arrayListOf(
                    it,
                    parameters.jsonSignTypes(it, walletChainId!!, signMessage)
                ),
                String::class.java,
                onRequested = {
                    triggerDeepLinkDelay()
                },
            )
        }
    }

    fun sendLoginSignTypedData(
        content: String,
        nonce: String
    ) {
        walletAddress?.let {
            mDapp.sendRequestAsync(
                CustomRpcMethods.signTypeData,//  EthRpcMethod.SignTypedData, // CustomRpcMethods.signTypeData
                data = arrayListOf(
                    it,
                    parameters.jsonLoginSignTypes(content, nonce, walletChainId!!)
                ),
                String::class.java,
                onRequested = {
                    triggerDeepLinkDelay()
                },
            )
        }
    }

    fun verify() {

        val content =
            "www.openvoice.com wants you to sign in with your Ethereum account:\n0xEe24Ddb5AA5f1dC7cf50498B3D97289095B1CDc4\n\nSign in with Ethereum to the app.\n\nURI: https://www.openvoice.com\nVersion: 1\nChain ID: 1\nNonce: Z2mLo9vS0HuDGzTc\nIssued At: 2023-02-01T10:04:38.611Z"
        val signatureBytes =
            Numeric.hexStringToByteArray("0x2cad71b1e09f3937090b3e0e5d5818e23214162c1023c90dae94cb2d092f99ea1c6f110bd17e901a5a31f619497a177f03aa16d71d87ff46788cc175fd04b91b1b")
        val r = Arrays.copyOfRange(signatureBytes, 0, 32)
        val s = Arrays.copyOfRange(signatureBytes, 32, 64)
        val v = signatureBytes[64]
        val recId = v - 27
        val sign = ECDSASignature(BigInteger(1, r), BigInteger(1, s))
        val recoverPubKey =
            Sign.recoverFromSignature(recId, sign, Hash.sha3(content.toString().toByteArray()))

        Log.d(
            TAG, "recover public_key: ${Numeric.prependHexPrefix(Keys.getAddress(recoverPubKey))}\n"
        )
    }

    fun verify2() {
        val PERSONAL_MESSAGE_PREFIX = "\u0019Ethereum Signed Message:\n"
        val content =
            "www.openvoice.com wants you to sign in with your Ethereum account:\n0xEe24Ddb5AA5f1dC7cf50498B3D97289095B1CDc4\n\nSign in with Ethereum to the app.\n\nURI: https://www.openvoice.com\nVersion: 1\nChain ID: 1\nNonce: Z2mLo9vS0HuDGzTc\nIssued At: 2023-02-01T10:04:38.611Z"
        val prefix = PERSONAL_MESSAGE_PREFIX + content.length
        val signatureBytes =
            Numeric.hexStringToByteArray("0x2cad71b1e09f3937090b3e0e5d5818e23214162c1023c90dae94cb2d092f99ea1c6f110bd17e901a5a31f619497a177f03aa16d71d87ff46788cc175fd04b91b1b")
        val r = Arrays.copyOfRange(signatureBytes, 0, 32)
        val s = Arrays.copyOfRange(signatureBytes, 32, 64)
        val v = signatureBytes[64]

        val signData = Sign.SignatureData(v, r, s)
        for (i in 0 until 4) {
            val recoverPubKey =
                Sign.recoverFromSignature(
                    i,
                    ECDSASignature(BigInteger(1, signData.r), BigInteger(1, signData.s)),
                    Hash.sha3((prefix + content).toByteArray())
                )
            if (recoverPubKey != null)
                Log.d(
                    TAG,
                    "recover public_key: ${Numeric.prependHexPrefix(Keys.getAddress(recoverPubKey))}\n"
                )
        }


    }

    fun addEthereumChain() {
        walletAddress?.let {
            val chainIdHex = "0x1"
            mDapp.sendRequestAsync(
                CustomRpcMethods.AddEthereumChain,
                data = arrayListOf(
                    EthereumChain(
                        chainIdHex,
                        "VoiceTest",
                        arrayListOf(BuildConfig.CHAIN_URL),
                        null,
                        null
                    )
                ),
                EthereumChain::class.java,
                onRequested = {
                    triggerDeepLinkDelay()
                },
            )
        }
    }

    private fun onSessionCallback(callbackData: CallbackData) {
        val coroutineScope = lifecycleScopeWeak?.get()?.coroutineScope ?: return
        coroutineScope.launch(parameters.dispatcherProvider.ui()) {
            when (callbackData) {
                is SessionCallback -> {
                    when (callbackData) {
                        is SessionCallback.SessionRequested -> {
                            Log.d(
                                TAG,
                                "onSessionCallback: SessionRequested"
                            )
                            triggerDeepLink()
                        }
                        is SessionCallback.SessionDeleted -> {
                            walletAddress = null
                            walletChainId = null
                            onCallbackDataWeak?.get()
                                ?.onSessionStateUpdated(walletAddress)
                        }
                        is SessionCallback.LocalSessionStateUpdated -> {
                            callbackData.sessionState?.apply {
                                walletChainId = chainId
                                accounts?.run {
                                    if (isNotEmpty()) {
                                        walletAddress = Keys.toChecksumAddress(first())
                                        onCallbackDataWeak?.get()
                                            ?.onSessionStateUpdated(walletAddress)
                                    }
                                }

                            }
                            Log.d(
                                TAG,
                                "onSessionCallback: LocalSessionStateUpdated account: $walletAddress ; chainId: $walletChainId "
                            )
                            if (walletAddress != null) {
                                Log.d(
                                    TAG,
                                    "onSessionCallback: LocalSessionStateUpdated checksum: ${
                                        Keys.toChecksumAddress(
                                            walletAddress
                                        )
                                    } ; chainId: $walletChainId"
                                )
                            }
                        }
                        is SessionCallback.SessionUpdated -> {
                            val address =
                                if (callbackData.accounts.isNullOrEmpty()) null else callbackData.accounts?.first()
                            Log.d(
                                TAG,
                                "onSessionCallback: SessionUpdated account: $address ; chainId: ${callbackData.chainId}"
                            )
                            Log.d(
                                TAG,
                                "onSessionCallback: SessionUpdated checksum: ${
                                    Keys.toChecksumAddress(
                                        address
                                    )
                                } ; chainId: ${callbackData.chainId}"
                            )
                        }
                        is SessionCallback.SessionRejected -> {
                            Log.d(TAG, "onSessionCallback: SessionRejected")
                            coroutineScope.launch(parameters.dispatcherProvider.io()) {
                                delay(1500)
                                Log.d(
                                    TAG,
                                    "onSessionCallback: Reconnect after deauthorization"
                                )
                                openConnect()
                            }
                        }
                        is SessionCallback.SessionClosedLocally -> {
                            walletAddress = null
                            walletChainId = null
                            onCallbackDataWeak?.get()
                                ?.onSessionStateUpdated(walletAddress)
                        }
                    }
                }
                is SocketCallback -> {
                    when (callbackData) {
                        SocketCallback.SocketConnected -> {
                            socketConnected = true
                            onCallbackDataWeak?.get()
                                ?.onSocketState(OnCallbackData.SocketState.SOCKET_CONNECTED)
                            Log.d(TAG, "onSessionCallback: SocketConnected")
                        }
                        SocketCallback.SocketClosed, SocketCallback.SocketDisconnected -> {
                            socketConnected = false
                            onCallbackDataWeak?.get()
                                ?.onSocketState(OnCallbackData.SocketState.SOCKET_DISCONNECTED)
                            Log.d(TAG, "onSessionCallback: SocketDisconnected")
                        }
                        SocketCallback.SocketConnecting -> {
                            onCallbackDataWeak?.get()
                                ?.onSocketState(OnCallbackData.SocketState.SOCKET_CONNECTING)
                            Log.d(TAG, "onSessionCallback: SocketConnecting")
                        }
                        is SocketCallback.SocketMessage -> {}
                    }
                }

                is RequestCallback.EthSendTxRequested -> {
                    triggerDeepLinkDelay()
                }
                is RequestCallback.EthSendTxResponse -> {
                    Log.d(
                        TAG,
                        "RequestCallback: EthSendTxResponse [transactionHash: ${callbackData.transactionHash}]"
                    )
                }
                is RequestCallback.EthSignResponse -> {
                    Log.d(
                        TAG,
                        "RequestCallback: EthSignResponse [signature: ${callbackData.signature}]"
                    )
                    onCallbackDataWeak?.get()
                        ?.onSignature(callbackData.signature)
                }
                is RequestCallback.RequestRejected -> {
                    onCallbackDataWeak?.get()
                        ?.onInterrupt()
                }
                is RequestCallback.CustomResponse -> {
                    Log.d(
                        TAG,
                        "RequestCallback: CustomResponse [signature: ${callbackData.data}]"
                    )
                    onCallbackDataWeak?.get()
                        ?.onSignature(callbackData.data as String)
                }
                is FailureCallback -> {
                    onCallbackDataWeak?.get()
                        ?.onInterrupt()
                }
                else -> {}
            }
        }
    }


    fun triggerDeepLink() {// metamask://
        val currentSessionState = mDapp.getInitialSessionState() ?: return
        val context = contextWeak?.get() ?: return
        try {
            val myIntent =
                Intent(Intent.ACTION_VIEW, Uri.parse(currentSessionState.connectionParams.toUri()))
//            myIntent.setPackage("io.metamask")
            context.startActivity(myIntent)
        } catch (_: ActivityNotFoundException) {
            onCallbackDataWeak?.get()
                ?.noWalletFound()
        }
    }

    private fun triggerDeepLinkDelay() {
        val coroutineScope = lifecycleScopeWeak?.get()?.coroutineScope ?: return
        coroutineScope.launch(parameters.dispatcherProvider.io()) {
            delay(2000)
            coroutineScope.launch(parameters.dispatcherProvider.ui()) {
                triggerDeepLink()
            }
        }
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
    fun close(source: LifecycleOwner) {
//        walletAddress = null
//        walletChainId = null
//        mDapp.closeAsync(deleteLocal = false, deleteRemote = false, delayMs = 500)
//        lifecycleScopeWeak?.get()?.removeObserver(this)
    }

    private fun closeRemoteSocket(state: SessionState) {
        val coroutineScope = lifecycleScopeWeak?.get()?.coroutineScope ?: return
        coroutineScope.launch(parameters.dispatcherProvider.io()) {
            mDapp.closeRemoteSocket(state)
        }
    }

    fun closeSocket() {
        mDapp.closeAsync(deleteLocal = false, deleteRemote = false, delayMs = 500)
    }

    fun logout() {
        walletAddress = null
        walletChainId = null
        if (::mDapp.isInitialized)
            mDapp.closeAsync(deleteLocal = false, deleteRemote = false, delayMs = 500)
        lifecycleScopeWeak?.get()?.removeObserver(this)
    }


    private fun openWallet() {
        val context = contextWeak?.get() ?: return
        val intent = Intent(Intent.ACTION_MAIN)
        intent.addCategory(Intent.CATEGORY_LAUNCHER)
        val comp = ComponentName("io.metamask", "io.metamask.MainActivity")
        intent.component = comp
        context.startActivity(intent)

    }
}