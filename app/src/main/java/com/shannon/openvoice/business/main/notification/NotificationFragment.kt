package com.shannon.openvoice.business.main.notification

import android.view.WindowManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.blankj.utilcode.util.LogUtils
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.shannon.android.lib.base.activity.KBaseFragment
import com.shannon.android.lib.extended.*
import com.shannon.android.lib.util.PreferencesUtil
import com.shannon.android.lib.util.ThemeUtil
import com.shannon.openvoice.FunApplication
import com.shannon.openvoice.R
import com.shannon.openvoice.business.main.mine.account.AccountActivity
import com.shannon.openvoice.business.status.StatusListActivity
import com.shannon.openvoice.business.timeline.detail.StatusDetailActivity
import com.shannon.openvoice.business.timeline.listener.LinkListener
import com.shannon.openvoice.business.timeline.listener.RefreshableFragment
import com.shannon.openvoice.components.ErrorPageView
import com.shannon.openvoice.components.ImageAndTextUi
import com.shannon.openvoice.databinding.FragmentNotificationBinding
import com.shannon.openvoice.dialog.NotificationTypeDialog
import com.shannon.openvoice.model.Notification
import com.shannon.openvoice.model.NotificationTypeSelectedBean
import com.shannon.openvoice.util.HttpHeaderLink
import com.shannon.openvoice.util.looksLikeMastodonUrl
import com.shannon.openvoice.util.openLink
import me.jessyan.autosize.AutoSizeConfig
import me.jessyan.autosize.utils.ScreenUtils

/**
 *
 * @ProjectName:    OpenVoice
 * @Package:        com.shannon.openvoice.business.main.notification
 * @ClassName:      NotificationFragment
 * @Description:     通知
 * @Author:         czhen
 * @CreateDate:     2022/7/25 16:51
 */
