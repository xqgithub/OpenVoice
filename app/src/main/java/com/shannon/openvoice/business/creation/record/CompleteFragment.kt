package com.shannon.openvoice.business.creation.record

import  android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ListPopupWindow
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.activityViewModels
import com.shannon.android.lib.BuildConfig
import com.shannon.android.lib.base.activity.KBaseFragment
import com.shannon.android.lib.base.viewmodel.EmptyViewModel
import com.shannon.android.lib.constant.ConfigConstants
import com.shannon.android.lib.extended.showConfirmDialog
import com.shannon.android.lib.extended.showDialogOnlyConfirmWithIcon
import com.shannon.android.lib.extended.singleClick
import com.shannon.android.lib.extended.visibility
import com.shannon.android.lib.util.ToastUtil
import com.shannon.android.lib.web3.contract.CallContractManager
import com.shannon.android.lib.web3.dapp.DappProxy
import com.shannon.android.lib.web3.dapp.OnCallbackData
import com.shannon.android.lib.web3.dapp.SignMessage
import com.shannon.openvoice.R
import com.shannon.openvoice.business.creation.CreationViewModel
import com.shannon.openvoice.business.pay.WalletListActivity
import com.shannon.openvoice.databinding.FragmentCompleteRecorderBinding
import com.shannon.openvoice.extended.getDrawableKt
import com.shannon.openvoice.model.EventRecorder
import com.shannon.openvoice.model.ModelDetail
import com.shannon.openvoice.model.VoiceModelResult
import com.shannon.openvoice.util.DecimalDigitsLengthFilter
import com.shannon.openvoice.util.DecimalPointFilter
import com.shannon.openvoice.util.UnitHelper
import org.greenrobot.eventbus.EventBus

/**
 *
 * @Package:        com.shannon.openvoice.business.creation.record
 * @ClassName:      CompleteFragment
 * @Description:     作用描述
 * @Author:         czhen
 * @CreateDate:     2022/8/16 11:13
 */
class CompleteFragment : KBaseFragment<FragmentCompleteRecorderBinding, EmptyViewModel>(),
    OnCallbackData {
    private val mViewModel by activityViewModels<CreationViewModel>()

    private val filters = arrayOf(DecimalPointFilter(), DecimalDigitsLengthFilter(4,5))
    private val earningsFilters = arrayOf(DecimalPointFilter(), DecimalDigitsLengthFilter(1, 2))

    private val mCurrencyAdapter by lazy { CurrencyAdapter(requireContext()) }
    private val listPopupWindow by lazy {
        ListPopupWindow(requireContext(), null).apply {
            setAdapter(mCurrencyAdapter)
            anchorView = binding.currencyLayout
            isModal = true
            setOnItemClickListener(this@CompleteFragment::onPopupItemClick)
            setBackgroundDrawable(requireContext().getDrawableKt(R.drawable.shap_list_popup_background))
        }
    }
    private val mDappProxy by lazy { DappProxy.instance }

    private var invitationCodeID: Int = -1

    companion object {
        fun newInstance(invitationCodeID: Int): CompleteFragment {
            val fragment = CompleteFragment()
            if (invitationCodeID > 0) {
                fragment.arguments = Bundle().apply {
                    putInt(RecordingNotesActivity.INVITATIONCODEID, invitationCodeID)
                }
            }
            return fragment
        }
    }

    override fun onStart() {
        super.onStart()
        mDappProxy.initialize(requireContext(), lifecycle, this)
        mDappProxy.openConnect()
    }

    override fun onInit() {
        arguments?.getInt(RecordingNotesActivity.INVITATIONCODEID)?.let {
            invitationCodeID = it
        }
        binding.run {
            connectWalletButton.singleClick { mDappProxy.requestSession() }
            switchWalletButton.singleClick { mDappProxy.switchWallet() }
            priceView.filters = filters
            creatorEarningsView.filters = earningsFilters
            currencyLayout.singleClick { listPopupWindow.show() }
            submitButton.singleClick(this@CompleteFragment::submit)
            priceView.doAfterTextChanged { submitEnabled() }
            creatorEarningsView.doAfterTextChanged { submitEnabled() }

        }
        mViewModel.currencyLive.observe(this, this::observeCurrency)
    }

    private fun submitEnabled() {
        val price = binding.priceView.text.toString()
        val earnings = binding.creatorEarningsView.text.toString()
        binding.submitButton.isEnabled = if (price.isNotEmpty() && earnings.isNotEmpty()) {
            val priceDouble = price.toDouble()
            val earningsDouble = earnings.toDouble()
            priceDouble > 0.0 && earningsDouble >= 0.0 && mDappProxy.getWalletAddress() != null
        } else false
    }

    private fun onPopupItemClick(parent: AdapterView<*>, view: View, position: Int, id: Long) {
        mViewModel.currencyLive.value = mCurrencyAdapter.setSelectedCurrency(position)
        listPopupWindow.dismiss()
    }

    private fun observeCurrency(currency: String) {
        binding.currencyView.text = currency
        binding.priceCurrencyView.text = currency
    }

    private fun submit(v: View) {
        val price = binding.priceView.text.toString()
        if (price.isEmpty()) {
            ToastUtil.showCenter(getString(R.string.tips_set_price))
            return
        }

        val earnings = binding.creatorEarningsView.text.toString()
        if (earnings.isNotEmpty()) {
            val earningsDouble = earnings.toDouble()
            if (earningsDouble > 10.0) {
                ToastUtil.showToast(getString(R.string.desc_creator_earnings))
                return
            }
        }

        if (mDappProxy.getWalletAddress() == null) {
            ToastUtil.showToast(getString(R.string.select_wallet))
            return
        }

        showLoading()
        val walletAddress = mDappProxy.getWalletAddress()!!
        if (BuildConfig.BUILD_TYPE == ConfigConstants.BuildType.RELEASE) {
            mViewModel.uploadVoiceToS3(
                UnitHelper.fixPrice(price),
                walletAddress,
                UnitHelper.fixPrice(earnings), invitationCodeID, this::submitSuccess
            )
        } else {
            mViewModel.uploadVoice(
                UnitHelper.fixPrice(price),
                walletAddress,
                UnitHelper.fixPrice(earnings), invitationCodeID, this::submitSuccess
            )
        }
    }

    private fun submitSuccess(result: VoiceModelResult){
        dismissLoading()
        showSuccessDialog()
    }


    private fun showSuccessDialog() {
        requireContext().showDialogOnlyConfirmWithIcon(
            getString(
                R.string.wait_model_generation
            ),
            R.drawable.icon_post_success
        ) {
            requireActivity().finish()
        }
    }


    override fun handleOnBackPressed(): Boolean {
        requireContext().showConfirmDialog(message = getString(R.string.abandon_submit)) {
            EventBus.getDefault().post(EventRecorder(null))
            onBackPressed()
        }
        return true
    }

    override fun onSessionStateUpdated(account: String?) {
        binding.connectWalletLayout.visibility(account == null)
        binding.switchWalletLayout.visibility(account != null)
        submitEnabled()
        binding.walletAddressView.text = account.orEmpty()
    }

    override fun onSocketState(state: OnCallbackData.SocketState) {
        val isEnabled = state == OnCallbackData.SocketState.SOCKET_CONNECTED
        binding.connectWalletButton.isEnabled = isEnabled
        binding.switchWalletButton.isEnabled = isEnabled
    }

    override fun onInterrupt() {
        requireActivity().runOnUiThread { dismissLoading() }
    }

    override fun noWalletFound() {
        startActivity(WalletListActivity.newIntent(requireContext()))
    }
}