package com.shannon.openvoice.business.creation.detail

import android.os.Bundle
import com.shannon.android.lib.base.activity.KBaseFragment
import com.shannon.android.lib.extended.nonNull
import com.shannon.android.lib.extended.singleClick
import com.shannon.android.lib.extended.visibility
import com.shannon.android.lib.util.ToastUtil
import com.shannon.openvoice.R
import com.shannon.openvoice.business.pay.PaymentActivity
import com.shannon.openvoice.business.pay.PaymentViewModel
import com.shannon.openvoice.databinding.FragmentModelInfoBinding
import com.shannon.openvoice.event.UniversalEvent
import com.shannon.openvoice.extended.clip
import com.shannon.openvoice.extended.loadAvatar
import com.shannon.openvoice.model.ModelDetail
import com.shannon.openvoice.util.EtherScanUtil
import com.shannon.openvoice.util.TimestampUtils
import com.shannon.openvoice.util.openLink
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import java.util.*
import kotlin.properties.Delegates

/**
 *
 * @Package:        com.shannon.openvoice.business.creation.detail
 * @ClassName:      ModelInfoFragment
 * @Description:     作用描述
 * @Author:         czhen
 * @CreateDate:     2023/3/9 11:33
 */
class ModelInfoFragment : KBaseFragment<FragmentModelInfoBinding, PaymentViewModel>() {
    private var modelId by Delegates.notNull<Long>()
    private var mModelDetail: ModelDetail? = null

    override fun onInit() {
        EventBus.getDefault().register(this)
        modelId = arguments?.getLong(MODEL_ID_ARGS, 0L) ?: 0L
        viewModel.modelDetail(modelId, onResult = this::bindViewData)
        binding.run {
            copyButton.singleClick {
                mModelDetail?.run {
                    if (tokenId != null && requireContext().clip(tokenId))
                        ToastUtil.showToast(getString(R.string.tips_tokenid_copied))
                }
            }
            contractAddressView.singleClick {
                mModelDetail?.run {
                    requireContext().openLink(
                        EtherScanUtil.getContractUrl(
                            getRegistryContractAddress()
                        )
                    )
                }
            }
        }
    }

    private fun bindViewData(detail: ModelDetail) {
        mModelDetail = detail
        binding.run {
            setTimestampInfo(detail.createdAt)
            contractAddressView.text = detail.getRegistryContractAddress()
            tokenView.text = detail.tokenId.nonNull("-")
            tokenStandardView.text = detail.tokenStrandards
            blockchainView.text = detail.chain
            platformFeeView.text = detail.serivceFee.plus("%")
            creatorEarningsView.text =
                if (detail.royaltiesFee == null) "-"
                else detail.royaltiesFee.nonNull("-").plus("%")
            creatorNameView.text = detail.creatorAccount.username
            ownerNameView.text = detail.account.username
            creatorWalletView.text = detail.creatorAccountAddress.nonNull("-")
            ownerWalletView.text = detail.ownershipAccountAddress.nonNull("-")
        }
    }


    private fun setTimestampInfo(createAt: Date?) {
        createAt ?: return
        val then = createAt.time
        val now = System.currentTimeMillis()
        binding.createTimeView.text =
            TimestampUtils.getRelativeTimeSpanString(requireContext(), then, now)
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun subscribePaymentEvent(event: UniversalEvent) {
        if (event.actionType != UniversalEvent.payment) return
        val detail = event.message as ModelDetail
        if (detail.id != modelId) return
        bindViewData(detail)
    }

    override fun onDestroy() {
        super.onDestroy()
        EventBus.getDefault().unregister(this)
    }

    companion object {
        private const val MODEL_ID_ARGS = "modelId"
        fun newInstance(modelId: Long): ModelInfoFragment {
            val fragment = ModelInfoFragment()
            fragment.arguments = Bundle().apply {
                putLong(MODEL_ID_ARGS, modelId)
            }
            return fragment
        }
    }
}