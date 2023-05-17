package com.shannon.android.lib.extended

import android.app.Activity
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.text.Spannable
import android.text.SpannableString
import android.text.SpannableStringBuilder
import android.text.Spanned
import android.text.style.AbsoluteSizeSpan
import android.text.style.ForegroundColorSpan
import android.view.View
import android.view.inputmethod.InputMethodManager
import androidx.activity.result.ActivityResultLauncher
import androidx.annotation.AttrRes
import androidx.annotation.ColorInt
import androidx.annotation.DrawableRes
import androidx.core.content.ContextCompat
import androidx.core.text.parseAsHtml
import com.blankj.utilcode.util.ToastUtils
import com.shannon.android.lib.R
import com.shannon.android.lib.constant.ConfigConstants.VARIABLE.globalApplication
import com.shannon.android.lib.util.ThemeUtil
import com.shannon.android.lib.util.ToastUtil
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.core.ObservableTransformer
import io.reactivex.rxjava3.schedulers.Schedulers
import java.lang.ref.WeakReference
import java.text.DecimalFormat
import java.util.*
import java.util.regex.Pattern

/**
 *
 * @ClassName:      Extended
 * @Description:     java类作用描述
 * @Author:         czhen
 */

inline fun <reified T : Any> MutableList<T>.addArray(vararg obj: T) {
    obj.forEach {
        this.add(it)
    }
}

/**
 * 判断是否为 null、"null"、空字符串
 */
inline fun isBlank(str: Any?): Boolean {
    return str == null ||
            (str is String && str.toString().trim().isEmpty()) ||
            (str is String && "null" == str.toString().uppercase(Locale.getDefault()))
}

inline fun isBlankPlus(vararg strs: Any?): Boolean {
    if (isBlank(strs)) return true
    strs.forEach {
        if (isBlank(it)) {
            return true
        }
    }
    return false
}

/**
 * 判断是否为空 并传入相关操作
 */
inline fun <reified T> T?.isNotNull(notNullAction: (T) -> Unit, nullAction: () -> Unit = {}) {
    if (!isBlank(this)) {
        notNullAction.invoke(this!!)
    } else {
        nullAction.invoke()
    }
}

/**
 * 动态设置Shape  RECTANGLE        背景色或者渐变色
 * @param view                    被设置的对象view
 * @param CornerRadiusLeftTop     左上角度数
 * @param CornerRadiusRightTop    右上角度数
 * @param CornerRadiusLeftBottom  左下角度数
 * @param CornerRadiusRightBottom 右下角度数
 * @param strokeWidth             边线的宽度
 * @param strokeColor             边线的颜色
 * @param orientation             渐变方向
 * @param style                  主题
 * @param _bgcolors               渐变色
 * @param bgColor(String)         渐变色
 */
fun setDynamicShapeRectangle(
    view: Array<View>,
    strokeWidth: Int = 0,
    strokeColor: String = "",
    CornerRadiusLeftTop: Float = 0f,
    CornerRadiusRightTop: Float = 0f,
    CornerRadiusLeftBottom: Float = 0f,
    CornerRadiusRightBottom: Float = 0f,
    _orientation: GradientDrawable.Orientation? = null,
    style: Int = 0,
    _bgcolors: Array<Int> = arrayOf(),
    bgColor: Array<String> = arrayOf(),
) {
    val drawable = GradientDrawable()
    drawable.apply {
        //设置shape的形状
        shape = GradientDrawable.RECTANGLE
        //设置shape的圆角度数
        val radius = floatArrayOf(
            CornerRadiusLeftTop,
            CornerRadiusLeftTop,
            CornerRadiusRightTop,
            CornerRadiusRightTop,
            CornerRadiusRightBottom,
            CornerRadiusRightBottom,
            CornerRadiusLeftBottom,
            CornerRadiusLeftBottom
        )
        cornerRadii = radius
        //设置shape的边的宽度和颜色
        if (strokeWidth != -1 && !isBlank(strokeColor)) setStroke(
            strokeWidth,
            Color.parseColor(strokeColor)
        )
        //设置shape的背景色
        if (bgColor.isNotEmpty()) {
            if (!isBlank(_orientation)) {
                val colors = IntArray(bgColor.size)
                for (i in bgColor.indices) {
                    colors[i] = Color.parseColor(bgColor[i])
                }
                setOrientation(_orientation)
                setColors(colors)
            } else {
                setColor(Color.parseColor(bgColor[0]))
            }
        }

        if (_bgcolors.isNotEmpty()) {
            if (!isBlank(_orientation)) {
                val colors = IntArray(_bgcolors?.size!!)
                for (i in _bgcolors.indices) {
                    colors[i] = ContextCompat.getColor(globalApplication!!, _bgcolors[i])
                }
                setOrientation(_orientation)
                setColors(colors)
            } else {
                setColor(ContextCompat.getColor(globalApplication!!, _bgcolors[0]))
            }
        }

        view.forEach {
            it.background = this
        }
    }
}

/**
 * 时间差的各个部件（天，小时，分钟，秒）
 */
