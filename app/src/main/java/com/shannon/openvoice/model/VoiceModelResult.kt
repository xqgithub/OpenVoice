package com.shannon.openvoice.model

import android.os.Parcelable
import android.text.TextUtils
import com.google.gson.annotations.SerializedName
import com.shannon.android.lib.extended.dataNullConvert
import com.shannon.android.lib.player.media.MediaData
import com.shannon.android.lib.web3.dapp.Parameters
import com.shannon.openvoice.business.player.MediaInfo
import kotlinx.parcelize.Parcelize
import java.util.*

/**
 *
 * @Package:        com.shannon.openvoice.model
 * @ClassName:      VoiceModelResult
 * @Description:     java类作用描述
 * @Author:         czhen
 */
@Parcelize
data class VoiceModelResult(
    val id: Long,
    val name: String,
    @SerializedName("model_type") val modelType: String,
    @SerializedName("model_source") val modelSource: String,
    val status: String,
    var activated: Boolean?,
    @SerializedName("is_model_owner") val isModelOwner: Boolean,
    @SerializedName("owner_account") val ownerAccount: TimelineAccount,
    @SerializedName("is_official") val isOfficial: Boolean,
    val price: String,
    val currency: String?,
    val isPlaying: Boolean?,
    val playlist: List<MediaInfo>?,
    val playingMediaId: String?,
    val playingProgress: Int?,

    @SerializedName("is_liked") val isLiked: Boolean,
    @SerializedName("creator_account") val creatorAccount: TimelineAccount,
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
    @SerializedName("audition_file") val auditionFile: String?,//官方模型的试听文件
    @SerializedName("like_count") val likeCount: Long,
    @SerializedName("usage_count") val usageCount: Long,
    @SerializedName("trading_count") val tradingCount: Long,
    @SerializedName("trading_rank") val tradingRank: Long,
) : Parcelable {

    fun beUsable() = TextUtils.equals(status, "unused") || TextUtils.equals(status, "used")

    @JvmName("getPlaylistJava")
    fun getPlaylist() = playlist ?: arrayListOf()

    @JvmName("getPlayingMediaIdJava")
    fun getPlayingMediaId() = playingMediaId ?: ""

    @JvmName("getPlayingProgressJava")
    fun getPlayingProgress() = playingProgress ?: 0

    fun getRegistryContractAddress() = contractAddress[0]
    fun getNftContractAddress() = contractAddress[1]


    fun convertDetail() = ModelDetail(
        id,
        name,
        modelType,
        modelSource,
        status,
        price,
        currency,
        isLiked,
        ownerAccount,
        isModelOwner,
        creatorAccount,
        isOfficial,
        metadataUrl,
        contractAddress,
        creatorAccountAddress,
        ownershipAccountAddress,
        royaltiesFee,
        chain,
        tokenId,
        tokenStrandards,
        serivceFee,
        payload,
        isMinted,
        createdAt,
        likeCount,
        usageCount, tradingCount, tradingRank
    )
}
