package com.shannon.android.lib.components

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.text.method.LinkMovementMethod
import android.view.KeyEvent
import androidx.annotation.DrawableRes
import androidx.core.content.ContextCompat
import com.shannon.android.lib.R
import com.shannon.android.lib.databinding.DialogLayoutConfirmBinding
import com.shannon.android.lib.extended.gone
import com.shannon.android.lib.extended.inflate
import com.shannon.android.lib.extended.singleClick
import com.shannon.android.lib.extended.visibility
import com.shannon.android.lib.util.ThemeUtil

/**
 *
 * @Package:        com.shannon.android.lib.components
 * @ClassName:      ConfirmDialog
 * @Description:     作用描述
 * @Author:         czhen
 * @CreateDate:     2022/8/9 10:28
 */
class ConfirmDialog : Dialog {

    private val binding by inflate<DialogLayoutConfirmBinding>()

    private var title: String = ""
    private var message: CharSequence = ""
    private var okText: String = ""
    private var cancelText: String = ""
    private var drawableRes: Int? = null

    private var doCancel: () -> Unit = {}
    private var doConfirm: () -> Unit = {}

    private var withOutCancel = false

    constructor(
        context: Context,
        title: String = "",
        message: CharSequence = "",
        okText: String = "",
        cancelText: String = "",
        @DrawableRes drawableRes: Int?,
        doCancel: () -> Unit = {},
        doConfirm: () -> Unit = {}
    ) : super(context, ThemeUtil.getAttrResourceId(context, R.attr.confirmDialogStyle)) {
        this.title = title
        this.message = message
        this.okText = okText
        this.cancelText = cancelText
        this.drawableRes = drawableRes
        this.doCancel = doCancel
        this.doConfirm = doConfirm
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding.run {
            if (title.isNotEmpty()) titleView.text = title else titleView.gone()
            if (message.isNotEmpty()) messageView.text = message else messageView.gone()
            cancelView.visibility(!withOutCancel)
            dividingView.visibility(!withOutCancel)
            if (okText.isNotEmpty()) confirmView.text = okText
            if (cancelText.isNotEmpty()) cancelView.text = cancelText

            messageView.movementMethod = LinkMovementMethod.getInstance()

            if (drawableRes != null)
                messageView.setCompoundDrawablesRelativeWithIntrinsicBounds(
                    null,
                    ContextCompat.getDrawable(context, drawableRes!!),
                    null,
                    null
                )

            confirmView.singleClick {
                doConfirm()
                dismiss()
            }
            cancelView.singleClick {
                doCancel()
                dismiss()
            }
        }

        val dialogWindow = window
        val lp = dialogWindow?.attributes
        val d = context.resources.displayMetrics
        lp?.width = (d.widthPixels * 0.66).toInt()
        dialogWindow?.attributes = lp
    }


    fun showWithOutCancel() {
        withOutCancel = true
        show()
    }

    fun dismissOnBackPressed(dismissOnBackPressed: Boolean) {
        setOnKeyListener { _, keyCode, _ ->
            keyCode == KeyEvent.KEYCODE_BACK && !dismissOnBackPressed
        }
    }
}