package com.shannon.openvoice.dialog

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import com.bigkoo.pickerview.builder.TimePickerBuilder
import com.bigkoo.pickerview.view.TimePickerView
import com.blankj.utilcode.util.ScreenUtils
import com.blankj.utilcode.util.ThreadUtils.runOnUiThread
import com.shannon.android.lib.base.activity.BaseSheetDialogFragment
import com.shannon.android.lib.base.activity.BottomSheetFixedHeightDialog
import com.shannon.android.lib.components.ShapeableButton
import com.shannon.android.lib.extended.*
import com.shannon.android.lib.util.ThemeUtil
import com.shannon.android.lib.util.ToastUtil
import com.shannon.android.lib.web3.contract.CallContractManager
import com.shannon.android.lib.web3.dapp.DappProxy
import com.shannon.android.lib.web3.dapp.OnCallbackData
import com.shannon.android.lib.web3.dapp.Parameters
import com.shannon.android.lib.web3.dapp.SignMessage
import com.shannon.openvoice.FunApplication
import com.shannon.openvoice.R
import com.shannon.openvoice.business.main.mine.account.AccountManager
import com.shannon.openvoice.business.pay.PaymentViewModel
import com.shannon.openvoice.business.pay.SellActivity
import com.shannon.openvoice.business.pay.WalletListActivity
import com.shannon.openvoice.databinding.DialogModelPriceBinding
import com.shannon.openvoice.databinding.DialogPaymentBinding
import com.shannon.openvoice.event.UniversalEvent
import com.shannon.openvoice.extended.loadAvatar
import com.shannon.openvoice.model.ModelDetail
import org.greenrobot.eventbus.EventBus
import timber.log.Timber
import java.text.SimpleDateFormat
import java.util.*

/**
 *
 * @Package:        com.shannon.openvoice.dialog
 * @ClassName:      PaymentDialog
 * @Description:     作用描述
 * @Author:         czhen
 * @CreateDate:     2023/3/9 14:42
 */
