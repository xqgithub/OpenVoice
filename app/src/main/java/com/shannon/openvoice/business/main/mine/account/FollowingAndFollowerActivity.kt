package com.shannon.openvoice.business.main.mine.account

import android.os.Bundle
import androidx.recyclerview.widget.LinearLayoutManager
import com.shannon.android.lib.base.activity.KBaseActivity
import com.shannon.android.lib.constant.ConfigConstants
import com.shannon.android.lib.extended.*
import com.shannon.android.lib.util.ThemeUtil
import com.shannon.openvoice.FunApplication.Companion.appViewModel
import com.shannon.openvoice.R
import com.shannon.openvoice.business.main.mine.MineViewModel
import com.shannon.openvoice.components.ErrorPageView
import com.shannon.openvoice.databinding.ActivityFollowingFollowerBinding
import com.shannon.openvoice.util.HttpHeaderLink

/**
 * Date:2022/8/18
 * Time:15:31
 * author:dimple
 * 关注者和追随者列表页面
 */
class FollowingAndFollowerActivity :
    KBaseActivity<ActivityFollowingFollowerBinding, MineViewModel>() {
    //列表本地分页page
    private var pageNum: Int = 1

    private var maxId: String? = null

    private lateinit var userIdentitType: UserIdentitType

    private lateinit var accountId: String

    private val followingAndFollowerAdapter by lazy {
        FollowingAndFollowerAdapter(this)
    }

    enum class UserIdentitType {
        Following, Follower
    }

    override fun onInit() {
        userIdentitType =
            intent.getSerializableExtra(ConfigConstants.CONSTANT.userIdentitType) as UserIdentitType
        accountId = intent.getStringExtra("accountId")!!

        binding.apply {
            toolsbar.apply {
                titleView.text =
                    if (userIdentitType == UserIdentitType.Following) getString(R.string.follow) else getString(
                        R.string.followers
                    )
                appViewModel.setTopViewHeight(
                    this@FollowingAndFollowerActivity,
                    clMain,
                    R.id.toolsbar,
                    5.dp
                )
                vDividingLine.gone()
            }
            initRecyclerView()
            dataEcho()
        }
    }

    /**
     * 初始化列表数据
     */
    private fun initRecyclerView() {
        binding.apply {
            recyclerView.apply {
                layoutManager = LinearLayoutManager(this@FollowingAndFollowerActivity)
                adapter = followingAndFollowerAdapter
                itemDivider(
                    headerNoShowSize = 0,
                    footerNoShowSize = 1,
                    size = 16.dp,
                    colorRes = R.color.transparent
                )

                clearAnimation()

                setOnItemChildClickListener { view, position ->
                    when (view.id) {
                        R.id.cl_main -> {
                            val bean = followingAndFollowerAdapter.getItemData(position)
                            intentToJump(
                                this@FollowingAndFollowerActivity,
                                AccountActivity::class.java,
                                bundle = Bundle().apply {
                                    putString("accountId", bean.id)
                                }
                            )
                        }
                    }
                }

                setOnLoadMoreListener {
                    if (!isBlankPlus(maxId)) {
                        pageNum++
//                        if (userIdentitType == UserIdentitType.Following) {
//
//                        }
                        viewModel.accountFollowingFollower(
                            accountId,
                            maxId,
                            userIdentitType
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

        viewModel.accountFollowingFollower(
            accountId,
            null,
            userIdentitType
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
    private fun dataEcho() {
        viewModel.accountFollowingFollowerLive.observe(this) { beans ->
            if (pageNum == 1) {
                if (beans.isEmpty()) {
                    bindEmptyView()
                } else {
                    binding.recyclerView.removeEmptyView()
                    followingAndFollowerAdapter.setNewData(beans)
                }
            } else {
                followingAndFollowerAdapter.addMoreData(beans)
            }
        }
    }

    /**
     * 当列表数据为空的时候，显示
     */
    private fun bindEmptyView() {
        val errorPageView = ErrorPageView(this@FollowingAndFollowerActivity)
        with(errorPageView) {
            post {
                val recyclerViewHeight = binding.recyclerView.height
                setErrorIcon(
                    R.drawable.ic_development,
                    120.dp,
                    120.dp
                )
                setErrorContent(
                    getString(R.string.desc_blank_follow),
                    12f,
                    _textColor = ThemeUtil.getTypedValue(
                        this@FollowingAndFollowerActivity,
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
            followingAndFollowerAdapter.setNewData(arrayListOf())
        }
    }


}