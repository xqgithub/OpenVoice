package com.shannon.openvoice.util

import android.text.InputFilter
import android.text.Spanned
import android.text.TextUtils

/**
 *
 * @Package:        com.shannon.openvoice.util
 * @ClassName:      DecimalPointFilter
 * @Description:     作用描述
 * @Author:         czhen
 * @CreateDate:     2022/8/16 14:03
 */
class DecimalPointFilter : InputFilter {
    override fun filter(
        source: CharSequence?,
        start: Int,
        end: Int,
        dest: Spanned?,
        dstart: Int,
        dend: Int
    ): CharSequence {
        if (source?.startsWith(".") == true && TextUtils.isEmpty(dest)) {
            return "0."
        }
        return source ?: ""
    }

}