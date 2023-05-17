package com.shannon.openvoice.business.pay

import android.content.Context
import android.content.Intent
import android.view.View
import com.shannon.android.lib.base.activity.KBaseActivity
import com.shannon.android.lib.extended.*
import com.shannon.android.lib.util.ToastUtil
import com.shannon.android.lib.web3.contract.CallContractManager
import com.shannon.android.lib.web3.dapp.DappProxy
import com.shannon.android.lib.web3.dapp.OnCallbackData
import com.shannon.android.lib.web3.dapp.Parameters
import com.shannon.android.lib.web3.dapp.SignMessage
import com.shannon.openvoice.R
import com.shannon.openvoice.databinding.ActivityPaymentBinding
import com.shannon.openvoice.dialog.PaymentPriceDialog
import com.shannon.openvoice.extended.clip
import com.shannon.openvoice.extended.loadAvatar
import com.shannon.openvoice.model.EventLikeVoice
import com.shannon.openvoice.model.EventSign
import com.shannon.openvoice.model.EventUpdateModel
import com.shannon.openvoice.model.ModelDetail
import com.shannon.openvoice.util.TimestampUtils
import com.shannon.openvoice.util.openLink
import org.greenrobot.eventbus.EventBus
import timber.log.Timber
import java.util.*
import kotlin.properties.Delegates

/**
 *
 * @Package:        com.shannon.openvoice.business.pay
 * @ClassName:      PaymentActivity
 * @Description:     支付界面
 * @Author:         czhen
 * @CreateDate:     2022/10/25 9:24
 */
class PaymentActivity : KBaseActivity<ActivityPaymentBinding, PaymentViewModel>(), OnCallbackData {

    private val mDappProxy by lazy { DappProxy.instance }
    private var modelId by Delegates.notNull<Long>()
    private var isFollowing = false
    private var mModelDetail: ModelDetail? = null

    override fun onStart() {
        super.onStart()
        Timber.e("onStart ---------------------------- ")
        mDappProxy.initialize(context, lifecycle, this)
        mDappProxy.openConnect()
    }

    override fun onInit() {
        setTitleText(R.string.purchase_details)
        modelId = intent.getLongExtra(MODEL_ID, 0L)


        binding.run {
            connectWalletButton.singleClick { mDappProxy.requestSession() }
            switchWalletButton.singleClick { mDappProxy.switchWallet() }
            followButton.singleClick {
                mModelDetail?.run {
                    viewModel.follow(isFollowing, account.id, this@PaymentActivity::followResult)
                }
            }
            buyButton.singleClick {
                if (mDappProxy.getWalletAddress() == null) {
                    ToastUtil.showToast(getString(R.string.select_wallet))
                    return@singleClick
                }
                buy()
            }
            signButton.singleClick {
                mModelDetail?.run {
                    if (mDappProxy.getWalletAddress() == null) {
                        ToastUtil.showToast(getString(R.string.select_wallet))
                        return@singleClick
                    }
                    sign()
                }
            }
            copyButton.singleClick {
                mModelDetail?.run {
                    if (tokenId != null && clip(tokenId))
                        ToastUtil.showToast(getString(R.string.tips_tokenid_copied))
                }
            }
            likeButton.singleClick(this@PaymentActivity::likeClick)
            contractAddressView.singleClick {
                mModelDetail?.run {
                    val contractUrl = "https://etherscan.io/address/${getRegistryContractAddress()}"
                    openLink(contractUrl)
                }
            }
        }

        viewModel.modelDetail(modelId, onResult = this::bindViewData)

    }

    private fun bindViewData(detail: ModelDetail) {
        mModelDetail = detail
        binding.run {
            modelNameView.text = detail.name
            currentPriceView.text = detail.price.plus(" ").plus(detail.currency)
            userAvatarView.loadAvatar(detail.account.avatar)
            displayNameView.text = detail.account.name
            userNameView.text = getString(
                R.string.post_username_format,
                detail.account.username
            )
            setTimestampInfo(detail.createdAt)
            buyButtonEnable()
            likeButton.isSelected = detail.isLiked
            likeButton.isEnabled = detail.beUsable()
            contractAddressView.text = detail.getRegistryContractAddress()
            tokenView.text = detail.tokenId
            tokenStandardView.text = detail.tokenStrandards
            blockchainView.text = detail.chain
            platformFeeView.text = detail.serivceFee.plus("%")
            creatorEarningsView.text = detail.royaltiesFee.plus("%")
            if (!detail.isModelOwner)
                viewModel.following(detail.account.id, this@PaymentActivity::followResult)
            buyButton.visibility(!detail.isModelOwner && !detail.isOfficial)
            signButton.visibility(detail.isModelOwner && !detail.allowTransaction())
        }
    }

    private fun buyButtonEnable() {
        mModelDetail?.run {
            binding.buyButton.isEnabled =
                beUsable() && allowTransaction() && mDappProxy.getWalletAddress() != null && legalModel()
        }
    }

    private fun setTimestampInfo(createAt: Date?) {
        createAt ?: return
        val then = createAt.time
        val now = System.currentTimeMillis()
        binding.createTimeView.text =
            TimestampUtils.getRelativeTimeSpanString(context, then, now)
    }

