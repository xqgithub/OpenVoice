package com.shannon.openvoice.dialog

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.DialogFragment
import com.shannon.android.lib.R
import com.shannon.android.lib.extended.*
import com.shannon.android.lib.util.ThemeUtil
import com.shannon.android.lib.util.ToastUtil
import com.shannon.android.lib.web3.dapp.DappProxy
import com.shannon.android.lib.web3.dapp.OnCallbackData
import com.shannon.android.lib.web3.dapp.SignMessage
import com.shannon.openvoice.databinding.DialogModelPriceBinding
import com.shannon.openvoice.model.ModelDetail
import com.shannon.openvoice.model.ModelInfo
import com.shannon.openvoice.util.DecimalDigitsLengthFilter
import com.shannon.openvoice.util.DecimalPointFilter
import com.shannon.openvoice.util.UnitHelper

/**
 *
 * @Package:        com.shannon.android.lib.components
 * @ClassName:      ConfirmDialog
 * @Description:     作用描述
 * @Author:         czhen
 * @CreateDate:     2022/8/9 10:28
 */
class PaymentPriceDialog(
    val mContext: Context,
    private val modelInfo: ModelDetail,
    val onDismiss: () -> Unit = {},
    val onSave: (String) -> Unit
) :
    Dialog(mContext, ThemeUtil.getAttrResourceId(mContext, R.attr.confirmDialogStyle)) {

    private val binding by inflate<DialogModelPriceBinding>()
    private val filters = arrayOf(DecimalPointFilter(), DecimalDigitsLengthFilter(9))

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding.run {
            setCanceledOnTouchOutside(false)
            setCancelable(false)
            walletLayout.gone()
            modelNameView.text = modelInfo.name
            byCreatorView.text = context.getString(com.shannon.openvoice.R.string.buy)
            currentPriceView.text = modelInfo.price.plus(" ").plus(modelInfo.currency)
            priceCurrencyView.text = modelInfo.currency
            priceView.filters = filters
            closeButton.gone()
            closeButton.singleClick { dismiss() }
            saveButton.singleClick {
                onSave(UnitHelper.fixPrice(binding.priceView.text.toString()))
                dismiss()
            }
            priceView.doAfterTextChanged {
                val text = it.toString()
                saveButton.isEnabled = if (text.isNotEmpty()) {
                    val priceDouble = text.toDouble()
                    priceDouble > 0.0
                } else {
                    false
                }
            }
            setOnDismissListener {
                onDismiss()
            }
        }

        val dialogWindow = window
        val lp = dialogWindow?.attributes
        val d = context.resources.displayMetrics
        lp?.width = (d.widthPixels * 0.8).toInt()
        dialogWindow?.attributes = lp
    }

    private fun getSourceText(modelSource: String): Int {
        return when (modelSource) {
            "offical" -> com.shannon.openvoice.R.string.official
            "original" -> com.shannon.openvoice.R.string.original
            "purchase" -> com.shannon.openvoice.R.string.buy
            else -> com.shannon.openvoice.R.string.official
        }
    }
}