package com.shannon.android.lib.web3.dapp

import android.content.Context
import android.util.Log
import com.google.gson.GsonBuilder
import com.shannon.android.lib.web3.R
import com.shannon.android.lib.web3.contract.LazyMintWith712
import com.shannon.android.lib.web3.contract.LazyMintWith712D
import com.shannon.android.lib.web3.impl.AndroidDispatcherProvider
import com.shannon.android.lib.web3.impl.AndroidLogger
import com.tinder.scarlet.Scarlet
import com.tinder.scarlet.lifecycle.LifecycleRegistry
import com.tinder.scarlet.messageadapter.gson.GsonMessageAdapter
import com.tinder.scarlet.retry.ExponentialBackoffStrategy
import com.tinder.scarlet.websocket.okhttp.newWebSocketFactory
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import org.web3j.abi.datatypes.Address
import org.web3j.abi.datatypes.DynamicArray
import org.web3j.abi.datatypes.Utf8String
import org.web3j.abi.datatypes.generated.Uint256
import org.web3j.abi.datatypes.generated.Uint96
import walletconnect.adapter.gson.GsonAdapter
import walletconnect.adapter.gson.type_adapter.JsonRpcMethodTypeAdapter
import walletconnect.adapter.gson.type_adapter.SocketMessageTypeAdapter
import walletconnect.core.cryptography.Cryptography
import walletconnect.core.cryptography.toHex
import walletconnect.core.requests.eth.EthTransaction
import walletconnect.core.session.model.InitialSessionState
import walletconnect.core.session.model.json_rpc.JsonRpcMethod
import walletconnect.core.session_state.SessionStore
import walletconnect.core.session_state.model.ConnectionParams
import walletconnect.core.session_state.model.PeerMeta
import walletconnect.core.socket.Socket
import walletconnect.core.socket.model.SocketMessageType
import walletconnect.core.util.DispatcherProvider
import walletconnect.socket.scarlet.FlowStreamAdapter
import walletconnect.socket.scarlet.SocketManager
import walletconnect.socket.scarlet.SocketService
import walletconnect.store.prefs.SharedPrefsSessionStore
import java.math.BigDecimal
import java.math.BigInteger
import java.util.*
import java.util.concurrent.TimeUnit
import kotlin.math.pow

/**
 *
 * @Package:        com.shannon.android.lib.web3.dapp
 * @ClassName:      Parameters
 * @Description:     作用描述
 * @Author:         czhen
 * @CreateDate:     2022/10/22 15:11
 */
class Parameters {

    val dispatcherProvider: DispatcherProvider = AndroidDispatcherProvider()
    val logger = AndroidLogger()


    fun initialSessionState() = InitialSessionState(
        connectionParams(),
        myPeerId = UUID.randomUUID().toString(),
        myPeerMeta = PeerMeta(
            name = "OpenVoiceover",
            url = "https://www.openvoiceover.com/",
            description = "Make your voice original",
            icons = listOf("https://oss.openvoiceover.com/openvoiceoverlogo.png")
        )
    )

    private fun connectionParams() = ConnectionParams(
        topic = UUID.randomUUID().toString(),
        version = "1",
        bridgeUrl = "https://safe-walletconnect.gnosis.io",
        symmetricKey = Cryptography.generateSymmetricKey().toHex()
    )

    fun createSocket(): Socket {
        return SocketManager(
            socketServiceFactory = { url, lifecycleRegistry ->
                createSocketService(
                    url,
                    lifecycleRegistry
                )
            },
            gson = createGson(),
            dispatcherProvider = dispatcherProvider,
            logger = logger
        )
    }

    private fun createSocketService(
        url: String,
        lifecycleRegistry: LifecycleRegistry
    )
            : SocketService {

        val interceptor = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.HEADERS
        }

        val okHttpClient = OkHttpClient.Builder()
            .callTimeout(10, TimeUnit.SECONDS)
            .readTimeout(10, TimeUnit.SECONDS)
            .writeTimeout(10, TimeUnit.SECONDS)
            .pingInterval(4, TimeUnit.SECONDS)
            .addNetworkInterceptor(interceptor)
            .cache(null)
            .build()

        val webSocketFactory = okHttpClient.newWebSocketFactory(url)

        val gson = GsonBuilder()
            .registerTypeAdapter(SocketMessageType::class.java, SocketMessageTypeAdapter())
            .registerTypeAdapter(JsonRpcMethod::class.java, JsonRpcMethodTypeAdapter())
            .create()

        val scarlet = Scarlet.Builder()
            .webSocketFactory(webSocketFactory)
            .addMessageAdapterFactory(GsonMessageAdapter.Factory(gson))
            .addStreamAdapterFactory(FlowStreamAdapter.Factory)
            .backoffStrategy(
                ExponentialBackoffStrategy(
                    initialDurationMillis = 1_000L,
                    maxDurationMillis = 8_000L
                )
            )
            .lifecycle(lifecycleRegistry)
            .build()

