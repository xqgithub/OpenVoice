package com.shannon.openvoice.business.creation

import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.text.bold
import androidx.core.text.buildSpannedString
import androidx.core.text.color
import androidx.core.text.scale
import com.shannon.android.lib.base.adapter.BaseFunBindingRecyclerViewAdapter
import com.shannon.android.lib.util.ThemeUtil
import com.shannon.openvoice.R
import com.shannon.openvoice.databinding.ItemTransactionRecordBinding
import com.shannon.openvoice.extended.loadAvatar
import com.shannon.openvoice.model.TimelineAccount
import com.shannon.openvoice.model.Tradings
import com.shannon.openvoice.util.TimestampUtils

/**
 *
 * @Package:        com.shannon.openvoice.business.creation
 * @ClassName:      TransactionRecordAdapter
 * @Description:     作用描述
 * @Author:         czhen
 * @CreateDate:     2022/8/25 10:17
 */
class TransactionRecordAdapter :
    BaseFunBindingRecyclerViewAdapter<Tradings, ItemTransactionRecordBinding>() {

    override fun bindView(binding: ItemTransactionRecordBinding, bean: Tradings, position: Int) {
        binding.run {
            val context = root.context
            modelNameView.text = bean.modelName
            priceView.text = bean.originPrice
            timestampView.text = TimestampUtils.formatDate(bean.tradingAt)

            if (bean.tradingType == "bought") {
//                modelSourceViw.text = context.getString(R.string.notice_purchased)
                setNameInfo(userNameView, bean.sellerAccount)
                bidIconView.setImageResource(R.drawable.icon_trade_purchase)
            } else {
//                modelSourceViw.text = context.getString(R.string.notice_sold)
                setNameInfo(userNameView, bean.buyerAccount)
                bidIconView.setImageResource(R.drawable.icon_trade_sale)
            }
        }
    }

    private fun setNameInfo(statusDisplayName: TextView, account: TimelineAccount) {
        if (account.fullName == null) {
            account.fullName = buildSpannedString() {
                bold {
                    color(
                        ThemeUtil.getColor(
                            statusDisplayName.context,
                            android.R.attr.textColorSecondary
                        )
                    ) {
                        append(account.name)
                    }
                }
                append(" ")
                color(ThemeUtil.getColor(statusDisplayName.context, android.R.attr.textColorLink)) {
                    scale(0.9f) {
                        append(
                            statusDisplayName.context.getString(
                                R.string.post_username_format,
                                account.username
                            )
                        )
                    }
                }
            }
        }
        statusDisplayName.text = account.fullName
    }
}