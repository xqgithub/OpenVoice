package com.shannon.android.lib.components

import android.app.Dialog
import android.content.Context
import android.view.KeyEvent
import com.shannon.android.lib.R
import com.shannon.android.lib.databinding.DialogLoadingBinding
import com.shannon.android.lib.extended.inflate
import com.shannon.android.lib.extended.visibility

/**
 *
 * @ClassName:      LoadingDialog
 * @Description:     Loading
 * @Author:         czhen
 */
class LoadingDialog(context: Context) : Dialog(context, R.style.TransparentDialog) {

    private val binding by inflate<DialogLoadingBinding>()


    init {
        binding
        setCancelable(false)
        setCanceledOnTouchOutside(false)
        setOnKeyListener { _, keyCode, event ->
            when (keyCode) {
                KeyEvent.KEYCODE_BACK -> {
                   dismiss()
                }
            }
            return@setOnKeyListener super.onKeyDown(keyCode, event)
        }
    }

    fun setLoadingMessage(resId: Int) {
        binding.run {
            val effective = resId > 0
            messageView.visibility(effective)
            if (effective)
                messageView.setText(resId)
        }
    }
}