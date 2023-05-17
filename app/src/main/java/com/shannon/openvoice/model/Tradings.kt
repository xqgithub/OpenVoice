package com.shannon.openvoice.model

import com.google.gson.annotations.SerializedName
import java.util.*

/**
 *
 * @Package:        com.shannon.openvoice.model
 * @ClassName:      Tradings
 * @Description:     作用描述
 * @Author:         czhen
 * @CreateDate:     2022/9/13 14:46
 */
data class Tradings(
    val id: Long,
    @SerializedName("model_name") val modelName: String,
    @SerializedName("origin_price") val originPrice: String,
    val currency: String,
    @SerializedName("buyer_account") val buyerAccount: TimelineAccount,
    @SerializedName("seller_account") val sellerAccount: TimelineAccount,
    @SerializedName("trading_at") val tradingAt: Date,
    @SerializedName("trading_type") val tradingType: String,
    @SerializedName("transaction_hash") val transactionHash: String?

)
