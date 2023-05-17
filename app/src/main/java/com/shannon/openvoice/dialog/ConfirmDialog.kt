package com.shannon.openvoice.dialog

import android.app.Activity
import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.os.Bundle
import android.text.SpannableString
import android.text.method.LinkMovementMethod
import android.view.Gravity
import android.view.View
import android.view.WindowManager
import android.widget.LinearLayout
import androidx.annotation.ColorInt
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import com.just.agentweb.AgentWeb
import com.shannon.android.lib.extended.*
import com.shannon.android.lib.util.ThemeUtil
import com.shannon.openvoice.R
import com.shannon.openvoice.databinding.DialogConfirmBinding
import com.shannon.openvoice.databinding.DialogLanguageBinding

/**
 * Date:2022/11/2
 * Time:15:16
 * author:dimple
 * 确认弹框
 */
class ConfirmDialog @JvmOverloads constructor(
    var mContext: Context,
    var width: Int = WindowManager.LayoutParams.MATCH_PARENT,
    var height: Int = WindowManager.LayoutParams.WRAP_CONTENT,
    var themeResId: Int = R.style.TransparentDialog,
    isCancelable: Boolean = false
) : Dialog(mContext, themeResId) {

    private val mBinding by inflate<DialogConfirmBinding>()

    init {
        setContentView(mBinding.root)
        setCancelable(false)
        setCanceledOnTouchOutside(false)
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
        window!!.decorView.setPadding(0, 0, 0, 0)
        window!!.attributes = layoutParams
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onStart() {
        super.onStart()
    }

    /**
     * 设置背景
     */
    fun setBG(color: Int) {
        setDynamicShapeRectangle(
            arrayOf(mBinding.clDialog), -1, "",
            CornerRadiusRightTop = 8f.dp,
            CornerRadiusLeftTop = 8f.dp,
            CornerRadiusLeftBottom = 8f.dp,
            CornerRadiusRightBottom = 8f.dp,
            _orientation = null,
            _bgcolors = arrayOf(
                ThemeUtil.getTypedValue(
                    mContext,
                    color
                ).resourceId
            )
        )
    }

    /**
     * 设置标题文字
     */
    fun setTitleText(
        _visibility: Int = View.VISIBLE,
        content: String = "",
        _textSize: Float = 14f,
        _textColorInt: Int? = null,
        _textColorString: String = "#57493B",
        _spannableString: SpannableString? = null
    ) {
        mBinding.tvTitle.apply {
            visibility = _visibility
            text = if (isBlankPlus(_spannableString)) content else _spannableString
            textSize = _textSize
            _textColorInt.isNotNull({
//                setTextColor(ContextCompat.getColor(mContext, it))
                setTextColor(it)
            }, {
                setTextColor(Color.parseColor(_textColorString))
            })

            if (!isBlankPlus(_spannableString)) {
                movementMethod = LinkMovementMethod.getInstance()
                highlightColor = ContextCompat.getColor(mContext, R.color.transparent)
            }
        }
    }

    /**
     * 设置内容文字
     */
    fun setContentText(
        _visibility: Int = View.VISIBLE,
        content: String = "",
        _textSize: Float = 14f,
        _textColorInt: Int? = null,
        _textColorString: String = "#57493B",
        _spannableString: SpannableString? = null,
        _gravity: Int = Gravity.CENTER
    ) {
        mBinding.tvContent.apply {
            visibility = _visibility
            gravity = _gravity
            text = if (isBlankPlus(_spannableString)) content else _spannableString
            textSize = _textSize
            _textColorInt.isNotNull({
//                setTextColor(ContextCompat.getColor(mContext, it))
                setTextColor(it)
            }, {
                setTextColor(Color.parseColor(_textColorString))
            })

            if (!isBlankPlus(_spannableString)) {
                movementMethod = LinkMovementMethod.getInstance()
                highlightColor = ContextCompat.getColor(mContext, R.color.transparent)
            }
        }
    }

    /**
     * 设置确认按钮1
     */
    fun setConfirmText(
        _visibility: Int = View.VISIBLE,
        content: String = "",
        _textSize: Float = 14f,
        _textColorInt: Int? = null,
        _textColorString: String = "#57493B",
        _spannableString: SpannableString? = null,
        onCallback: () -> Unit
    ) {
        mBinding.tvConfirm.apply {
            visibility = _visibility
            text = if (isBlankPlus(_spannableString)) content else _spannableString
            textSize = _textSize
            _textColorInt.isNotNull({
//                setTextColor(ContextCompat.getColor(mContext, it))
                setTextColor(it)
            }, {
                setTextColor(Color.parseColor(_textColorString))
            })

            if (!isBlankPlus(_spannableString)) {
                movementMethod = LinkMovementMethod.getInstance()
                highlightColor = ContextCompat.getColor(mContext, R.color.transparent)
            }

            singleClick {
                onCallback()
            }
        }
    }

    /**
     * 设置确认按钮2
     */
    fun setConfirm2Text(
        _visibility: Int = View.VISIBLE,
        content: String = "",
        _textSize: Float = 14f,
        _textColorInt: Int? = null,
        _textColorString: String = "#57493B",
        _spannableString: SpannableString? = null,
        onCallback: () -> Unit
    ) {
        mBinding.tvConfirm2.apply {
            visibility = _visibility
            text = if (isBlankPlus(_spannableString)) content else _spannableString
            textSize = _textSize
            _textColorInt.isNotNull({
//                setTextColor(ContextCompat.getColor(mContext, it))
                setTextColor(it)
            }, {
                setTextColor(Color.parseColor(_textColorString))
            })

            if (!isBlankPlus(_spannableString)) {
                movementMethod = LinkMovementMethod.getInstance()
                highlightColor = ContextCompat.getColor(mContext, R.color.transparent)
            }

            singleClick {
                onCallback()
            }
        }
    }

    /**
     * 设置webview content
     */
    private var mAgentWeb: AgentWeb? = null
    private var preWeb: AgentWeb.PreAgentWeb? = null

    fun setWebViewContent(
        _visibility: Int = View.VISIBLE,
        url: String = ""
    ) {
        mBinding.webcontent.visibility = _visibility
        preWeb = AgentWeb.with(mContext as Activity)
            .setAgentWebParent(mBinding.webcontent, LinearLayout.LayoutParams(-1, -1))
            .useDefaultIndicator()
            .createAgentWeb()
            .ready()

        //加载网页
        mAgentWeb = preWeb?.go(url)
    }


    /**
     * 关闭弹框
     */
    fun dismissDialog() {
        if (isShowing) {
            dismiss()
            mAgentWeb?.webLifeCycle?.onDestroy()
        }
    }

    override fun onStop() {
        super.onStop()
    }


    /**
     * 修改文字间的距离
     */
    fun changeTvContentPositiontvTitle(offset: Int = 0) {
        val layout = ConstraintLayout.LayoutParams(
            ConstraintLayout.LayoutParams.WRAP_CONTENT,
            ConstraintLayout.LayoutParams.WRAP_CONTENT
        )
        layout.startToStart = R.id.clDialog
        layout.endToEnd = R.id.clDialog
        layout.topToBottom = R.id.tvTitle
        layout.topMargin = offset
        mBinding.tvContent.layoutParams = layout
    }

    /**
     * 设置物理返回按钮是否可以弹框消失，点击弹框外是否可以弹框消失
     */
    fun setCanceledOnTouchOutsideAndCanceled(
        isCancelable: Boolean,
        isCanceledOnTouchOutside: Boolean
    ) {
        setCancelable(isCancelable)
        setCanceledOnTouchOutside(isCanceledOnTouchOutside)
    }
}