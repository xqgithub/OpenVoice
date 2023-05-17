package com.shannon.openvoice.dialog

import android.app.Activity
import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.view.Gravity
import android.view.WindowManager
import androidx.annotation.ColorInt
import androidx.annotation.ColorRes
import androidx.core.content.ContentProviderCompat.requireContext
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import com.gyf.immersionbar.ImmersionBar
import com.shannon.android.lib.extended.*
import com.shannon.android.lib.util.PreferencesUtil
import com.shannon.android.lib.util.PreferencesUtil.Constant.NOTIFICATION_FILTER
import com.shannon.android.lib.util.ThemeUtil
import com.shannon.openvoice.R
import com.shannon.openvoice.business.main.notification.NotificationTypeAdapter
import com.shannon.openvoice.components.ErrorPageView
import com.shannon.openvoice.databinding.DialogNotificationTypeBinding
import com.shannon.openvoice.model.Notification
import com.shannon.openvoice.model.NotificationTypeSelectedBean

/**
 * Date:2022/8/3
 * Time:17:20
 * author:dimple
 * 通知类型弹框
 */
class NotificationTypeDialog @JvmOverloads constructor(
    var mContext: Context,
    var width: Int = WindowManager.LayoutParams.MATCH_PARENT,
    var height: Int = WindowManager.LayoutParams.WRAP_CONTENT,
    var themeResId: Int = R.style.TransparentDialog2
) : Dialog(mContext, themeResId) {

    private val mBinding by inflate<DialogNotificationTypeBinding>()

    private val notificationTypeAdapter by lazy {
        NotificationTypeAdapter(mContext)
    }

    init {
        setContentView(mBinding.root)
        setCancelable(false)
        setCanceledOnTouchOutside(false)
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
        setStatusBarBg(R.color.color_030E0D)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onStart() {
        super.onStart()
        initRecyclerView()

        //关闭弹框
        mBinding.ibBack.singleClick {
            dismissDialog()
            setStatusBarBg(R.color.transparent)
        }
    }


    /**
     * 设置背景
     */
    fun setBG(color: Int) {
        setDynamicShapeRectangle(
            arrayOf(mBinding.clDialog), -1, "",
            _orientation = null,
            _bgcolors = arrayOf(
                ThemeUtil.getTypedValue(
                    mContext,
                    color
                ).resourceId
            )
        )
    }


    private fun initRecyclerView() {
        mBinding.apply {
            recyclerView.apply {
                layoutManager = LinearLayoutManager(mContext)
                adapter = notificationTypeAdapter
                clearAnimation()
                setLoadingMoreBottomHeight(0f)

                setOnItemChildClickListener { view, position ->
                    when (view.id) {
                        R.id.cl_main -> {
                            val bean = notificationTypeAdapter.getItemData(position)
                            bean.isTypeSelected = !bean.isTypeSelected
                            notificationTypeAdapter.notifyDataSetChanged()
                        }
                    }
                }

                val types = getNotificationTypeData()
                if (types.isEmpty()) {
                    bindEmptyView()
                } else {
                    mBinding.recyclerView.removeEmptyView()
                    notificationTypeAdapter.setNewDataIgnoreSize(types)
                }
            }
        }
    }

    /**
     * 获取通知类型数据
     */
    fun getNotificationTypeData(): MutableList<NotificationTypeSelectedBean> {
        var NotificationTypeSelectedBeans: MutableList<NotificationTypeSelectedBean> =
            mutableListOf()

        val NotificationTypeSelectedBeanJson = PreferencesUtil.getString(NOTIFICATION_FILTER, "")

        if (!isBlankPlus(NotificationTypeSelectedBeanJson)) {
            NotificationTypeSelectedBeans = Gson().fromJson(
                NotificationTypeSelectedBeanJson,
                object : TypeToken<MutableList<NotificationTypeSelectedBean?>?>() {}.type
            )
        } else {
            val NotificationTypeList = Notification.Type.asList
            NotificationTypeList.forEach { type ->
                NotificationTypeSelectedBeans.add(
                    NotificationTypeSelectedBean(
                        type,
                        getNotificationText(type),
                        true
                    )
                )
            }
        }

        return NotificationTypeSelectedBeans
    }

    /**
     * 根据通知类型获取displayName
     */
    private fun getNotificationText(type: Notification.Type): String {
        return when (type) {
            Notification.Type.MENTION -> mContext.getString(R.string.mention)
            Notification.Type.REBLOG -> mContext.getString(R.string.extrapolation)
            Notification.Type.FAVOURITE -> mContext.getString(R.string.collections)
            Notification.Type.FOLLOW -> mContext.getString(R.string.follow)
            Notification.Type.VOICEMODELCREATED -> mContext.getString(R.string.model_submit)
            Notification.Type.VOICEMODELGENERATED -> mContext.getString(R.string.model_generation_succ)
            Notification.Type.VOICEMODELGENERATEFAILED -> mContext.getString(R.string.model_generation_failed)
            Notification.Type.VOICEMODELBOUGHTED -> mContext.getString(R.string.model_purchase_succ)
            Notification.Type.VOICEMODELSOLDED -> mContext.getString(R.string.model_sold_succ)
            else -> {
                "Unknown"
            }
        }
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
            notificationTypeAdapter.setNewData(arrayListOf())
        }
    }

    fun tvSaveOnClick(onCallback: (beans: MutableList<NotificationTypeSelectedBean>) -> Unit) {
        mBinding.tvSave.setOnClickListener {
            //保存所选的数据
            PreferencesUtil.putString(
                NOTIFICATION_FILTER,
                Gson().toJson(notificationTypeAdapter.data)
            )

            onCallback(notificationTypeAdapter.data)
        }
    }


    /**
     * 设置状态栏背景色
     */
    private fun setStatusBarBg(@ColorRes bgColor: Int) {
        val isDarkFont = !ThemeUtil.isDarkMode(mContext)
        ImmersionBar.with(mContext as Activity)
            .statusBarDarkFont(isDarkFont)
            .statusBarColor(bgColor)
            .navigationBarDarkIcon(isDarkFont)
            .init()
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