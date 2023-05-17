package com.shannon.android.lib.extended

import android.content.Context
import androidx.annotation.DrawableRes
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import com.shannon.android.lib.components.ConfirmDialog

/**
 *
 * @ClassName:      ConfirmDialog
 * @Description:     java类作用描述
 * @Author:         czhen
 */
fun Context.showConfirmDialog(
    title: String = "",
    message: CharSequence,
    okText: String = "",
    cancelText: String = "",
    isCanceledOnTouchOutside: Boolean = true,
    doCancel: () -> Unit = {},
    doConfirm: () -> Unit = {}
) {
    val lifecycle = toLifecycle()
    if (lifecycle?.currentState != Lifecycle.State.DESTROYED) {
        val dialog = ConfirmDialog(
            this,
            title,
            message,
            okText,
            cancelText,
            null,
            doCancel,
            doConfirm
        )
        dialog.setCanceledOnTouchOutside(isCanceledOnTouchOutside)
        dialog.setCancelable(isCanceledOnTouchOutside)
        dialog.show()
    }
}

fun Context.showDialogOnlyConfirm(
    title: String = "",
    message: CharSequence,
    okText: String = "",
    isCanceledOnTouchOutside: Boolean = false,
    doConfirm: () -> Unit = {}
) {
    val lifecycle = toLifecycle()
    if (lifecycle?.currentState != Lifecycle.State.DESTROYED) {
        val dialog =
            ConfirmDialog(this, title, message, okText, drawableRes = null, doConfirm = doConfirm)
        dialog.setCanceledOnTouchOutside(isCanceledOnTouchOutside)
        dialog.setCancelable(isCanceledOnTouchOutside)
        dialog.showWithOutCancel()
    }
}

fun Context.showDialogOnlyConfirmWithIcon(
    message: CharSequence,
    @DrawableRes drawableRes: Int,
    okText: String = "",
    isCanceledOnTouchOutside: Boolean = false,
    doConfirm: () -> Unit = {}
) {
    val lifecycle = toLifecycle()
    if (lifecycle?.currentState != Lifecycle.State.DESTROYED) {
        val dialog =
            ConfirmDialog(
                this,
                message = message,
                okText = okText,
                drawableRes = drawableRes,
                doConfirm = doConfirm
            )
        dialog.setCanceledOnTouchOutside(isCanceledOnTouchOutside)
        dialog.setCancelable(isCanceledOnTouchOutside)
        dialog.showWithOutCancel()
    }
}


/**
 * 显示带有图标的选择确认框
 */
fun Context.showDialogConfirmWithIcon(
    message: CharSequence,
    @DrawableRes drawableRes: Int,
    okText: String = "",
    cancelText: String = "",
    isCanceledOnTouchOutside: Boolean = false,
    doConfirm: () -> Unit = {},
    doCancel: () -> Unit = {},
) {
    val lifecycle = toLifecycle()
    if (lifecycle?.currentState != Lifecycle.State.DESTROYED) {
        val dialog =
            ConfirmDialog(
                this,
                message = message,
                okText = okText,
                cancelText = cancelText,
                drawableRes = drawableRes,
                doConfirm = doConfirm,
                doCancel = doCancel
            )
        dialog.setCanceledOnTouchOutside(isCanceledOnTouchOutside)
        dialog.setCancelable(isCanceledOnTouchOutside)
        dialog.show()
    }
}


private fun Context.toLifecycle(): Lifecycle? {
    return when (this) {
        is AppCompatActivity -> {
            lifecycle
        }
        is Fragment -> {
            viewLifecycleOwner.lifecycle
        }
        else -> {
            null
        }
    }
}