class NotificationFragment : KBaseFragment<FragmentNotificationBinding, NotificationModel>(),
    LinkListener, RefreshableFragment {

    //本地分页page
    private var pageNum: Int = 1

    //最后一条数据中id，用来加载新数据
    private var maxId: String? = null

    //刷新列表从该id开始
    private var sinceId: String? = null

    //过滤排除
    private var notificationFilter: MutableSet<Notification.Type> = mutableSetOf()

    private val notificationListAdapter: NotificationListAdapter by lazy {
        NotificationListAdapter(requireContext(), this)
    }

    companion object {
        fun newInstance(): NotificationFragment {
            return NotificationFragment()
        }
    }

    override fun onInit() {
        binding.apply {
            //添加默认排除的数据
            notificationFilter.clear()
            notificationFilter.add(Notification.Type.FOLLOW_REQUEST)
            notificationFilter.add(Notification.Type.POLL)
            notificationFilter.add(Notification.Type.STATUS)
            notificationFilter.add(Notification.Type.SIGN_UP)
            notificationFilter.add(Notification.Type.UPDATE)

            //判断是否有缓存数据
            val NotificationTypeSelectedBeanJson =
                PreferencesUtil.getString(PreferencesUtil.Constant.NOTIFICATION_FILTER, "")
            if (!isBlankPlus(NotificationTypeSelectedBeanJson)) {
                var NotificationTypeSelectedBeans: MutableList<NotificationTypeSelectedBean> =
                    Gson().fromJson(
                        NotificationTypeSelectedBeanJson,
                        object : TypeToken<MutableList<NotificationTypeSelectedBean?>?>() {}.type
                    )

                NotificationTypeSelectedBeans.forEach { bean ->
                    if (bean.isTypeSelected) {
                        notificationFilter.remove(bean.notificationtype)
                    } else {
                        notificationFilter.add(bean.notificationtype)
                    }
                }
            }

            itFilter.apply {
                setUIFormatType(ImageAndTextUi.ImageAndTextType.left_right)
                setAvatarDataFromRes(imgId = R.drawable.ic_opv_notification_filter, 24.dp, 24.dp)
                setTextData(
                    content = getString(R.string.filter),
                    _textSize = 16f,
                    _textColorInt =
                    ThemeUtil.getColor(
                        requireContext(),
                        R.attr.textColorPrimary
                    )
                )
                changeTvContentPositionToAvatar(12.dp)

                setOnClickListener {
                    NotificationTypeDialog(
                        requireContext(),
                        width = WindowManager.LayoutParams.MATCH_PARENT,
                        height = (0.8 * AutoSizeConfig.getInstance().screenHeight).toInt()
                    ).apply {
                        setBG(R.attr.colorBackgroundPlaceholder)
                        tvSaveOnClick { beans ->
                            beans.forEach { bean ->
                                if (bean.isTypeSelected) {
                                    notificationFilter.remove(bean.notificationtype)
                                } else {
                                    notificationFilter.add(bean.notificationtype)
                                }
                            }

                            pageNum = 1
                            maxId = null
                            viewModel.notifications(
                                maxId,
                                sinceId,
                                notificationFilter
                            ) { isSuccess, linkHeader ->
                                if (isSuccess) {
                                    val links = HttpHeaderLink.parse(linkHeader)
                                    val next = HttpHeaderLink.findByRelationType(links, "next")
                                    maxId = next?.uri?.getQueryParameter("max_id")
                                }
                            }
                            dismissDialog()
                        }
                        show()
                    }
                }
            }

            itClean.apply {
                setUIFormatType(ImageAndTextUi.ImageAndTextType.left_right)
                setAvatarDataFromRes(imgId = R.drawable.ic_opv_notification_clean, 24.dp, 24.dp)
                setTextData(
                    content = getString(R.string.clean),
                    _textSize = 16f,
                    _textColorInt =
                    ThemeUtil.getColor(
                        requireContext(),
                        R.attr.textColorPrimary
                    )
                )
                changeTvContentPositionToAvatar(12.dp)

                singleClick {
                    requireContext().showConfirmDialog(
                        message = getString(R.string.tips_clear_notification),
                    ) {
                        viewModel.clearNotifications(requireActivity()) { isSuccess ->
                            if (isSuccess) {
                                bindEmptyView()
                            }
                        }
                    }
                }
            }
        }
        initRecyclerView()
        dataEcho()
    }

    override fun onLazyInit() {
        binding.recyclerView.isRefreshing = true
    }

    override fun refreshContent() {
        if (isInitialized())
            binding.recyclerView.isRefreshing = true
    }

    /**
     * 初始化列表数据
     */
    private fun initRecyclerView() {
        binding.apply {
            recyclerView.apply {
                layoutManager = LinearLayoutManager(requireContext())
                adapter = notificationListAdapter
                itemDivider(colorRes = R.color.transparent, size = 16.dp)
                clearAnimation()

                setOnItemChildClickListener { view, position ->
                    val bean = notificationListAdapter.getItemData(position)

                    when (view.id) {
                        R.id.sivAvatar, R.id.tvUserNameNickName -> {
                            when (bean.type) {
                                Notification.Type.MENTION, Notification.Type.REBLOG,
                                Notification.Type.FAVOURITE, Notification.Type.FOLLOW -> {
                                    requireContext().startActivity(
                                        AccountActivity.newIntent(
                                            requireContext(),
                                            bean.account.id
                                        )
                                    )
                                }
                            }
                        }
                        R.id.tvForwarderUsername -> {
                            when (bean.type) {
                                Notification.Type.REBLOG, Notification.Type.FAVOURITE -> {
                                    requireContext().startActivity(
                                        AccountActivity.newIntent(
                                            requireContext(),
                                            bean.status?.account?.id ?: ""
                                        )
                                    )
                                }
                            }
                        }
                        R.id.clDuwenContent, R.id.tvStatusContent -> {
                            requireContext().startActivity(
                                StatusDetailActivity.newIntent(
                                    requireContext(),
                                    bean.status!!.copy(isPlaying = false)
                                )
                            )
                        }
                        R.id.tvFollowTips -> {
                            requireContext().startActivity(
                                AccountActivity.newIntent(
                                    requireContext(),
                                    bean.account.id
                                )
                            )
                        }
                    }
                }
                setOnRefreshListener { refresh() }

                setOnLoadMoreListener {
                    if (!isBlankPlus(maxId)) {
                        pageNum++

                        viewModel.notifications(
                            maxId,
                            sinceId,
                            notificationFilter
                        ) { isSuccess, linkHeader ->
                            if (isSuccess) {
                                val links = HttpHeaderLink.parse(linkHeader)
                                val next = HttpHeaderLink.findByRelationType(links, "next")
                                maxId = next?.uri?.getQueryParameter("max_id")
                            }
                        }
                    }
                }
            }
        }
    }

    private fun refresh() {
        pageNum = 1
        maxId = null

        viewModel.notifications(
            maxId,
            sinceId,
            notificationFilter
        ) { isSuccess, linkHeader ->
            if (isSuccess) {
                val links = HttpHeaderLink.parse(linkHeader)
                val next = HttpHeaderLink.findByRelationType(links, "next")
                maxId = next?.uri?.getQueryParameter("max_id")
            }
        }
    }

    /**
     * 数据回显
     */
    var allNotifications = mutableListOf<Notification>()
    private fun dataEcho() {
        viewModel.notificationsLive.observe(this) { beans ->
            if (pageNum == 1) {
                if (beans.isEmpty()) {
                    bindEmptyView()
                } else {
//                    sinceId = allNotifications[0].id
                    binding.recyclerView.removeEmptyView()
                    notificationListAdapter.setNewDataIgnoreSize(beans)
                }
            } else {
                notificationListAdapter.addMoreData(beans)
            }

        }
    }

    /**
     * 当列表数据为空的时候，显示
     */
    private fun bindEmptyView() {
        val errorPageView = ErrorPageView(requireContext())
        with(errorPageView) {
            post {
                val recyclerViewHeight = binding.recyclerView.height
                setErrorIcon(
                    R.drawable.ic_development_notification,
                    120.dp,
                    120.dp
                )
                setErrorContent(
                    resources.getString(R.string.desc_wilderness),
                    12f,
                    _textColor = ThemeUtil.getTypedValue(
                        requireContext(),
                        com.shannon.android.lib.R.attr.emptyLayoutButtonTextColor
                    ).coerceToString().toString()
                )
                changeErrorIconPositionToTOP(
                    (0.18 * recyclerViewHeight).toInt()
                )
                changeErrorTextPositionToTOP(0.dp)
            }
        }

        binding.recyclerView.apply {
            setEmptyView(errorPageView)
//            setLoadingMoreBottomHeight(0f)
            isLoadMoreEnabled = false
            notificationListAdapter.setNewData(arrayListOf())
        }
    }

    /**
     * 开启过滤条件设置
     */
    fun turnOnFilterSettings() {
//        LogUtils.i(
//            "是否是设备高度 =-= ${AutoSizeConfig.getInstance().isUseDeviceSize}",
//            "设备高度是多少 =-= ${ScreenUtils.getScreenSize(FunApplication.getInstance())[1]}",
//            "屏幕高度 =-= ${AutoSizeConfig.getInstance().screenHeight}"
//        )
        NotificationTypeDialog(
            requireContext(),
            width = WindowManager.LayoutParams.MATCH_PARENT,
            height = ScreenUtils.getScreenSize(FunApplication.getInstance())[1]
        ).apply {
            setBG(R.attr.colorBackgroundDarkGreen)
            tvSaveOnClick { beans ->
                beans.forEach { bean ->
                    if (bean.isTypeSelected) {
                        notificationFilter.remove(bean.notificationtype)
                    } else {
                        notificationFilter.add(bean.notificationtype)
                    }
                }

                pageNum = 1
                maxId = null
                viewModel.notifications(
                    maxId,
                    sinceId,
                    notificationFilter
                ) { isSuccess, linkHeader ->
                    if (isSuccess) {
                        val links = HttpHeaderLink.parse(linkHeader)
                        val next = HttpHeaderLink.findByRelationType(links, "next")
                        maxId = next?.uri?.getQueryParameter("max_id")
                    }
                }
                dismissDialog()
            }
            show()
        }
    }

    /**
     * 清空通知消息
     */
    fun clearNotificationMessage() {
        requireContext().showConfirmDialog(
            message = getString(R.string.tips_clear_notification),
        ) {
            viewModel.clearNotifications(requireActivity()) { isSuccess ->
                if (isSuccess) {
                    bindEmptyView()
                }
            }
        }
    }


    override fun onViewTag(tag: String) {
        requireContext().startActivity(StatusListActivity.newHashtagIntent(requireContext(), tag))
    }

    override fun onViewAccount(id: String) {
        requireContext().startActivity(AccountActivity.newIntent(requireContext(), id))
    }

    override fun onViewUrl(url: String) {
        if (!looksLikeMastodonUrl(url)) {
            requireContext().openLink(url)
        }
    }
}