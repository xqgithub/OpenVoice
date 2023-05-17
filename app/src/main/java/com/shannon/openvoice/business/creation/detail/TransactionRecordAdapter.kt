package com.shannon.openvoice.business.creation.detail

import com.shannon.android.lib.base.adapter.BaseFunBindingRecyclerViewAdapter
import com.shannon.android.lib.extended.singleClick
import com.shannon.android.lib.extended.visibility
import com.shannon.openvoice.R
import com.shannon.openvoice.databinding.ItemDetailTransactionRecordBinding
import com.shannon.openvoice.extended.loadAvatar
import com.shannon.openvoice.model.Tradings
import com.shannon.openvoice.util.EtherScanUtil
import com.shannon.openvoice.util.TimestampUtils
import com.shannon.openvoice.util.openLink

/**
 *
 * @Package:        com.shannon.openvoice.business.creation
 * @ClassName:      TransactionRecordAdapter
 * @Description:     作用描述
 * @Author:         czhen
 * @CreateDate:     2022/8/25 10:17
 */
class TransactionRecordAdapter :
    BaseFunBindingRecyclerViewAdapter<Tradings, ItemDetailTransactionRecordBinding>() {

    override fun bindView(
        binding: ItemDetailTransactionRecordBinding,
        bean: Tradings,
        position: Int
    ) {
        binding.run {
            val context = root.context
            modelNameView.text = bean.modelName
            unitView.text = bean.currency
            priceView.text = bean.originPrice
            timestampView.text = TimestampUtils.formatDate(bean.tradingAt)

            sellerAvatarView.loadAvatar(bean.sellerAccount.avatar)
            sellerDisplayNameView.text = bean.sellerAccount.name
            sellerUserNameView.text =
                context.getString(R.string.post_username_format, bean.sellerAccount.username)

            buyerAvatarView.loadAvatar(bean.buyerAccount.avatar)
            buyerDisplayNameView.text = bean.buyerAccount.name
            buyerUserNameView.text =
                context.getString(R.string.post_username_format, bean.buyerAccount.username)
            hashView.visibility(!bean.transactionHash.isNullOrEmpty())
            bean.transactionHash?.run {
                hashView.text = this
            }
            hashView.singleClick {
                it.context.openLink(EtherScanUtil.getTransactionUrl(bean.transactionHash ?: ""))
            }
            hashView.visibility(!bean.transactionHash.isNullOrEmpty())
        }
    }

}