package com.shannon.openvoice.dialog

import android.app.Dialog
import android.content.Context
import android.view.KeyEvent
import com.bumptech.glide.Glide
import com.shannon.android.lib.R
import com.shannon.android.lib.databinding.DialogLoadingBinding
import com.shannon.android.lib.extended.inflate
import com.shannon.android.lib.extended.visibility
import com.shannon.openvoice.databinding.DialogComposeLoadingBinding

/**
 *
 * @ClassName:      ComposeLoadingDialog
 * @Description:     Loading
 * @Author:         czhen
 */
class ComposeLoadingDialog(context: Context) : Dialog(context, R.style.TransparentDialog) {

    private val binding by inflate<DialogComposeLoadingBinding>()


    init {
        binding
        setCancelable(false)
        setCanceledOnTouchOutside(false)
        setOnKeyListener { _, keyCode, event ->
            when (keyCode) {
                KeyEvent.KEYCODE_BACK -> {
                    return@setOnKeyListener true
                }
            }
            return@setOnKeyListener super.onKeyDown(keyCode, event)
        }
        Glide.with(context).asGif().load(com.shannon.openvoice.R.drawable.compose_load).into(binding.imageView)
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