package com.shannon.openvoice.dialog

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.coroutineScope
import com.shannon.android.lib.R
import com.shannon.android.lib.extended.inflate
import com.shannon.android.lib.extended.showConfirmDialog
import com.shannon.android.lib.extended.singleClick
import com.shannon.android.lib.extended.visibility
import com.shannon.android.lib.util.ThemeUtil
import com.shannon.android.lib.util.ToastUtil
import com.shannon.android.lib.web3.dapp.DappProxy
import com.shannon.android.lib.web3.dapp.OnCallbackData
import com.shannon.android.lib.web3.dapp.SignMessage
import com.shannon.openvoice.business.pay.WalletListActivity
import com.shannon.openvoice.databinding.DialogModelPriceBinding
import com.shannon.openvoice.model.ModelDetail
import com.shannon.openvoice.util.DecimalDigitsLengthFilter
import com.shannon.openvoice.util.DecimalPointFilter
import com.shannon.openvoice.util.UnitHelper
import timber.log.Timber

/**
 *
 * @Package:        com.shannon.android.lib.components
 * @ClassName:      ConfirmDialog
 * @Description:     作用描述
 * @Author:         czhen
 * @CreateDate:     2022/8/9 10:28
 */
class ModelPriceDialog(
    private var modelInfo: ModelDetail,
    val onSave: (String, String) -> Unit
) : DialogFragment(), OnCallbackData {

    private val binding by inflate<DialogModelPriceBinding>()
    private val filters = arrayOf(DecimalPointFilter(), DecimalDigitsLengthFilter(9))
    private val mDappProxy by lazy { DappProxy.instance }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(
            STYLE_NO_TITLE,
            ThemeUtil.getAttrResourceId(requireContext(), R.attr.confirmDialogStyle)
        )
    }

    override fun onStart() {
        super.onStart()
        mDappProxy.initialize(requireContext(), lifecycle, this)
        mDappProxy.openConnect()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ) = binding.root

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.run {
            Timber.d("isMinted = ${if (!modelInfo.isMinted && modelInfo.payload.isNullOrEmpty()) true else modelInfo.isMinted}")
            connectWalletButton.singleClick { mDappProxy.requestSession() }
            switchWalletButton.singleClick { mDappProxy.switchWallet() }
            modelNameView.text = modelInfo.name
            byCreatorView.text = requireContext().getString(getSourceText(modelInfo.modelSource))
            currentPriceView.text = modelInfo.price.plus(" ").plus(modelInfo.currency)
            priceCurrencyView.text = modelInfo.currency
            priceView.filters = filters
            closeButton.singleClick { dismiss() }
            saveButton.singleClick {
                if (mDappProxy.getWalletAddress() == null) {
                    ToastUtil.showToast(getString(com.shannon.openvoice.R.string.select_wallet))
                    return@singleClick
                }

                if (!mDappProxy.getWalletAddress()
                        .equals(modelInfo.ownershipAccountAddress, true)
                ) {
                    requireContext().showConfirmDialog(
                        getString(com.shannon.openvoice.R.string.title_wallet_tips),
                        getString(
                            com.shannon.openvoice.R.string.content_wallet_tips,
                            modelInfo.ownershipAccountAddress
                        ),
                        getString(com.shannon.openvoice.R.string.change_wallets)
                    ) {
                        mDappProxy.triggerDeepLink()
                    }
                    return@singleClick
                }
                saveButton.isEnabled = false
                with(modelInfo) {
                    creatorAccountAddress ?: return@with
                    val modifyPrice = UnitHelper.fixPrice(binding.priceView.text.toString())
                    modelInfo = copy(price = modifyPrice)
                    val isMinted = if (!isMinted && payload.isNullOrEmpty()) true else isMinted
                    val signMessage = SignMessage(
                        isMinted,
                        creatorAccountAddress,
                        tokenId!!,
                        getRegistryContractAddress(),
                        getNftContractAddress(),
                        modifyPrice,
                        serivceFee,
                        royaltiesFee!!
                    )
                    mDappProxy.sendSignTypedData(signMessage)
                }
            }
            priceView.doAfterTextChanged { submitEnabled() }
        }

        val dialogWindow = dialog?.window
        val lp = dialogWindow?.attributes
        val d = requireContext().resources.displayMetrics
        lp?.width = (d.widthPixels * 0.8).toInt()
        dialogWindow?.attributes = lp
    }

    private fun submitEnabled() {
        val price = binding.priceView.text.toString()
        binding.saveButton.isEnabled = if (price.isNotEmpty()) {
            val priceDouble = price.toDouble()
            priceDouble > 0.0 && mDappProxy.getWalletAddress() != null
        } else false
    }

    private fun getSourceText(modelSource: String): Int {
        return when (modelSource) {
            "offical" -> com.shannon.openvoice.R.string.official
            "original" -> com.shannon.openvoice.R.string.original
            "purchase" -> com.shannon.openvoice.R.string.buy
            else -> com.shannon.openvoice.R.string.official
        }
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

    override fun onSignature(signature: String) {
        lifecycle.coroutineScope.launchWhenResumed {
            dismiss()
            onSave(modelInfo.price, signature)
        }
    }

    override fun onInterrupt() {
        requireActivity().runOnUiThread { submitEnabled() }
    }

    override fun noWalletFound() {
        startActivity(WalletListActivity.newIntent(requireContext()))
    }
}
