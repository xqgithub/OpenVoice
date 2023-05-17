package com.shannon.openvoice.business.creation.detail

import android.os.Bundle
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import com.shannon.android.lib.base.activity.KBaseFragment
import com.shannon.android.lib.extended.*
import com.shannon.openvoice.R
import com.shannon.openvoice.business.creation.CreationViewModel
import com.shannon.openvoice.databinding.FragmentTransactionRecordBinding
import com.shannon.openvoice.event.UniversalEvent
import com.shannon.openvoice.model.ModelDetail
import com.shannon.openvoice.model.Tradings
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode

/**
 *
 * @Package:        com.shannon.openvoice.business.creation
 * @ClassName:      TransactionRecordFragment
 * @Description:     作用描述
 * @Author:         czhen
 * @CreateDate:     2022/8/24 16:37
 */
class TransactionRecordFragment :
    KBaseFragment<FragmentTransactionRecordBinding, CreationViewModel>() {

    private var modelId: Long = 0L
    private val mAdapter = TransactionRecordAdapter()
    private var pageNum = 0

    override fun onInit() {
        modelId = arguments?.getLong(MODEL_ID_ARGS, 0L) ?: 0L
        binding.run {
            recyclerView.layoutManager = LinearLayoutManager(requireContext())
            recyclerView.adapter = mAdapter
            recyclerView.clearAnimations()
            recyclerView.setPadding(0, 0, 0, 16.dp)
            recyclerView.clipToPadding = false
            recyclerView.setOnRefreshListener { refresh() }
            recyclerView.setOnLoadMoreListener {
                pageNum++
                viewModel.voiceModelTradings(
                    modelId,
                    pageNum,
                    this@TransactionRecordFragment::loadMore
                )
            }
        }

    }

    override fun onLazyInit() {
        binding.recyclerView.isRefreshing = true
    }

    private fun refresh() {
        pageNum = 0
        viewModel.voiceModelTradings(
            modelId,
            pageNum,
            this@TransactionRecordFragment::refreshData
        )
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

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun subscribePaymentEvent(event: UniversalEvent) {
        if (event.actionType != UniversalEvent.payment) return
        refresh()
    }

    companion object {
        private const val MODEL_ID_ARGS = "modelId"
        fun newInstance(modelId: Long): TransactionRecordFragment {
            val fragment = TransactionRecordFragment()
            fragment.arguments = Bundle().apply {
                putLong(MODEL_ID_ARGS, modelId)
            }
            return fragment
        }
    }
}