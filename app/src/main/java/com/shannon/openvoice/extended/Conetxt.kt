package com.shannon.openvoice.extended

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Bundle
import androidx.annotation.ColorRes
import androidx.annotation.DrawableRes
import androidx.core.content.ContextCompat
import com.shannon.android.lib.constant.ConfigConstants
import com.shannon.android.lib.extended.intentToJump
import com.shannon.android.lib.extended.isBlankPlus
import com.shannon.openvoice.R
import com.shannon.openvoice.business.main.enum.ActivityType
import com.shannon.openvoice.business.main.login.LoginActivity
import com.shannon.openvoice.business.main.login.LoginMethodActivity
import com.shannon.openvoice.business.main.mine.account.AccountManager
import kotlinx.coroutines.flow.MutableStateFlow

/**
 *
 * @ :        com.shannon.openvoice.extended
 * @ClassName:      Conetxt
 * @Description:     作用描述
 * @Author:         czhen
 * @CreateDate:     2022/8/11 14:10
 */

fun Context.getColorKt(@ColorRes color: Int): Int {
    return ContextCompat.getColor(this, color)
}

fun Context.getDrawableKt(@DrawableRes drawable: Int): Drawable? {
    return ContextCompat.getDrawable(this, drawable)
}

fun Context.clip(content: String): Boolean {
    return try {
        val clipManager = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        clipManager.setPrimaryClip(ClipData.newPlainText(getString(R.string.app_name), content))
        true
    } catch (e: Exception) {
        false
    }
}

public inline fun <T> MutableStateFlow<T>.updateAndGet(function: (T) -> T): T {
    while (true) {
        val prevValue = value
        val nextValue = function(prevValue)
        if (compareAndSet(prevValue, nextValue)) {
            return nextValue
        }
    }
}

public inline fun <T> MutableStateFlow<T>.update(function: (T) -> T) {
    while (true) {
        val prevValue = value
        val nextValue = function(prevValue)
        if (compareAndSet(prevValue, nextValue)) {
            return
        }
    }
}

fun Context.launchAppStoreDetail(appPkg: String): Boolean {
    try {
        if (isBlankPlus(appPkg)) return false
        val uri = Uri.parse("https://play.google.com/store/apps/details?id=$appPkg")
        val intent = Intent(Intent.ACTION_VIEW, uri)
        intent.setPackage("com.android.vending")
        startActivity(intent)
    } catch (e: Exception) {
        e.printStackTrace()
        return false
    }
    return true
}

fun Context.isIntentResolvable(intent: Intent): Boolean {
    return intent.resolveActivity(packageManager) != null
}

/**
 * 拦截登录
 * @receiver Context
 * @param logic Function0<Unit>
 */
fun Context.startActivityIntercept(condition: Boolean = false, logic: () -> Unit = {}): Boolean {
    return if (condition || AccountManager.accountManager.isLogin()) {
        logic()
        true
    } else {
        val intent = Intent(this, LoginMethodActivity::class.java)
//        intent.putExtras(Bundle().apply {
//            putSerializable(
//                ConfigConstants.CONSTANT.activityType,
//                ActivityType.login
//            )
//        })
        startActivity(intent)
        false
    }
}