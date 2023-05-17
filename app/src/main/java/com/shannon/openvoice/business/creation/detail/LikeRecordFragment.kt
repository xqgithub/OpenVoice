package com.shannon.openvoice.business.creation.detail

import android.os.Bundle
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import com.shannon.android.lib.base.activity.KBaseFragment
import com.shannon.android.lib.extended.*
import com.shannon.openvoice.R
import com.shannon.openvoice.business.creation.CreationViewModel
import com.shannon.openvoice.databinding.FragmentModelLikesBinding
import com.shannon.openvoice.model.EventLikeVoice
import com.shannon.openvoice.model.Likes
import com.shannon.openvoice.model.ModelLikes
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode

/**
 *
 * @Package:        com.shannon.openvoice.business.creation.detail
 * @ClassName:      LikeRecordFragment
 * @Description:     作用描述
 * @Author:         czhen
 * @CreateDate:     2022/8/27 9:43
 */
class LikeRecordFragment : KBaseFragment<FragmentModelLikesBinding, CreationViewModel>() {
    private var modelId: Long = 0L
    private val mAdapter = LikeRecordAdapter()
    private var pageNum = 0
    override fun onInit() {
        EventBus.getDefault().register(this)
        modelId = arguments?.getLong(MODEL_ID_ARGS, 0L) ?: 0L
        binding.run {
            recyclerView.layoutManager = LinearLayoutManager(requireContext())
            recyclerView.adapter = mAdapter
            recyclerView.clearAnimations()
            recyclerView.itemDivider(footerNoShowSize = 2)
            recyclerView.setPadding(0, 0, 0, 16.dp)
            recyclerView.clipToPadding = false
            recyclerView.setOnRefreshListener { refresh() }
            recyclerView.setOnLoadMoreListener {
                pageNum++
                viewModel.voiceModelLikes(modelId, pageNum, this@LikeRecordFragment::loadMore)
            }
            recyclerView.setOnItemChildClickListener { view, position ->
                val bean = mAdapter.getItemData(position)
                if (view.id == R.id.followButton) {
                    if (bean.isFollow) {
                        requireContext().showDialogConfirmWithIcon(
                            message = getString(R.string.desc_sure_unfollow),
                            drawableRes = R.drawable.icon_dialog_mark,
                            doConfirm = {
                                viewModel.unfollowAccount(bean.account.id) {
                                    mAdapter.data[position] = bean.copy(isFollow = false)
                                    mAdapter.refreshNotifyItemChanged(position)
                                }
                            }
                        )
                    } else {
                        viewModel.followAccount(bean.account.id) {
                            mAdapter.data[position] = bean.copy(isFollow = true)
                            mAdapter.refreshNotifyItemChanged(position)
                        }
                    }
                }
            }
        }
    }

    override fun onLazyInit() {
        binding.recyclerView.isRefreshing = true
    }

    private fun refresh() {
        pageNum = 0
        viewModel.voiceModelLikes(modelId, pageNum, this@LikeRecordFragment::refreshData)
    }

    private fun refreshData(data: List<ModelLikes>) {
        if (data.isEmpty()) {
            binding.recyclerView.bindEmptyView(
                R.drawable.ic_development, R.string.desc_wilderness,
                height = ViewGroup.LayoutParams.MATCH_PARENT
            )
        } else {
            binding.recyclerView.removeEmptyView()
        }
        mAdapter.setNewData(data)
    }

    private fun loadMore(data: List<ModelLikes>) {
        mAdapter.addMoreData(data)
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun subscribeLikeEvent(event: EventLikeVoice) {
        if (event.hashCode == hashCode()) return
        refresh()
    }

    override fun onDestroy() {
        super.onDestroy()
        EventBus.getDefault().unregister(this)
    }

    companion object {
        private const val MODEL_ID_ARGS = "modelId"
        fun newInstance(modelId: Long): LikeRecordFragment {
            val fragment = LikeRecordFragment()
            fragment.arguments = Bundle().apply {
                putLong(MODEL_ID_ARGS, modelId)
            }
            return fragment
        }
    }
}