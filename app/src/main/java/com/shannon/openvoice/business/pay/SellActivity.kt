package com.shannon.openvoice.business.pay

import android.content.Context
import android.content.Intent
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.core.widget.doAfterTextChanged
import com.bigkoo.pickerview.builder.TimePickerBuilder
import com.bigkoo.pickerview.view.TimePickerView
import com.blankj.utilcode.util.KeyboardUtils
import com.shannon.android.lib.base.activity.KBaseActivity
import com.shannon.android.lib.components.ShapeableButton
import com.shannon.android.lib.extended.*
import com.shannon.android.lib.util.ThemeUtil
import com.shannon.android.lib.util.ToastUtil
import com.shannon.android.lib.web3.dapp.DappProxy
import com.shannon.android.lib.web3.dapp.OnCallbackData
import com.shannon.android.lib.web3.dapp.SignMessage
import com.shannon.openvoice.R
import com.shannon.openvoice.databinding.ActivitySellBinding
import com.shannon.openvoice.extended.loadAvatar
import com.shannon.openvoice.model.EventSellModel
import com.shannon.openvoice.model.EventSign
import com.shannon.openvoice.model.EventUpdateModel
import com.shannon.openvoice.model.ModelDetail
import com.shannon.openvoice.util.DecimalDigitsLengthFilter
import com.shannon.openvoice.util.DecimalPointFilter
import com.shannon.openvoice.util.UnitHelper
import org.greenrobot.eventbus.EventBus
import timber.log.Timber
import java.text.SimpleDateFormat
import java.util.*
import kotlin.properties.Delegates

/**
 *
 * @Package:        com.shannon.openvoice.business.pay
 * @ClassName:      SellActivity
 * @Description:     作用描述
 * @Author:         czhen
 * @CreateDate:     2023/3/9 18:31
 */
class SellActivity : KBaseActivity<ActivitySellBinding, PaymentViewModel>(), OnCallbackData {
    private val mDappProxy by lazy { DappProxy.instance }
    private var modelId by Delegates.notNull<Long>()
    private var mModelDetail: ModelDetail? = null
    private var modifiedPrice: String? = null
    private val filters = arrayOf(DecimalPointFilter(), DecimalDigitsLengthFilter(4, 5))

    override fun onStart() {
        super.onStart()
        Timber.e("onStart ---------------------------- ")
        mDappProxy.initialize(context, lifecycle, this)
        mDappProxy.openConnect()
    }

    override fun onInit() {
        setToolbarBackground(R.color.transparent)
        setTitleText(R.string.model_sell)
        modelId = intent.getLongExtra(MODEL_ID, 0L)
        viewModel.modelDetail(modelId, onResult = this::bindViewData)
        binding.run {
            priceEditView.filters = filters
            priceEditView.doAfterTextChanged {
                subButtonEnable()
            }
            submitButton.singleClick { submit() }
            connectWalletButton.singleClick { mDappProxy.requestSession() }
            switchWalletButton.singleClick { mDappProxy.switchWallet() }
            saleView.singleClick { showTimePicker() }
            priceEditView.onFocusChangeListener =
                View.OnFocusChangeListener { view, hasFocus ->
                    if (!hasFocus) {
                        KeyboardUtils.hideSoftInput(view)
                    }
                }
        }
    }

    private fun bindViewData(detail: ModelDetail) {
        mModelDetail = detail
        binding.run {
            modelNameView.text = detail.name.replace("No.", "No.\n")
            priceView.text = detail.price
            unitView.text = detail.currency
            platformFeeView.text = detail.serivceFee.plus("%")
            subButtonEnable()
        }
    }

    private fun submit() {
        val currentTime = selectedDateTime?.time ?: System.currentTimeMillis()
        val threeMouth = threeMouth()
        Timber.d("currentTime = $currentTime")

        Timber.d("threeMouth = $threeMouth")
        val tenMinutes = tenMinutes()
        if (currentTime in tenMinutes..threeMouth) {
            sign()
        } else if (currentTime > threeMouth) {
            ToastUtil.showToast(getString(R.string.tips_sale_cycle_exceed))
        } else if (currentTime >= System.currentTimeMillis()) {
            ToastUtil.showToast(getString(R.string.tips_sale_cycle_below))
        } else {
            ToastUtil.showToast(getString(R.string.tip_sale_cycle_error))
        }
    }

    private fun threeMouth(): Long {
        val calendar = Calendar.getInstance()
        calendar.time = Date()
        calendar.add(Calendar.MONTH, 3)
        return calendar.time.time
    }