    private fun followResult(following: Boolean) {
        binding.followButton.visible()
        isFollowing = following
        binding.followButton.text =
            context.getString(if (following) R.string.button_unfollow else R.string.follow)
    }

    private fun likeClick(view: View) {
        mModelDetail?.run {
            viewModel.likeVoiceModel(isLiked, modelId) {
                mModelDetail = copy(isLiked = it)
                binding.likeButton.isSelected = it
                EventBus.getDefault().post(EventLikeVoice(modelId, it, hashCode()))
            }
        }
    }

    private fun buy() {
        mModelDetail?.run {
            payload ?: return@run
            metadataUrl ?: return@run
            val walletAddress = mDappProxy.getWalletAddress() ?: return
            showLoading()
            viewModel.buyModel(modelId, walletAddress) {
                //如果is_minted为false的话 就进行铸造
                if (isMinted) transferNFT() else mintNFT()
            }
        }
    }

    private var modifiedPrice: String? = null
    private fun modifyPrice() {
        mModelDetail?.run {
            PaymentPriceDialog(
                context,
                this,
                onDismiss = {
                    sign()
                }
            ) { price ->
                modifiedPrice = price
            }.show()
        }
    }

    private fun sign() {
        mModelDetail?.run {
            creatorAccountAddress ?: return
            ownershipAccountAddress ?: return
            tokenId ?: return
            royaltiesFee ?: return

            if (!mDappProxy.getWalletAddress().equals(ownershipAccountAddress, true)) {
                showConfirmDialog(
                    getString(R.string.title_wallet_tips),
                    getString(R.string.content_wallet_tips, ownershipAccountAddress),
                    getString(R.string.change_wallets)
                ) {
                    mDappProxy.triggerDeepLink()
                }
                return
            }
            val signMessage = SignMessage(
                true,
                creatorAccountAddress,
                tokenId,
                getRegistryContractAddress(),
                getNftContractAddress(),
                modifiedPrice ?: price,
                serivceFee,
                royaltiesFee
            )
            mDappProxy.sendSignTypedData(signMessage)
        }
    }


    companion object {
        private const val MODEL_ID = "modelId"
        fun newIntent(context: Context): Intent {
            return Intent(context, PaymentActivity::class.java)
        }

        fun newIntent(context: Context, modelId: Long) =
            Intent(context, PaymentActivity::class.java).apply {
                putExtra(MODEL_ID, modelId)
            }
    }

    override fun onSessionStateUpdated(account: String?) {
        Timber.d("DappProxy : $account")
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

    override fun onTransactionHash(hash: String) {
        mModelDetail?.run {
            loadContractManager().fetchTransactionReceipt(hash)
            transactionSuccessCallback(hash)
        }
    }

    private fun transactionSuccessCallback(hash: String) {
        val walletAddress = mDappProxy.getWalletAddress() ?: return
        mModelDetail?.run {
            viewModel.buyModelDone(modelId, hash, walletAddress, {
                dismissLoading()
                onBackPressed()
            }) {
                mModelDetail = copy(ownershipAccountAddress = walletAddress)
                binding.buyButton.gone()
                binding.signButton.visible()
                dismissLoading()
                showDialogOnlyConfirm(
                    getString(R.string.title_successful_purchase),
                    getString(R.string.content_successful_purchase),
                    getString(R.string.set_price),
                    isCanceledOnTouchOutside = false,
                    doConfirm = { modifyPrice() })
            }
        }
    }

    override fun onInterrupt() {
        runOnUiThread { dismissLoading() }
    }

    override fun onSignature(signature: String) {
        mModelDetail?.run {
            viewModel.updateVoiceModel(
                true,
                modelId,
                price = modifiedPrice ?: price,
                payload = signature
            ) {
                modifiedPrice?.let { price ->
                    EventBus.getDefault().post(EventUpdateModel(modelId, price))
                    mModelDetail = copy(price = price)
                }
                EventBus.getDefault().post(EventSign(modelId, signature))
                if (payload.isNullOrEmpty()) {
                    mModelDetail = copy(payload = signature)
                    bindViewData(mModelDetail!!)
                } else {
                    ToastUtil.showCenter(getString(R.string.model_purchase_succ))
                    onBackPressed()
                }
            }
        }
    }

    override fun noWalletFound() {
        startActivity(WalletListActivity.newIntent(context))
    }

    private fun mintNFT() {
        mModelDetail?.run {
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
        mModelDetail?.run {
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
            mModelDetail!!.creatorAccountAddress!!,
            mModelDetail!!.tokenId!!,
            mModelDetail!!.getRegistryContractAddress(),
            mModelDetail!!.getNftContractAddress(),
            mModelDetail!!.price,
            mModelDetail!!.serivceFee,
            mModelDetail!!.royaltiesFee!!
        )
    }

    private var callContractManager: CallContractManager? = null
    private fun loadContractManager(): CallContractManager {
        return callContractManager ?: CallContractManager.create(
            mModelDetail!!.getRegistryContractAddress(),
            mModelDetail!!.getNftContractAddress(),
        )
            .also { callContractManager = it }
    }

}