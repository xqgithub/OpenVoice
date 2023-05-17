package com.shannon.openvoice.business.main.mine.account

import android.content.Context
import android.graphics.Color
import android.os.Build
import android.view.LayoutInflater
import android.view.View
import androidx.annotation.RequiresApi
import com.lxj.xpopup.core.AttachPopupView
import com.lxj.xpopup.core.PositionPopupView
import com.shannon.android.lib.extended.dp
import com.shannon.android.lib.extended.singleClick
import com.shannon.android.lib.util.ThemeUtil
import com.shannon.openvoice.R
import com.shannon.openvoice.components.ImageAndTextUi
import com.shannon.openvoice.databinding.PopviewAccountMoreBinding

/**
 * Date:2022/8/26
 * Time:16:44
 * author:dimple
 * Ta的主页更多弹框
 */
class AccountMorePopView(val mContext: Context) :
    AttachPopupView(mContext) {

    private lateinit var mBinding: PopviewAccountMoreBinding


    @RequiresApi(Build.VERSION_CODES.Q)
    override fun getImplLayoutId(): Int {
        return R.layout.popview_account_more
//        val root = View.inflate(context, R.layout.popview_account_more, this)
//        mBinding = PopviewAccountMoreBinding.bind(root)
//        return mBinding.root.sourceLayoutResId
    }


    /**
     * 加入黑名单
     */
    fun AddBlacklist(onCallBack: () -> Unit) {
        attachPopupContainer.findViewById<ImageAndTextUi>(R.id.itAddBlacklist).apply {
            setUIFormatType(ImageAndTextUi.ImageAndTextType.left_right)
            setAvatarDataFromRes(R.drawable.icon_menu_black_list, 24.dp, 24.dp)
            setTextData(
                mContext.getString(R.string.add_blacklist),
                12f,
                _textColorInt = ThemeUtil.getColor(
                    mContext,
                    android.R.attr.textColorPrimary
                )
            )
            changeTvContentPositionToAvatar(8.dp)
            singleClick {
                onCallBack()
            }
        }
    }

    /**
     * 举报
     */
    fun report(onCallBack: () -> Unit) {
        attachPopupContainer.findViewById<ImageAndTextUi>(R.id.itReport).apply {
            setUIFormatType(ImageAndTextUi.ImageAndTextType.left_right)
            setAvatarDataFromRes(R.drawable.icon_menu_report, 24.dp, 24.dp)
            setTextData(
                mContext.getString(R.string.report),
                12f,
                _textColorInt = ThemeUtil.getColor(
                    mContext,
                    android.R.attr.textColorPrimary
                )
            )
            changeTvContentPositionToAvatar(8.dp)
            singleClick {
                onCallBack()
            }
        }
    }
}