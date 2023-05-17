package com.shannon.android.lib.util

import android.content.Context
import android.content.res.Configuration
import android.graphics.Color
import android.util.TypedValue
import androidx.annotation.AttrRes
import androidx.annotation.ColorInt
import androidx.annotation.NonNull
import androidx.annotation.StyleRes
import androidx.appcompat.app.AppCompatDelegate
import androidx.appcompat.view.ContextThemeWrapper
import com.google.android.material.color.MaterialColors

/**
 *
 * @ClassName:      ThemeUtil
 * @Description:     java类作用描述
 * @Author:         czhen
 */
object ThemeUtil {
    private const val THEME_NIGHT = "night"
    private const val THEME_DAY = "day"
    private const val THEME_AUTO = "auto"
    private const val THEME_SYSTEM = "auto_system"

    const val APP_THEME_DEFAULT: String = THEME_NIGHT

    @ColorInt
    fun getColor(context: Context, @AttrRes attribute: Int): Int {
        val value = TypedValue()
        return if (context.theme.resolveAttribute(attribute, value, true)) {
            value.data
        } else {
            Color.BLACK
        }
    }

    /**
     * 获取attrs定义的ID
     * @param context Context 上下文
     * @param attr Int attrs
     * @return Int value
     */
    fun getAttrResourceId(context: Context, @AttrRes attr: Int): Int {
        val ints = intArrayOf(attr)
        val typedValue = context.obtainStyledAttributes(ints)
        val value = typedValue.getResourceId(0, 0)
        typedValue.recycle()
        return value
    }

    fun setAppNightMode(flavor: String) {
        when (flavor) {
            THEME_NIGHT -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
            THEME_DAY -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
            THEME_AUTO -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_AUTO_BATTERY)
            THEME_SYSTEM -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
        }
    }

    fun isDarkMode(context: Context): Boolean {
        val mode = context.resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK
        return mode == Configuration.UI_MODE_NIGHT_YES
    }

//    @ColorInt
//    fun getThemeAttrColor(
//        @NonNull context: Context,
//        @StyleRes themeResId: Int,
//        @AttrRes attrResId: Int
//    ): Int {
//        return MaterialColors.getColor(
//            ContextThemeWrapper(context, themeResId),
//            attrResId,
//            Color.WHITE
//        )
//    }

    fun getTypedValue(context: Context, @AttrRes attribute: Int): TypedValue {
        val value = TypedValue()
        context.theme.resolveAttribute(attribute, value, true)
        return value
    }
}