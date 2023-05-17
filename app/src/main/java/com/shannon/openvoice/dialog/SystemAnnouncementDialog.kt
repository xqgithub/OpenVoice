package com.shannon.openvoice.dialog

import android.app.Activity
import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.view.Gravity
import android.view.WindowManager
import androidx.fragment.app.FragmentManager
import com.shannon.android.lib.extended.dp
import com.shannon.android.lib.extended.inflate
import com.shannon.android.lib.extended.setDynamicShapeRectangle
import com.shannon.android.lib.extended.singleClick
import com.shannon.android.lib.util.ThemeUtil
import com.shannon.openvoice.R
import com.shannon.openvoice.components.prioritywindow.IWindow
import com.shannon.openvoice.components.prioritywindow.OnWindowDismissListener
import com.shannon.openvoice.databinding.DialogSysAnnouncementBinding

/**
 * Date:2022/11/8
 * Time:16:39
 * author:dimple
 * 系统公告弹框
 */
class SystemAnnouncementDialog @JvmOverloads constructor(
    var mContext: Context,
    var width: Int = WindowManager.LayoutParams.MATCH_PARENT,
    var height: Int = WindowManager.LayoutParams.WRAP_CONTENT,
    var themeResId: Int = R.style.TransparentDialog
) : Dialog(mContext, themeResId), IWindow {

    private val mBinding by inflate<DialogSysAnnouncementBinding>()
    private var onWindowDismissListener: OnWindowDismissListener? = null

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
//        window!!.setWindowAnimations(R.style.AnimBottom)
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
            CornerRadiusRightBottom = 8f.dp,
            CornerRadiusLeftBottom = 8f.dp,
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
     * 设置公告的内容
     */
    fun setAnnouncementContent(content: String) {
        mBinding.atvInvitationCode.text = content
    }

    /**
     * 点击确认按钮
     */
    fun clicksbGoToCreation(onCallback: () -> Unit) {
        mBinding.sbGoToCreation.singleClick {
            onCallback()
            dismissDialog()
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

    override fun getClassName(): String {
        return SystemAnnouncementDialog::class.java.simpleName
    }

    override fun show(activity: Activity?, manager: FragmentManager?) {
        if (activity == null || activity.isFinishing || activity.isDestroyed) {
            return
        }
        show()
    }

    /**
     * 设置窗口关闭监听
     */
    override fun setOnWindowDismissListener(listener: OnWindowDismissListener?) {
        onWindowDismissListener = listener
        setOnDismissListener { onWindowDismissListener?.onDismiss() }
    }

}