package com.shannon.openvoice.business.timeline.menu

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import com.shannon.openvoice.R

/**
 *
 * @Package:        com.shannon.openvoice.business.timeline.menu
 * @ClassName:      StatusMenuItem
 * @Description:     作用描述
 * @Author:         czhen
 * @CreateDate:     2022/8/18 14:31
 */
data class StatusMenuItem(
    val type: Type,
    @StringRes val stringId: Int,
    @DrawableRes val drawableId: Int,
    val info: Any? = null
) {

    companion object {

        fun pin(resId: Int) = StatusMenuItem(Type.PIN, resId, R.drawable.icon_menu_pin)
        fun follow(resId: Int,follow:Boolean) = StatusMenuItem(Type.FOLLOW, resId, R.drawable.icon_menu_follow,follow)
        fun delete() =
            StatusMenuItem(Type.DELETE, R.string.delete, R.drawable.icon_menu_delete)

        fun republish() = StatusMenuItem(
            Type.REPUBLISH,
            R.string.delete_edit,
            R.drawable.icon_menu_republish
        )

        fun report() =
            StatusMenuItem(Type.REPORT, R.string.report, R.drawable.icon_menu_report)

        fun blackList() = StatusMenuItem(
            Type.BLACK_LIST,
            R.string.add_blacklist,
            R.drawable.icon_menu_black_list
        )

    }

    enum class Type {
        DELETE,//删除
        PIN,//置顶
        FOLLOW,//关注
        REPUBLISH,//删除并重新编辑
        REPORT,//举报
        BLACK_LIST;//黑名单
    }
}