class PaymentDialog(val mContext: Context, var detail: ModelDetail) :
    BaseSheetDialogFragment<PaymentViewModel>(), OnCallbackData {
    private val binding by inflate<DialogPaymentBinding>()
    private val mDappProxy by lazy { DappProxy.instance }
    private var trading = false

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return BottomSheetFixedHeightDialog(
            mContext, R.style.TransparentBottomDialog,
            (ScreenUtils.getScreenHeight() * 0.7).toInt()
        )
    }

    override fun onStart() {
        super.onStart()
        Timber.e("onStart ---------------------------- ")
        mDappProxy.initialize(mContext, lifecycle, this)
        mDappProxy.openConnect()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ) = binding.root

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        binding.run {
            initData()
            connectWalletButton.singleClick { mDappProxy.requestSession() }
            switchWalletButton.singleClick { mDappProxy.switchWallet() }
            buyButton.singleClick(this@PaymentDialog::buyClick)
        }
    }

    private fun initData() {
        binding.apply {
            priceView.text = detail.price
            userAvatarView.loadAvatar(detail.account.avatar)
            statusDisplayName.text = detail.account.name
            userNameView.text = getString(
                R.string.post_username_format,
                detail.account.username
            )
        }
    }

    private fun buyClick(view: View) {
        if (mDappProxy.getWalletAddress() == null) {
            ToastUtil.showToast(getString(R.string.select_wallet))
            return
        }
        buy()
    }

    private fun buy() {
        detail.run {
            payload ?: return@run
            metadataUrl ?: return@run
            val walletAddress = mDappProxy.getWalletAddress() ?: return

            binding.buyButton.isEnabled = false
            showLoading()
            viewModel.modelDetail(id, false) {
                if (it.status == "deleted") {
                    EventBus.getDefault().post(it)
                    ToastUtil.showCenter(getString(R.string.tips_deleted_model))
                    dismissLoading()
                    dismiss()
                } else if (it.payload.isNullOrEmpty()) {
                    EventBus.getDefault().post(it)
                    ToastUtil.showCenter(getString(R.string.tips_unsell_model))
                    dismissLoading()
                    dismiss()
                } else {
                    viewModel.buyModel(id, walletAddress) {
                        //如果is_minted为false的话 就进行铸造
                        if (isMinted) transferNFT() else mintNFT()
                    }
                }
            }

        }
    }

    private fun mintNFT() {
        detail.run {
            payload ?: return@run
            metadataUrl ?: return@run
            creatorAccountAddress ?: return@run

            val signMessage = getSignMessage()
            val mintData =
                loadContractManager().mint(
                    signMessage,
                    ownershipAccountAddress!!,
                    mDappProxy.getWalletAddress()!!,
                    payload
                )
            mDappProxy.sendMintTransaction(
                getRegistryContractAddress(),
                mintData,
                Parameters.calculateConsumptionAmount(price, serivceFee, "0.0")
            )
        }
    }

    private fun transferNFT() {
        detail.run {
            payload ?: return@run
            metadataUrl ?: return@run
            creatorAccountAddress ?: return@run

            val signMessage = getSignMessage()
            val transferFromData =
                loadContractManager().transferFrom(
                    signMessage,
                    ownershipAccountAddress!!,//"0xEe24Ddb5AA5f1dC7cf50498B3D97289095B1CDc4",//ownershipAccountAddress,
                    mDappProxy.getWalletAddress()!!,
                    payload
                )
            mDappProxy.sendTransaction(
                getRegistryContractAddress(),
                transferFromData,
                Parameters.calculateConsumptionAmount(price, serivceFee, royaltiesFee!!)
            )
        }
    }

    private fun getSignMessage(): SignMessage {
        return SignMessage(
            true,
            detail.creatorAccountAddress!!,
            detail.tokenId!!,
            detail.getRegistryContractAddress(),
            detail.getNftContractAddress(),
            detail.price,
            detail.serivceFee,
            detail.royaltiesFee!!
        )
    }

    private fun transactionSuccessCallback(hash: String) {
        val walletAddress = mDappProxy.getWalletAddress() ?: return
        detail.run {
            viewModel.buyModelDone(id, hash, walletAddress, {
                dismissLoading()
                this@PaymentDialog.dismiss()
            }) {
                val account = AccountManager.accountManager.getAccountData()
                detail = copy(
                    ownershipAccountAddress = walletAddress,
                    payload = "",
                    account = detail.account.copy(
                        id = account.id,
                        username = account.username,
                        displayName = account.name,
                        avatar = account.avatar
                    )
                )
                initData()
                EventBus.getDefault().post(UniversalEvent(UniversalEvent.payment, detail))
                binding.buyButton.gone()
                binding.signButton.visible()
                dismissLoading()
                requireContext().showConfirmDialog(
                    getString(R.string.title_successful_purchase),
                    getString(R.string.content_successful_purchase),
                    getString(R.string.button_sell),
                    getString(R.string.sure),
                    isCanceledOnTouchOutside = false,
                    doCancel = { this@PaymentDialog.dismiss() },
                    doConfirm = {
                        this@PaymentDialog.dismiss()
                        startActivity(SellActivity.newIntent(requireContext(), detail.id))
                    })
            }
        }
    }

    private var callContractManager: CallContractManager? = null
    private fun loadContractManager(): CallContractManager {
        return callContractManager ?: CallContractManager.create(
            detail.getRegistryContractAddress(),
            detail.getNftContractAddress(),
        )
            .also { callContractManager = it }
    }

    private fun buyButtonEnable() {
        detail.run {
            binding.buyButton.isEnabled =
                beUsable() && allowTransaction() && mDappProxy.getWalletAddress() != null && legalModel()
                        && !trading
        }
    }

    override fun onSessionStateUpdated(account: String?) {
        Timber.tag("DappProxy").d("onSessionStateUpdated: $account")
        binding.connectWalletLayout.visibility(account == null)
        binding.switchWalletLayout.visibility(account != null)
        buyButtonEnable()
        binding.walletAddressView.text = account.orEmpty()
    }

    override fun onSocketState(state: OnCallbackData.SocketState) {
        val isEnabled = state == OnCallbackData.SocketState.SOCKET_CONNECTED
        binding.connectWalletButton.isEnabled = isEnabled
        binding.switchWalletButton.isEnabled = isEnabled
    }

    override fun noWalletFound() {
        startActivity(WalletListActivity.newIntent(mContext))
    }

    override fun onTransactionHash(hash: String) {
        detail.run {
            trading = true
            buyButtonEnable()
            loadContractManager().fetchTransactionReceipt(hash)
            transactionSuccessCallback(hash)
        }
    }

    override fun onInterrupt() {
        runOnUiThread {
            buyButtonEnable()
            dismissLoading()
        }
    }
}