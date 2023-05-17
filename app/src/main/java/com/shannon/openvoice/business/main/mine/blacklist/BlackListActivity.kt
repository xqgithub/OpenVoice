package com.shannon.openvoice.business.main.mine.blacklist

import androidx.recyclerview.widget.LinearLayoutManager
import com.shannon.android.lib.base.activity.KBaseActivity
import com.shannon.android.lib.extended.*
import com.shannon.android.lib.util.ThemeUtil
import com.shannon.openvoice.FunApplication
import com.shannon.openvoice.R
import com.shannon.openvoice.business.main.mine.MineViewModel
import com.shannon.openvoice.components.ErrorPageView
import com.shannon.openvoice.databinding.ActivityBlacklistBinding
import com.shannon.openvoice.util.HttpHeaderLink

/**
 * Date:2022/8/15
 * Time:9:46
 * author:dimple
 * 黑名单列表页面
 */
class BlackListActivity : KBaseActivity<ActivityBlacklistBinding, MineViewModel>() {


    //黑名单列表本地分页page
    private var pageNum: Int = 1
    private var maxId: String? = null

    private val blackBlistAdapter by lazy {
        BlackBlistAdapter(this@BlackListActivity)
    }

    override fun onInit() {
        binding.apply {
            toolsbar.apply {
                titleView.text = resources.getString(R.string.blacklist)
                FunApplication.appViewModel.setTopViewHeight(
                    this@BlackListActivity,
                    clMain,
                    R.id.toolsbar,
                    5.dp
                )
                vDividingLine.gone()
            }
            initBlackListRecyclerView()
            dataEcho()
        }
    }


    /**
     * 初始化列表数据
     */
    private fun initBlackListRecyclerView() {
        binding.apply {
            recyclerView.apply {
                layoutManager = LinearLayoutManager(this@BlackListActivity)
                adapter = blackBlistAdapter
                itemDivider(headerNoShowSize = 0)
                clearAnimation()

                setOnItemChildClickListener { view, position ->
                    when (view.id) {
                        R.id.ibDelete -> {
                            val bean = blackBlistAdapter.getItemData(position)
                            showConfirmDialog(
                                message = getString(
                                    R.string.tips_remove_blacklist
                                )
                            ) {
                                viewModel.unblockAccount(bean.id, position, this@BlackListActivity)
                            }
                        }
                    }
                }

                setOnLoadMoreListener {
                    if (!isBlankPlus(maxId)) {
                        pageNum++
                        viewModel.listblocks(maxId) { isSuccess, linkHeader ->
                            val links = HttpHeaderLink.parse(linkHeader)
                            val next = HttpHeaderLink.findByRelationType(links, "next")
                            maxId = next?.uri?.getQueryParameter("max_id")
                        }
                    }
                }
            }

            viewModel.listblocks(null) { isSuccess, linkHeader ->
                if (isSuccess) {
                    val links = HttpHeaderLink.parse(linkHeader)
                    val next = HttpHeaderLink.findByRelationType(links, "next")
                    maxId = next?.uri?.getQueryParameter("max_id")
                }
            }
        }
    }

    /**
     * 数据回显
     */
    private fun dataEcho() {
        viewModel.apply {
            blocksLive.observe(this@BlackListActivity) { beans ->
                if (pageNum == 1) {
                    if (beans.isEmpty()) {
                        bindEmptyView()
                    } else {
                        binding.recyclerView.removeEmptyView()
                        blackBlistAdapter.setNewData(beans)
                    }
                } else {
                    blackBlistAdapter.addMoreData(beans)
                }
            }

            unblockAccountLive.observe(this@BlackListActivity) {
                blackBlistAdapter.removeData(it.second)
                if (blackBlistAdapter.data.size == 0) {
                    bindEmptyView()
                }
            }
        }
    }

    /**
     * 当列表数据为空的时候，显示
     */
    private fun bindEmptyView() {
        val errorPageView = ErrorPageView(this@BlackListActivity)
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
                        this@BlackListActivity,
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
            blackBlistAdapter.setNewData(arrayListOf())
        }
    }


}