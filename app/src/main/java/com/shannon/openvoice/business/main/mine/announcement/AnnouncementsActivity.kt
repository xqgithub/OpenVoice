package com.shannon.openvoice.business.main.mine.announcement

import androidx.recyclerview.widget.LinearLayoutManager
import com.shannon.android.lib.base.activity.KBaseActivity
import com.shannon.android.lib.extended.dp
import com.shannon.android.lib.extended.isBlankPlus
import com.shannon.android.lib.extended.removeEmptyView
import com.shannon.openvoice.FunApplication.Companion.appViewModel
import com.shannon.openvoice.R
import com.shannon.openvoice.business.main.mine.MineViewModel
import com.shannon.openvoice.business.timeline.listener.LinkListener
import com.shannon.openvoice.components.ErrorPageView
import com.shannon.openvoice.databinding.ActivityAnnouncementsBinding
import com.shannon.openvoice.util.looksLikeMastodonUrl
import com.shannon.openvoice.util.openLink

/**
 * Date:2022/8/12
 * Time:15:29
 * author:dimple
 * 公告页面
 */
class AnnouncementsActivity : KBaseActivity<ActivityAnnouncementsBinding, MineViewModel>(),
    LinkListener {

    private val announcementsAdapter by lazy {
        AnnouncementsAdapter(this@AnnouncementsActivity, this)
    }

    override fun onInit() {

        binding.apply {
            toolsbar.titleView.text = resources.getString(R.string.announcements)
            appViewModel.setTopViewHeight(
                this@AnnouncementsActivity,
                clMain,
                R.id.toolsbar,
                5.dp
            )
        }
        initAnnouncementsRecyclerView()
        dataEcho()
    }

    /**
     * 初始化列表数据
     */
    private fun initAnnouncementsRecyclerView() {
        binding.apply {
            recyclerView.apply {
                layoutManager = LinearLayoutManager(this@AnnouncementsActivity)
                adapter = announcementsAdapter
                clearAnimation()

                setOnRefreshListener {
                    viewModel.listAnnouncements(owner = this@AnnouncementsActivity)
                }
            }
        }
        viewModel.listAnnouncements(owner = this)
    }

    /**
     * 数据回显
     */
    private fun dataEcho() {
        viewModel.apply {
            announcementsLive.observe(this@AnnouncementsActivity) { beans ->
                if (!isBlankPlus(beans) && beans.isNotEmpty()) {
                    binding.recyclerView.removeEmptyView()
                    announcementsAdapter.setNewData(beans)
                } else {
                    bindEmptyView()
                }
            }
        }
    }

    /**
     * 当列表数据为空的时候，显示
     */
    private fun bindEmptyView() {
        val errorPageView = ErrorPageView(this@AnnouncementsActivity)
        with(errorPageView) {
            post {
                val recyclerViewHeight = binding.recyclerView.height
                setErrorIcon(
                    R.drawable.ic_development,
                    120.dp,
                    120.dp
                )
                setErrorContent(resources.getString(R.string.desc_wilderness), 12f, "#A1A7AF")
                changeErrorIconPositionToTOP(
                    (0.5 * recyclerViewHeight).toInt()
                )
                changeErrorTextPositionToTOP(10.dp)
            }
        }

        binding.recyclerView.apply {
            setEmptyView(errorPageView)
//            setLoadingMoreBottomHeight(0f)
            isLoadMoreEnabled = false
            announcementsAdapter.setNewData(arrayListOf())
        }
    }

    override fun onViewTag(tag: String) {
    }

    override fun onViewAccount(id: String) {
    }

    override fun onViewUrl(url: String) {
        if (!looksLikeMastodonUrl(url)) {
            openLink(url)
        }
    }


}