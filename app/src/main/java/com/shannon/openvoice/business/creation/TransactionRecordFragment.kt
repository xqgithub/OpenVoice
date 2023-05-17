package com.shannon.openvoice.business.creation

import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import com.shannon.android.lib.base.activity.KBaseFragment
import com.shannon.android.lib.extended.*
import com.shannon.openvoice.R
import com.shannon.openvoice.business.timeline.listener.RefreshableFragment
import com.shannon.openvoice.databinding.FragmentTransactionRecordBinding
import com.shannon.openvoice.model.Tradings

/**
 *
 * @Package:        com.shannon.openvoice.business.creation
 * @ClassName:      TransactionRecordFragment
 * @Description:     作用描述
 * @Author:         czhen
 * @CreateDate:     2022/8/24 16:37
 */
class TransactionRecordFragment :
    KBaseFragment<FragmentTransactionRecordBinding, CreationViewModel>(), RefreshableFragment {
    private val mAdapter = TransactionRecordAdapter()
    private var pageNum = 0


    override fun onInit() {
        binding.run {
            recyclerView.layoutManager = LinearLayoutManager(requireContext())
            recyclerView.adapter = mAdapter
            recyclerView.clearAnimations()
            recyclerView.itemDivider(footerNoShowSize = 2)
            recyclerView.setOnRefreshListener { refresh() }
            recyclerView.setOnLoadMoreListener {
                pageNum++
                viewModel.accountTradings(pageNum, this@TransactionRecordFragment::loadMore)
            }
        }
    }

    override fun refreshContent() {
        if (isInitialized())
            binding.recyclerView.isRefreshing = true
    }

    override fun onLazyInit() {
        binding.recyclerView.isRefreshing = true
    }

    private fun refresh() {
        pageNum = 0
        viewModel.accountTradings(pageNum, this@TransactionRecordFragment::refreshData)
    }

    private fun refreshData(data: List<Tradings>) {
        if (data.isEmpty()) {
            binding.recyclerView.bindEmptyView(
                R.drawable.ic_development_transaction, R.string.desc_wilderness,
                height = ViewGroup.LayoutParams.MATCH_PARENT
            )
        } else {
            binding.recyclerView.removeEmptyView()
        }
        mAdapter.setNewData(data)
    }

    private fun loadMore(data: List<Tradings>) {
        mAdapter.addMoreData(data)
    }
}