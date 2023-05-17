package com.shannon.openvoice.dialog

import android.app.Activity
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.view.WindowManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.shannon.android.lib.constant.ConfigConstants
import com.shannon.android.lib.extended.*
import com.shannon.android.lib.util.PreferencesUtil
import com.shannon.android.lib.util.PreferencesUtil.Constant.LANGUAGE
import com.shannon.android.lib.util.ScreenTools
import com.shannon.android.lib.util.ThemeUtil
import com.shannon.openvoice.R
import com.shannon.android.lib.util.ToastUtil
import com.shannon.openvoice.FunApplication
import com.shannon.openvoice.FunApplication.Companion.appViewModel
import com.shannon.openvoice.business.main.MainActivity
import com.shannon.openvoice.business.main.mine.LanguageAdapter
import com.shannon.openvoice.business.main.mine.setting.SettingActivity
import com.shannon.openvoice.components.ErrorPageView
import com.shannon.openvoice.databinding.DialogLanguageBinding
import com.shannon.openvoice.event.UniversalEvent
import com.shannon.openvoice.model.LanguageBean
import org.greenrobot.eventbus.EventBus

/**
 * Date:2022/8/3
 * Time:17:20
 * author:dimple
 * 语言选择弹框
 */
class LanguageDialog @JvmOverloads constructor(
    var mContext: Context,
    var width: Int = 240.dp,
    var height: Int = WindowManager.LayoutParams.WRAP_CONTENT,
    var themeResId: Int = R.style.TransparentDialog
) : Dialog(mContext, themeResId) {

    private val mBinding by inflate<DialogLanguageBinding>()

    private val languageAdapter by lazy {
        LanguageAdapter(mContext, this::languageAdapterClickListener)
    }

    init {
        setContentView(mBinding.root)
        setCancelable(true)
        setCanceledOnTouchOutside(true)
    }

    override fun show() {
        super.show()
        /**
         * 设置宽度全屏，要设置在show的后面
         */
        val layoutParams = window!!.attributes
        layoutParams.width = width
        layoutParams.height = height
        layoutParams.gravity = Gravity.BOTTOM
        window!!.setWindowAnimations(R.style.AnimBottom)
        window!!.decorView.setPadding(0, 0, 0, 0)
        window!!.attributes = layoutParams
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onStart() {
        super.onStart()
        initLanguageRecyclerView()
    }


    /**
     * 设置背景
     */
    fun setBG(color: Int) {
        setDynamicShapeRectangle(
            arrayOf(mBinding.clDialog), -1, "",
            CornerRadiusRightTop = 20f.dp,
            CornerRadiusLeftTop = 20f.dp,
            _orientation = null,
            _bgcolors = arrayOf(
                ThemeUtil.getTypedValue(
                    mContext,
                    color
                ).resourceId
            )
        )
    }

    private fun initLanguageRecyclerView() {
        mBinding.apply {
            recyclerView.apply {

                layoutManager = LinearLayoutManager(mContext)
                adapter = languageAdapter

                itemDivider(
                    headerNoShowSize = 0,
                    footerNoShowSize = 2,
                    size = 1.dp,
                    colorRes = R.color.color_111A19
                )

                clearAnimation()


                val languages = appViewModel.getLanguageDatas(mContext)
                if (languages.isEmpty()) {
                    bindEmptyView()
                } else {
                    mBinding.recyclerView.removeEmptyView()
                    languageAdapter.setNewDataIgnoreSize(languages)
                }


                isFootViewEnabled = false
                setNotFullScreenNoLoadMore()
                languageAdapter.notifyDataSetChanged()
            }
        }
    }

    /**
     * adapter 点击事件
     */
    private fun languageAdapterClickListener(position: Int) {
        val bean = languageAdapter.getItemData(position)
//        ToastUtil.showCenter("选择语言：${bean.LanguageEntries} =-= ${bean.LanguageValues}")
        PreferencesUtil.putString(LANGUAGE, bean.LanguageValues)
        val _flag = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
//        intentToJump(mContext as Activity, MainActivity::class.java, isFinish = true, flag = _flag)
        intentToJump(
            mContext as Activity,
            SettingActivity::class.java,
            bundle = Bundle().apply {
                putBoolean("restartActivitiesOnExit", true)
            },
            isFinish = true,
            flag = _flag
        )
        dismissDialog()
    }

    /**
     * 当列表数据为空的时候，显示
     */
    private fun bindEmptyView() {
        val errorPageView = ErrorPageView(mContext)
        with(errorPageView) {
            post {
                val recyclerViewHeight = mBinding.recyclerView.height
                setErrorIcon(
                    R.drawable.ic_development,
                    120.dp,
                    120.dp
                )
                setErrorContent(resources.getString(R.string.desc_wilderness), 12f, "#A1A7AF")
                changeErrorIconPositionToTOP(
                    (0.3 * recyclerViewHeight).toInt()
                )
                changeErrorTextPositionToTOP(10.dp)
            }
        }

        mBinding.recyclerView.apply {
            setEmptyView(errorPageView)
//            setLoadingMoreBottomHeight(0f)
            isLoadMoreEnabled = false
            languageAdapter.setNewData(arrayListOf())
        }
    }


    fun tvCancelOnClick() {
        mBinding.tvCancel.singleClick {
            dismissDialog()
        }
    }


    /**
     * 关闭弹框
     */
    fun dismissDialog() {
        if (isShowing) {
            dismiss()
        }
    }

}