package com.shannon.android.lib.extended

import android.text.Spanned
import androidx.core.text.parseAsHtml
import java.util.*

/**
 *
 * @ClassName:      String
 * @Description:     java类作用描述
 * @Author:         czhen
 */

fun String?.nonNull(defaultValue: String = ""): String {
    if (isNullOrEmpty()) return defaultValue
    return this
}

fun String.isLessThanOrEqual(other: String): Boolean {
    return this == other || isLessThan(other)
}

fun String.isLessThan(other: String): Boolean {
    return when {
        this.length < other.length -> true
        this.length > other.length -> false
        else -> this < other
    }
}

fun Spanned.trimTrailingWhitespace(): Spanned {
    var i = length
    do {
        i--
    } while (i >= 0 && get(i).isWhitespace())
    return subSequence(0, i + 1) as Spanned
}

fun String.parseAsAppHtml(): Spanned {
    return this.replace("<br> ", "<br>&nbsp;")
        .replace("<br /> ", "<br />&nbsp;")
        .replace("<br/> ", "<br/>&nbsp;")
        .replace("  ", "&nbsp;&nbsp;")
        .parseAsHtml()
        .trimTrailingWhitespace()
}

private const val POSSIBLE_CHARS = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZ"
fun randomAlphanumericString(count: Int): String {
    val chars = CharArray(count)
    val random = Random()
    for (i in 0 until count) {
        chars[i] = POSSIBLE_CHARS[random.nextInt(POSSIBLE_CHARS.length)]
    }
    return String(chars)
}