package com.shannon.openvoice.util

import java.math.RoundingMode
import java.text.DecimalFormat

/**
 *
 * @Package:        com.shannon.openvoice.util
 * @ClassName:      UnitHelper
 * @Description:     作用描述
 * @Author:         czhen
 * @CreateDate:     2022/8/15 16:52
 */
object UnitHelper {
    private val decimalFormat = DecimalFormat("#.0").apply { roundingMode = RoundingMode.FLOOR }
    private const val WAN = 10000 // 万
    private const val YI = 100000000 // 万

    fun quantityConvert(count: Long): String {
        return if (count < WAN) {
            count.toString()
        } else if (count >= WAN && count < 100 * WAN) {
            val q = count / 1000.0
            decimalFormat.format(q).plus("k")
        } else if (count >= 100 * WAN && count < 10 * YI) {
            val q = count / Math.pow(10.0, 6.0)
            decimalFormat.format(q).plus("m")
        } else if (count >= 10 * YI) {
            val q = count / Math.pow(10.0, 9.0)
            decimalFormat.format(q).plus("b")
        } else {
            count.toString()
        }
    }

    fun fixPrice(price: String): String {
        return if (!price.contains(".")) {
            price.plus(".0")
        } else if (price.endsWith(".")) {
            price.plus("0")
        } else if (price.startsWith(".")) {
            "0".plus(price)
        } else {
            price
        }
    }
}