    private fun tenMinutes(): Long {
        val calendar = Calendar.getInstance()
        calendar.time = Date()
        calendar.add(Calendar.MINUTE, 10)
        return calendar.time.time
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
            showLoading()
            modifiedPrice = UnitHelper.fixPrice(binding.priceEditView.text.toString())
            val signMessage = SignMessage(
                isMinted,
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

    private fun subButtonEnable() {
        val price = binding.priceEditView.text.toString().nonNull("0.0")

        mModelDetail?.run {
            binding.submitButton.isEnabled =
                beUsable() && mDappProxy.getWalletAddress() != null && legalModel()
                        && price.toDouble() > 0.0 && selectedDateTime != null
        }
    }

    override fun onSessionStateUpdated(account: String?) {
        Timber.d("DappProxy : $account")
        binding.connectWalletLayout.visibility(account == null)
        binding.switchWalletLayout.visibility(account != null)
        subButtonEnable()
        binding.walletAddressView.text = account.orEmpty()
    }

    override fun onSocketState(state: OnCallbackData.SocketState) {
        val isEnabled = state == OnCallbackData.SocketState.SOCKET_CONNECTED
        binding.connectWalletButton.isEnabled = isEnabled
        binding.switchWalletButton.isEnabled = isEnabled
    }

    override fun noWalletFound() {
        startActivity(WalletListActivity.newIntent(context))
    }

    override fun onSignature(signature: String) {
        mModelDetail?.run {
            val saleCycle = (selectedDateTime?.time ?: System.currentTimeMillis()) / 1000
            viewModel.updateVoiceModel(
                true,
                modelId,
                price = modifiedPrice ?: price,
                payload = signature,
                saleCycle = saleCycle.toString()
            ) {
                EventBus.getDefault()
                    .post(EventSellModel(modelId, signature, modifiedPrice ?: price))
                onBackPressed()
            }
        }
    }

    override fun onInterrupt() {
        super.onInterrupt()
        dismissLoading()
    }

    private val mTimePicker: TimePickerView by lazy {
        initCustomTimePicker()
    }
    private val dateFormat = SimpleDateFormat("yy-MM-dd HH:mm", Locale.getDefault())
    private var selectedDateTime: Date? = null

    private fun showTimePicker() {
        mTimePicker.show()
    }

    private fun initCustomTimePicker(): TimePickerView {
        //系统当前时间
        val selectedDate: Calendar = Calendar.getInstance()
        selectedDate.time = Date()

        val startDate = Calendar.getInstance()
        startDate.set(
            startDate.get(Calendar.YEAR),
            startDate.get(Calendar.MONTH),
            startDate.get(Calendar.DAY_OF_MONTH),
            startDate.get(Calendar.HOUR_OF_DAY),
            startDate.get(Calendar.MINUTE)
        )
        Timber.d("hour: ${startDate.get(Calendar.HOUR)}")
        val endDate = Calendar.getInstance()
        endDate.set(2099, 11, 31, 23, 59)
        //时间选择器 ，自定义布局
        return TimePickerBuilder(context) { date, _ ->//选中事件回调
            Timber.d("OnTimeSelectListener: ${dateFormat.format(date)}")
            selectedDateTime = date
            binding.saleView.text = dateFormat.format(date)
            subButtonEnable()
        }.setDate(selectedDate)
            .setRangDate(startDate, endDate)
            .setLayoutRes(R.layout.view_sell_picker_time) { v ->
                val rootLayout: ConstraintLayout = v.findViewById(R.id.cl_main) as ConstraintLayout
                val tvConfirm: TextView = v.findViewById(R.id.sbSure) as ShapeableButton
                val tvCancel: TextView = v.findViewById(R.id.sbCancel) as ShapeableButton
                val tvMonthDay: TextView = v.findViewById(R.id.tv_month_day) as TextView
                val ivClose: ImageView = v.findViewById(R.id.iv_close) as ImageView

                //设置背景
                setDynamicShapeRectangle(
                    arrayOf(rootLayout),
                    CornerRadiusLeftTop = 16f.dp,
                    CornerRadiusRightTop = 16f.dp,
                    bgColor = arrayOf("#1F1F1F")
                )

                tvCancel.apply {
                    background =
                        ContextCompat.getDrawable(context, R.drawable.shape_primary_background3)
                }

                tvConfirm.singleClick {
                    mTimePicker.returnData()
                    mTimePicker.dismiss()
                }
                tvCancel.singleClick {
                    mTimePicker.dismiss()
                }
                ivClose.setOnClickListener {
                    mTimePicker.dismiss()
                }
            }
            .setContentTextSize(16)
            .setType(booleanArrayOf(true, true, true, true, true, false))
            .setLabel("", "", "", "", "", "秒")
            .setLineSpacingMultiplier(20f)
            .setTextXOffset(0, 0, 0, 40, 0, -40)
            .isCenterLabel(false) //是否只显示中间选中项的label文字，false则每项item全部都带有label。
            .setBgColor(ContextCompat.getColor(context, R.color.transparent))
            .setDividerColor(ContextCompat.getColor(context, R.color.transparent))
            .setTextColorCenter(
                ThemeUtil.getColor(context, R.attr.textColorPrimary)
            )
            .setTextColorOut(
                ContextCompat.getColor(context, R.color.color_A1A7AF)
            )
            .build()
    }


    companion object {

        private const val MODEL_ID = "modelId"

        fun newIntent(context: Context, modelId: Long) =
            Intent(context, SellActivity::class.java).apply {
                putExtra(MODEL_ID, modelId)
            }
    }
}