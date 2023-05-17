package com.shannon.android.lib.web3.contract

import android.util.Log
import com.shannon.android.lib.web3.BuildConfig
import com.shannon.android.lib.web3.dapp.Parameters
import com.shannon.android.lib.web3.dapp.SignMessage
import org.web3j.abi.Utils
import org.web3j.crypto.Credentials
import org.web3j.protocol.Web3j
import org.web3j.protocol.http.HttpService
import org.web3j.tx.ClientTransactionManager
import org.web3j.tx.gas.DefaultGasProvider
import org.web3j.tx.response.PollingTransactionReceiptProcessor
import org.web3j.utils.Numeric
import walletconnect.core.cryptography.hexToByteArray
import java.math.BigDecimal
import java.math.BigInteger
import kotlin.concurrent.thread

/**
 *
 * @Package:        com.shannon.android.lib.web3.contract
 * @ClassName:      CallContractManager
 * @Description:     调用智能合约
 * @Author:         czhen
 * @CreateDate:     2022/10/22 10:54
 */
class CallContractManager private constructor(
    registryContractAddress: String,
    nftContractAddress: String
) {
    private val registryContract: LazyMintWith712
    private val nftContract: AssetContractShared
    private val web3j = Web3j.build(HttpService(BuildConfig.CHAIN_URL))

    companion object {
        fun create(registryContractAddress: String, nftContractAddress: String) =
            CallContractManager(registryContractAddress, nftContractAddress)
    }

    init {

        Log.d("DappProxy", "chainUrl = ${BuildConfig.CHAIN_URL} ")
        val credentials =
            Credentials.create("3d1789e9a4f00f0633fa4403ca4f93fbd542113af9fec84d2d5f6a9e989a501f")
//        val credentials =
//            Credentials.create("6a42ab02075932d74967b6e617adff235a5c02ecd8137d96442abcefab16f2c8")
        registryContract = LazyMintWith712.load(
            registryContractAddress,
            web3j,
            ClientTransactionManager(web3j, "0xEe24Ddb5AA5f1dC7cf50498B3D97289095B1CDc4"),
            DefaultGasProvider()
        )
        nftContract = AssetContractShared.load(
            nftContractAddress,
            web3j,
            ClientTransactionManager(web3j, "0xEe24Ddb5AA5f1dC7cf50498B3D97289095B1CDc4"),
            DefaultGasProvider()
        )
    }

    fun setTokenUri(tokenId: String, uri: String): String {
        return nftContract.setURI(BigInteger(tokenId), uri).encodeFunctionCall()
    }

    fun getTokenUri(tokenId: String) {
        thread {
            val uri = nftContract.uri(BigInteger(tokenId)).sendAsync().get()
            Log.d("DappProxy", "getTokenUri: $uri")
        }
    }

    fun getChainId() {
        thread {
            val chainId = web3j.ethChainId().sendAsync().get()
            Log.d("DappProxy", "getChainId: ${chainId.chainId}")
        }
    }

    fun mint(
        signMessage: SignMessage,
        sellerAddress: String,
        buyerAddress: String,
        signature: String
    ): String {
        Log.d("DappProxy", "mint: $signMessage")

        val signatureByteArray = signature.replace("0x", "").hexToByteArray()
        val royaltyFraction =
            BigDecimal(signMessage.royaltyFraction).times(BigDecimal(100)).toBigInteger()
        val amount = Parameters.toWei(
            Parameters.calculateConsumptionAmount(
                signMessage.initialPrice,
                signMessage.serviceFee,
                if (signMessage.isMinted) signMessage.royaltyFraction else "0.0" //如果没有铸造，下次交易不计算版税
            )
        )

        return registryContract.mintNFT(
            BigInteger(signMessage.tokenId),
            signMessage.contract,
            signMessage.royaltyReceiver,
            royaltyFraction,
            sellerAddress,
            buyerAddress,
            BigInteger("1"),
            "".toByteArray(),
            signatureByteArray,
            amount
        ).encodeFunctionCall()
    }

    /**
     *
     * @param from String 合约地址
     * @param to String 购买人钱包地址
     * @param tokenId String
     * @return String
     */
    fun transferFrom(
        signMessage: SignMessage,
        sellerAddress: String,
        buyerAddress: String,
        signature: String
    ): String {
        Log.d("DappProxy", "transferFrom: $signMessage")

        val signatureByteArray = signature.replace("0x", "").hexToByteArray()
        val royaltyFraction =
            BigDecimal(signMessage.royaltyFraction).times(BigDecimal(100)).toBigInteger()
        val amount = Parameters.toWei(
            Parameters.calculateConsumptionAmount(
                signMessage.initialPrice,
                signMessage.serviceFee,
                if (signMessage.isMinted) signMessage.royaltyFraction else "0.0" //如果没有铸造，下次交易不计算版税
            )
        )
        return registryContract.transferNFT(
            BigInteger(signMessage.tokenId),
            signMessage.contract,
            signMessage.royaltyReceiver,
            royaltyFraction,
            sellerAddress,
            buyerAddress,
            BigInteger("1"),
            "".toByteArray(),
            signatureByteArray,
            amount
        ).encodeFunctionCall()
    }

    fun fetchTransactionReceipt(hash: String): Boolean {
        val processor = PollingTransactionReceiptProcessor(
            web3j,
            5 * 1000,
            6
        )
        Log.d("DappProxy", "waitForTransactionReceipt --------------- start ")
        return try {
            val receipt = processor.waitForTransactionReceipt(hash)
            Log.d("DappProxy", "waitForTransactionReceipt --------------- end ${receipt.isStatusOK}")
            receipt.isStatusOK
        }catch (io:Exception){
            true
        }
    }

}