var timePartsCollection = { timeDifference: Long ->
    with(mutableMapOf<String, Long>()) {
        var _timeDifference = if (timeDifference.toString().length == 10) {
            timeDifference
        } else {
            timeDifference
        }
        val yushu_day: Long = _timeDifference % (1000 * 60 * 60 * 24)
        val yushu_hour: Long = (_timeDifference % (1000 * 60 * 60 * 24)
                % (1000 * 60 * 60))
        val yushu_minute: Long = (_timeDifference % (1000 * 60 * 60 * 24)
                % (1000 * 60 * 60) % (1000 * 60))
        val yushu_second: Long = (_timeDifference % (1000 * 60 * 60 * 24)
                % (1000 * 60 * 60) % (1000 * 60) % 1000)

        val day: Long = _timeDifference / (1000 * 60 * 60 * 24)
        val hour = yushu_day / (1000 * 60 * 60)
        val minute = yushu_hour / (1000 * 60)
        val second = yushu_minute / 1000

        this["day"] = day
        this["hour"] = hour
        this["minute"] = minute
        this["second"] = second
        this
    }
}


/**
 * null类型转化
 */
inline fun <reified T> dataNullConvert(params: Any?): T {
    when (T::class) {
        Int::class -> {
            return if (isBlank(params)) {
                -99 as T
            } else {
                params as T
            }
        }
        String::class -> {
            return if (isBlank(params)) {
                "" as T
            } else {
                params as T
            }
        }
        Long::class -> {
            return if (isBlank(params)) {
                -99L as T
            } else {
                params as T
            }
        }
        Float::class -> {
            return if (isBlank(params)) {
                -99f as T
            } else {
                params as T
            }
        }
        Double::class -> {
            return if (isBlank(params)) {
                -99.00 as T
            } else {
                params as T
            }
        }
        else -> {
            return throw IllegalStateException("不支持该类型")
        }
    }
}

/**
 * 页面跳转
 * @param mActivity   上下文
 * @param cls         目标的Activity
 * @param flag        跳转的方式
 * @param bundle      bundle值
 * @param isFinish    进入页面后，是否结束上一个页面
 * @param enterAnimID 进入动画ID
 * @param exitAnimID  退出动画ID
 */
fun intentToJump(
    mActivity: Activity, cls: Class<*>,
    bundle: Bundle? = null, isFinish: Boolean = false,
    enterAnimID: Int = -1, exitAnimID: Int = -1,
    flag: Int = -1
) {
    val intent = Intent(mActivity, cls)
    intent.apply {
        bundle?.let { putExtras(it) }
        if (flag != -1) flags = flag
        mActivity.startActivity(this)
        if (isFinish) mActivity.finish()
        if (enterAnimID == -1 && exitAnimID == -1) {
            return
        } else {
            mActivity.overridePendingTransition(
                if (enterAnimID == -1) 0 else enterAnimID,
                if (exitAnimID == -1) 0 else exitAnimID
            )
        }
    }
}

/**
 * 将包含来自 html 的字符串解析为 Spanned
 */
fun String.parseAsMastodonHtml(): Spanned {
    return this.replace("<br> ", "<br>&nbsp;")
        .replace("<br /> ", "<br />&nbsp;")
        .replace("<br/> ", "<br/>&nbsp;")
        .replace("  ", "&nbsp;&nbsp;")
        .parseAsHtml()
        /* Html.fromHtml returns trailing whitespace if the html ends in a </p> tag, which
         * most status contents do, so it should be trimmed. */
        .trimTrailingWhitespace()
}


/**
 * rxjava网路请求代码优化
 */
fun <T> observableToMain(): ObservableTransformer<T, T> {
    return ObservableTransformer { ob ->
        ob.subscribeOn(Schedulers.io())
            .unsubscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
    }
}

/**
 * 内容
 * 高亮显示
 */
fun contentHighlight(
    content: String,
    param: Array<String>,
    @ColorInt color: Array<Int> = arrayOf(),
    textSizes: Array<Int> = arrayOf()
): SpannableString {
    val msp = SpannableString(content)

    for (i in param.indices) {
        val startIndex = content.indexOf(param[i])
        val endIndex = startIndex + param[i].length
        val colorSpan = ForegroundColorSpan(if (i < color.size) color[i] else color[0])
        msp.setSpan(colorSpan, startIndex, endIndex, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
        if (textSizes.isNotEmpty()) {
            msp.setSpan(
                AbsoluteSizeSpan(textSizes[i], true),
                startIndex,
                endIndex,
                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
            )
        }
    }
//    param.forEach { _param ->
//        val startIndex = content.indexOf(_param)
//        val endIndex = startIndex + _param.length
//        val colorSpan = ForegroundColorSpan(color)
//        msp.setSpan(colorSpan, startIndex, endIndex, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
//
//        if (textSizes.isNotEmpty()) {
//            msp.setSpan(
//                AbsoluteSizeSpan(26, true),
//                startIndex,
//                endIndex,
//                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
//            )
//        }
//    }
    return msp
}

/**
 * 数字超过3位数后每3位数需要使用逗号隔开
 */
val addComma = { str: String ->
    val myformat = DecimalFormat().apply {
        applyPattern("#,###")
    }
    myformat.format(str.toDouble())
}

/**
 * 隐藏软键盘
 */
fun hideSoftKeyboard(activity: Activity?) {
    activity?.let { act ->
        val view = act.currentFocus
        view?.let {
            val inputMethodManager =
                act.getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
            inputMethodManager.hideSoftInputFromWindow(
                view.windowToken,
                InputMethodManager.HIDE_NOT_ALWAYS
            )
        }
    }
}
















