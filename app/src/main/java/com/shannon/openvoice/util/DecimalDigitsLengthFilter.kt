package com.shannon.openvoice.util

import android.text.InputFilter
import android.text.Spanned
import timber.log.Timber

/**
 *
 * @Package:        com.shannon.openvoice.util
 * @ClassName:      DecimalDigitsInputFilter
 * @Description:     小数点后位数
 * @Author:         czhen
 * @CreateDate:     2022/8/16 14:05
 */
class DecimalDigitsLengthFilter(
    private val decimalLength: Int,
    private var intBitsLength: Int = -1
) : InputFilter {
    override fun filter(
        source: CharSequence,
        start: Int,
        end: Int,
        dest: Spanned?,
        dstart: Int,
        dend: Int
    ): CharSequence? {

        if (intBitsLength == -1)
            intBitsLength = decimalLength

        var dotPos = -1
        dest?.forEachIndexed { index, c ->
            if (c == '.') dotPos = index
        }
        val length = dest?.length ?: 0
//        Timber.e("source = $source ; dest = $dest ; start = $start ; dstart = $dstart ; end = $end ; dend = $dend ; dotPos = $dotPos ; length = $length")
        //source =  ; dest = 123456789.123456789 ; start = 0 ; dstart = 9 ; end = 0 ; dend = 10
        //如果有小数点且已输入的长度大于11，则不允许删除小数点
        if (dotPos != -1 && length >= (intBitsLength + 2) && dstart == dotPos && dstart - dend < 0) return "."

        if (length >= (decimalLength + intBitsLength + 1)) return ""

        if (dotPos >= 0) {
            if (source == ".") return ""

            if (dend <= dotPos) return null

            val len = dest?.length ?: 0
            if (len - dotPos > decimalLength) return ""
        } else {
            val len = dest?.length ?: 0
            if (len >= intBitsLength && source != ".") return ""
        }
        return null
    }
}