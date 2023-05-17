package com.shannon.openvoice.model

import android.text.TextUtils
import com.google.gson.annotations.SerializedName
import java.util.*

/**
 *
 * @Package:        com.shannon.openvoice.model
 * @ClassName:      ModelDetail
 * @Description:     作用描述
 * @Author:         czhen
 * @CreateDate:     2022/8/26 11:40
 */
data class ModelDetail(
    val id: Long,
    val name: String,
    @SerializedName("model_type") val modelType: String,
    @SerializedName("model_source") val modelSource: String,
    val status: String,
    val price: String,
    val currency: String?,
    @SerializedName("is_liked") val isLiked: Boolean,
    @SerializedName("owner_account") val account: TimelineAccount,
    @SerializedName("is_model_owner") val isModelOwner: Boolean,
    @SerializedName("creator_account") val creatorAccount: TimelineAccount,
    @SerializedName("is_official") val isOfficial: Boolean,
    @SerializedName("metadata_url") val metadataUrl: String?,
    @SerializedName("contract_address") val contractAddress: List<String>,
    @SerializedName("creator_account_address") val creatorAccountAddress: String?,
    @SerializedName("ownership_account_address") val ownershipAccountAddress: String?,
    @SerializedName("royalties_fee") val royaltiesFee: String?,
    @SerializedName("chain") val chain: String,
    @SerializedName("token_id") val tokenId: String?,
    @SerializedName("token_strandards") val tokenStrandards: String,
    @SerializedName("service_fee") val serivceFee: String,
    @SerializedName("payload") val payload: String?,
    @SerializedName("is_minted") val isMinted: Boolean,
    @SerializedName("created_at") val createdAt: Date?,
    @SerializedName("like_count") val likeCount: Long,
    @SerializedName("usage_count") val usageCount: Long,
    @SerializedName("trading_count") val tradingCount: Long,
    @SerializedName("trading_rank") val tradingRank: Long,
) {

    fun beUsable() = TextUtils.equals(status, "unused") || TextUtils.equals(status, "used")

    fun getRegistryContractAddress() = contractAddress[0]
    fun getNftContractAddress() = contractAddress[1]

    /**
     * 是否允许交易
     * @return Boolean
     */
    fun allowTransaction() = !payload.isNullOrEmpty()

    fun legalModel() = payload != "123456"

}