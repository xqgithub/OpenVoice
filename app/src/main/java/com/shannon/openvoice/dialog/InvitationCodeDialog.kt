package com.shannon.openvoice.dialog

import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.LinearGradient
import android.graphics.Shader
import android.os.Bundle
import android.text.Editable
import android.view.Gravity
import android.view.WindowManager
import androidx.core.content.ContextCompat
import androidx.core.widget.doAfterTextChanged
import com.shannon.android.lib.extended.inflate
import com.shannon.android.lib.extended.singleClick
import com.shannon.android.lib.util.ThemeUtil
import com.shannon.openvoice.BuildConfig
import com.shannon.openvoice.FunApplication.Companion.appViewModel
import com.shannon.openvoice.R
import com.shannon.openvoice.databinding.DialogInvitationcodeBinding

/**
 * Date:2022/11/1
 * Time:16:06
 * author:dimple
 * 邀请码弹框
 */
class InvitationCodeDialog @JvmOverloads constructor(
    var mContext: Context,
    var width: Int = WindowManager.LayoutParams.MATCH_PARENT,
    var height: Int = WindowManager.LayoutParams.WRAP_CONTENT,
    var themeResId: Int = R.style.TransparentDialog
) : Dialog(mContext, themeResId) {

    private val mBinding by inflate<DialogInvitationcodeBinding>()

    init {
        setContentView(mBinding.root)
        setCancelable(true)
        setCanceledOnTouchOutside(true)
    }

    override fun show() {
        super.show()
        /**
         * 设置宽度全屏，要设置在show的后面
         */
        val layoutParams = window!!.attributes
        layoutParams.width = width
        layoutParams.height = height
        layoutParams.gravity = Gravity.CENTER
//        window!!.setWindowAnimations(R.style.AnimBottom)
        window!!.decorView.setPadding(0, 0, 0, 0)
        window!!.attributes = layoutParams
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        sbGoToCreationisEnabled()

        mBinding.apply {
            etInvitationCode.apply {
                doAfterTextChanged {
                    sbGoToCreationisEnabled()
                }
            }
            if (BuildConfig.DEBUG) etInvitationCode.text =
                Editable.Factory().newEditable("TvhnFt4cLm")
        }

    }

    override fun onStart() {
        super.onStart()
    }


    /**
     * 检查按钮是否可以点击
     */
    private fun sbGoToCreationisEnabled() {
        mBinding.apply {
            sbGoToCreation.apply {
                isEnabled = etInvitationCode.text.toString().trim().isNotEmpty()
            }
        }
    }

    /**
     * 验证邀请码
     */
    fun toCreation(onCallback: (invitationCode: String) -> Unit) {
        mBinding.apply {
            sbGoToCreation.singleClick {
                val invitation_code = etInvitationCode.text.toString().trim()
                onCallback(invitation_code)
            }
        }
    }

    /**
     * 关闭弹框
     */
    fun dismissDialog() {
        if (isShowing) {
            dismiss()
        }
    }

}