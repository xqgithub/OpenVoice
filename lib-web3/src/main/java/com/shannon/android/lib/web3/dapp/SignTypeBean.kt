package com.shannon.android.lib.web3.dapp

import java.math.BigInteger


/**
 *
 * @Package:        com.shannon.android.lib.web3.dapp
 * @ClassName:      SignTypeBean
 * @Description:     作用描述
 * @Author:         czhen
 * @CreateDate:     2022/10/31 20:31
 */
data class SignTypeBean<T>(
    val types: Types = createTypes(),
    val primaryType: String = "OpenVoice",
    val domain: Domain,
    val message: T
)

data class Types(val EIP712Domain: List<NameType>, val OpenVoice: List<NameType>? = null, val Login: List<NameType>? = null)

data class NameType(val name: String, val type: String)

data class Domain(
    val name: String,
    val version: String,
    val chainId: Int,
    val verifyingContract: String? = null
)

data class Message(
    val tokenId: String,
    val contract: String,
    val price: String,
    val creator: String,
    val royaltyFraction: Int,
    val seller: String,
)

data class LoginMessage(
    val contents: String,
    val nonces: String? = null,
)


/**
 *
 * @param isMinted Boolean 是否铸造
 * @param royaltyReceiver String 版税接收者的钱包地址
 * @param tokenId String tokenId
 * @param contract String NFT合约地址
 * @param initialPrice String 价格 用户输入的初始价格
 * @param serviceFee String 平台服务费比例
 * @param royaltyFraction String 版税
 */
data class SignMessage(
    val isMinted: Boolean,
    val royaltyReceiver: String,
    val tokenId: String,
    val registryContractAddress: String,
    val contract: String,
    val initialPrice: String,
    val serviceFee: String,
    val royaltyFraction: String,
)

private fun createTypes(): Types {
    val eip712Domain = arrayListOf<NameType>()
    eip712Domain.add(NameType("name", "string"))
    eip712Domain.add(NameType("version", "string"))
    eip712Domain.add(NameType("chainId", "uint256"))
    eip712Domain.add(NameType("verifyingContract", "address"))

    val openVoice = arrayListOf<NameType>()
    openVoice.add(NameType("tokenId", "uint256"))
    openVoice.add(NameType("contract", "address"))
    openVoice.add(NameType("price", "uint256"))
    openVoice.add(NameType("creator", "address"))
    openVoice.add(NameType("royaltyFraction", "uint96"))
    openVoice.add(NameType("seller", "address"))

    return Types(eip712Domain, OpenVoice =  openVoice)
}

fun createLoginTypes(): Types {
    val eip712Domain = arrayListOf<NameType>()
    eip712Domain.add(NameType("name", "string"))
    eip712Domain.add(NameType("version", "string"))
    eip712Domain.add(NameType("chainId", "uint256"))

    val login = arrayListOf<NameType>()
    login.add(NameType("contents", "string"))
//    login.add(NameType("nonces", "uint256"))

    return Types(eip712Domain, Login =  login)
}
