package com.shannon.openvoice.business.main.mine.account

import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import androidx.core.app.ActivityOptionsCompat
import androidx.core.view.ViewCompat
import androidx.recyclerview.widget.GridLayoutManager
import com.blankj.utilcode.util.LogUtils
import com.shannon.android.lib.base.activity.KBaseFragment
import com.shannon.android.lib.extended.addMoreData
import com.shannon.android.lib.extended.bindEmptyView
import com.shannon.android.lib.extended.dp
import com.shannon.android.lib.extended.removeEmptyView
import com.shannon.android.lib.util.ScreenTools
import com.shannon.android.lib.util.ThemeUtil
import com.shannon.openvoice.R
import com.shannon.openvoice.business.main.mine.MineViewModel
import com.shannon.openvoice.business.main.viewmedia.ViewMediaActivity
import com.shannon.openvoice.business.timeline.TimelineViewModel
import com.shannon.openvoice.components.CommentDialogDecoration
import com.shannon.openvoice.components.ErrorPageView
import com.shannon.openvoice.databinding.FragmentAccountmediaBinding
import com.shannon.openvoice.model.Attachment
import com.shannon.openvoice.model.AttachmentViewData
import com.shannon.openvoice.model.StatusModel
import com.shannon.openvoice.util.openLink

/**
 * Date:2022/8/22
 * Time:10:21
 * author:dimple
 * 媒体显示
 */
class AccountMediaFragment : KBaseFragment<FragmentAccountmediaBinding, MineViewModel>() {

    //列表本地分页page
    private var pageNum: Int = 1

    private lateinit var accountId: String

    private val accountMediaAdapter by lazy {
        AccountMediaAdapter(requireContext())
    }

    companion object {
        fun newInstance(accountId: String): AccountMediaFragment {
            val accountMediaFragment = AccountMediaFragment()
            accountMediaFragment.arguments = Bundle().apply {
                putString("accountId", accountId)
            }
            return accountMediaFragment
        }
    }

    override fun onInit() {
        accountId = arguments?.getString("accountId", "").toString()
        initRecyclerView()
        dataEcho()
    }


    /**
     * 初始化列表数据
     */
    private fun initRecyclerView() {
        binding.apply {
            recyclerView.apply {
                layoutManager = GridLayoutManager(requireContext(), 3)
                adapter = accountMediaAdapter
                clearAnimation()

                addItemDecoration(
                    CommentDialogDecoration().getInstance()
                        .setSpaceItemDecoration(
                            0,
                            5.dp,
                            0,
                            0
                        )
                )

                setOnItemChildClickListener { view, position ->
                    when (view.id) {
                        R.id.cl_main -> {
                            val attachmentViewData = mutableListOf<AttachmentViewData>()
                            statusModels.forEach { bean ->
                                attachmentViewData.addAll(AttachmentViewData.list(bean))
                            }
                            viewMedia(attachmentViewData, position, view)
                        }
                    }
                }

                setOnRefreshListener {
                    pageNum = 1
                    viewModel.accountStatusesOnlyMedia(accountId, null, this@AccountMediaFragment)
                }
                setOnLoadMoreListener {
                    statusModels.lastOrNull()?.let { (id) ->
                        pageNum++
                        viewModel.accountStatusesOnlyMedia(accountId, id, this@AccountMediaFragment)
                    }
                }
            }
        }
    }


    override fun onLazyInit() {
        viewModel.accountStatusesOnlyMedia(accountId, null, this)
    }

    /**
     * 数据回显
     */

    var statusModels = mutableListOf<StatusModel>()
    private fun dataEcho() {
        viewModel.accountStatusesOnlyMediaLive.observe(this) { beans ->
//            LogUtils.e("accountStatusesOnlyMediaLive =-=  $bean")
            if (pageNum == 1) {
                if (beans.isEmpty()) {
                    bindEmptyView()
                } else {
                    binding.recyclerView.removeEmptyView()
                    val attachmentViewData = mutableListOf<AttachmentViewData>()
                    statusModels.clear()
                    statusModels.addAll(beans)
                    statusModels.forEach { bean ->
                        attachmentViewData.addAll(AttachmentViewData.list(bean))
                    }
                    accountMediaAdapter.setNewData(attachmentViewData)
                }
            } else {
                val attachmentViewData = mutableListOf<AttachmentViewData>()
                if (!beans.isEmpty()) {
                    statusModels.addAll(beans)
                    beans.forEach { bean ->
                        attachmentViewData.addAll(AttachmentViewData.list(bean))
                    }
                }
                accountMediaAdapter.addMoreData(attachmentViewData)
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
                    R.drawable.ic_development,
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
            isLoadMoreEnabled = false
            accountMediaAdapter.setNewDataIgnoreSize(arrayListOf())
        }
    }


    private fun viewMedia(items: List<AttachmentViewData>, currentIndex: Int, view: View?) {
        when (items[currentIndex].attachment.type) {
            Attachment.Type.IMAGE,
            Attachment.Type.GIFV,
            Attachment.Type.VIDEO,
            Attachment.Type.AUDIO -> {
                val intent = ViewMediaActivity.newIntent(context, items, currentIndex)
                if (view != null && activity != null) {
                    val url = items[currentIndex].attachment.url
                    ViewCompat.setTransitionName(view, url)
                    val options = ActivityOptionsCompat.makeSceneTransitionAnimation(
                        requireActivity(),
                        view,
                        url
                    )
                    startActivity(intent, options.toBundle())
                } else {
                    startActivity(intent)
                }
            }
            Attachment.Type.UNKNOWN -> {
                context?.openLink(items[currentIndex].attachment.url)
            }
        }
    }


}