package com.shannon.openvoice.util

import android.content.Context
import com.shannon.openvoice.R
import java.text.SimpleDateFormat
import java.util.*

/**
 *
 * @ProjectName:    OpenVoice
 * @Package:        com.shannon.openvoice.util
 * @ClassName:      TimestampUtils
 * @Description:     java类作用描述
 * @Author:         czhen
 * @CreateDate:     2022/8/2 10:47
 */
object TimestampUtils {

    private const val SECOND_IN_MILLIS: Long = 1000
    private const val MINUTE_IN_MILLIS = SECOND_IN_MILLIS * 60
    private const val HOUR_IN_MILLIS = MINUTE_IN_MILLIS * 60
    private const val DAY_IN_MILLIS = HOUR_IN_MILLIS * 24
    private const val YEAR_IN_MILLIS = DAY_IN_MILLIS * 365

    private val dateFormat = SimpleDateFormat("HH:mm/dd/MM yy", Locale.US)

    //聊天会话的时间格式
    private val formatterYearMax = createFormatter("dd/MM/yyyy", "dd/MM/yyyy")
    private val formatterYear = createFormatter("HH:mm dd.MM.yy", "HH:mm dd.MM.yy")
    private val formatterWeek = createFormatter("HH:mm EEE", "HH:mm EEE")
    private val formatterDay = createFormatter("HH:mm", "HH:mm")

    fun getRelativeTimeSpanString(context: Context, then: Long, now: Long): String {
        var span = now - then
        if (span < 0) {
            span = -span
        }
        val format: Int
        if (span < MINUTE_IN_MILLIS) {
            span /= SECOND_IN_MILLIS
            format = R.string.seconds_ago
        } else if (span < HOUR_IN_MILLIS) {
            span /= MINUTE_IN_MILLIS
            format = R.string.minutes_ago
        } else if (span < DAY_IN_MILLIS) {
            span /= HOUR_IN_MILLIS
            format = R.string.hours_ago
        } else if (span < YEAR_IN_MILLIS) {
            span /= DAY_IN_MILLIS
            format = R.string.days_ago
        } else {
            span /= YEAR_IN_MILLIS
            format = R.string.years_ago
        }
        return context.getString(format, span)
    }

    fun formatDate(date: Date): String {
        return dateFormat.format(date)
    }

    fun stringForMessageListDate(date: Long): String {
        try {
            val rightNow = Calendar.getInstance()
            val day = rightNow[Calendar.DAY_OF_YEAR]
            val year = rightNow[Calendar.YEAR]
            rightNow.timeInMillis = date
            val dateDay = rightNow[Calendar.DAY_OF_YEAR]
            val dateYear = rightNow[Calendar.YEAR]
            return if (year != dateYear) {
                formatterYearMax.format(Date(date))
            } else {
                val dayDiff = dateDay - day
                if (dayDiff == 0 || dayDiff == -1 && (System.currentTimeMillis() / 1000).toInt() - (date / 1000) < 60 * 60 * 8) {
                    formatterDay.format(Date(date))
                } else {
                    formatterYearMax.format(Date(date))
                }
            }
        } catch (e: java.lang.Exception) {
        }
        return "LOC_ERR"
    }

    fun formatMessageDate(context: Context, date: Long): String {
        try {
            val rightNow = Calendar.getInstance()
            val day = rightNow[Calendar.DAY_OF_YEAR] //当前的天数
            val year = rightNow[Calendar.YEAR] //当前的年
            rightNow.timeInMillis = date
            val dateDay = rightNow[Calendar.DAY_OF_YEAR] //消息的天数
            val dateYear = rightNow[Calendar.YEAR] //消息的年

            return if (dateDay == day && year == dateYear) { //当天的时间格式化
                formatterDay.format(Date(date))
            } else if (dateDay + 1 == day && year == dateYear) { //昨天
                context.getString(R.string.Yesterday) + " " + formatterDay.format(Date(date))
            } else if (day - dateDay in 2..6 && year == dateYear) { //一周之内
                formatterWeek.format(Date(date))
            } else {
                formatterYear.format(Date(date))
            }
        } catch (e: java.lang.Exception) {
        }
        return "LOC_ERR: formatDate"
    }

    private fun createFormatter(
        format: String,
        defaultFormat: String
    ): SimpleDateFormat {
        var f = format.ifEmpty {
            defaultFormat
        }
        var formatter: SimpleDateFormat
        try {
            formatter = SimpleDateFormat(f, Locale.US)
        } catch (e: Exception) {
            f = defaultFormat
            formatter = SimpleDateFormat(f, Locale.US)
        }
        return formatter
    }
}