        return scarlet.create(SocketService::class.java)
    }

    private fun createGson() = GsonBuilder()
        .registerTypeAdapter(SocketMessageType::class.java, SocketMessageTypeAdapter())
        .registerTypeAdapter(JsonRpcMethod::class.java, JsonRpcMethodTypeAdapter())
        .create()

    fun createJsonAdapter() = GsonAdapter(createGson())

    fun createSessionStore(context: Context)
            : SessionStore {
        val sharedPrefs = context.applicationContext.getSharedPreferences(
            context.applicationContext.getString(R.string.app_name),
            Context.MODE_PRIVATE
        )

        return SharedPrefsSessionStore(
            sharedPrefs,
            dispatcherProvider,
            logger
        )
    }

    fun createMintTransaction(
        walletAddress: String,
        toAddress: String,
        data: String,
        chainId: Int,
        value: String?
    ): EthTransaction {
        val chainIdHex = "0x" + chainId.toHex()
        return EthTransaction(
            from = walletAddress, //买方钱包地址
            to = toAddress, //合约地址
            data = data,
            chainId = chainIdHex,
            gas = null,
            gasPrice = null,
            gasLimit = null,
            maxFeePerGas = null,
            maxPriorityFeePerGas = null,
            value = toWeiValue(value),
            nonce = null
        )
    }

    fun createTokenTransaction(
        walletAddress: String,
        toAddress: String,
        data: String,
        chainId: Int,
        value: String?
    ): EthTransaction {
        val chainIdHex = "0x" + chainId.toHex()
        return EthTransaction(
            from = walletAddress, //买方钱包地址
            to = toAddress, //卖房钱包地址
            data = data,
            chainId = chainIdHex,
            gas = null,
            gasPrice = null,
            gasLimit = null,
            maxFeePerGas = null,
            maxPriorityFeePerGas = null,
            value = toWeiValue(value),
            nonce = null
        )
    }

    fun jsonSignTypes(
        walletAddress: String, //钱包地址
        chainId: Int, //钱包链ID
        signMessage: SignMessage
    ): String {
        return createGson().toJson(
            SignTypeBean(
                domain = Domain(
                    "OpenVoice",
                    "1.0.0",
                    chainId,
                    signMessage.registryContractAddress
                ),
                message = Message(
                    signMessage.tokenId,
                    signMessage.contract,
                    toWei(
                        calculateConsumptionAmount(
                            signMessage.initialPrice,
                            signMessage.serviceFee,
                            if (signMessage.isMinted) signMessage.royaltyFraction else "0.0" //如果没有铸造，下次交易不计算版税
                        )
                    ).toString(),
                    signMessage.royaltyReceiver,
                    BigDecimal(signMessage.royaltyFraction).times(BigDecimal(100)).toInt(),
                    walletAddress
                )
            )
        ).toString()
    }

    fun jsonLoginSignTypes(
        content: String,
        nonce: String,
        chainId: Int //钱包链ID
    ): String {
        return createGson().toJson(
            SignTypeBean(
                types = createLoginTypes(),
                primaryType = "Login",
                domain = Domain(
                    "Login",
                    "1",
                    chainId
                ),
                message = LoginMessage(content)
            )
        ).toString().also {
            Log.d("Parameters", "login sign: $it")
        }
    }

    companion object {
//        val registryContractAddress = "0x4B7FA9eC373AE1a2993aE80fB7Ce33CF07E87370"
//        val nftContractAddress = "0x58CBfa13b528cdDbBc7B4C8C969D042Ec2DeA145"

        //ETMP
//        val registryContractAddress = "0xE8AB4A63B2164b07582f30eD6752406468AE5a2A"
//        val nftContractAddress = "0x4c8332Ef38c07dC92f781E0B41809594399b776b"
        fun calculateConsumptionAmount(
            initialPrice: String,
            serviceFee: String,
            royaltiesFee: String
        ): String {
            val serviceFeeDecimal = BigDecimal(serviceFee).divide(BigDecimal(100)).toString().also {
                Log.d("Parameters", "serviceFeeDecimal: $it")
            }
            val royaltiesFeeDecimal =
                BigDecimal(royaltiesFee).divide(BigDecimal(100)).toString().also {
                    Log.d("Parameters", "royaltiesFeeDecimal: $it")
                }
            return BigDecimal("1").plus(BigDecimal(serviceFeeDecimal))
                .plus(BigDecimal(royaltiesFeeDecimal))
                .times(BigDecimal(initialPrice)).toString().also {
                    Log.d("Parameters", "calculateConsumptionAmount: $it ; toWei = ${toWei(it)}")
                }
        }

        fun toWeiValue(value: String?): String? {
            return if (value.isNullOrEmpty()) {
                null
            } else {
                "0x" + toWei(value).toString(16)
            }
        }

        fun toWei(value: String): BigInteger {
            return BigDecimal(value).times(BigDecimal(10.0.pow(18.0).toLong())).toBigInteger()
        }

        fun verifyInfo(
            signMessage: SignMessage,
            sellerAddress: String
        ): LazyMintWith712D.VerifyInfo {
            val tokenId = Uint256(BigInteger(signMessage.tokenId))
            val price = toWei(
                calculateConsumptionAmount(
                    signMessage.initialPrice,
                    signMessage.serviceFee,
                    if (signMessage.isMinted) signMessage.royaltyFraction else "0.0" //如果没有铸造，下次交易不计算版税
                )
            )
            val royaltyFraction =
                BigDecimal(signMessage.royaltyFraction).times(BigDecimal(100)).toBigInteger()
            val creator = if (signMessage.isMinted) sellerAddress else signMessage.royaltyReceiver
            return LazyMintWith712D.VerifyInfo(
                tokenId,// signMessage.tokenId,
                Address(160, signMessage.contract),
                Uint256(price),
                Address(160, creator),
                Uint96(royaltyFraction),
                Address(160, sellerAddress)
            )//0x37dd3d38277148beff585f93f391edde547113db00000000000001000000000a
        }

    }
}