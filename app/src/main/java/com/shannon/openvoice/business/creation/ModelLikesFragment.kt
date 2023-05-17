package com.shannon.openvoice.business.creation

import android.os.Bundle
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import com.shannon.android.lib.base.activity.KBaseFragment
import com.shannon.android.lib.extended.*
import com.shannon.android.lib.util.ToastUtil
import com.shannon.openvoice.R
import com.shannon.openvoice.business.main.mine.account.AccountManager
import com.shannon.openvoice.business.pay.PaymentActivity
import com.shannon.openvoice.business.timeline.listener.RefreshableFragment
import com.shannon.openvoice.databinding.FragmentModelLikesBinding
import com.shannon.openvoice.model.EventActivateVoice
import com.shannon.openvoice.model.EventLikeVoice
import com.shannon.openvoice.model.EventPurchasedModel
import com.shannon.openvoice.model.Likes
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode

/**
 *
 * @Package:        com.shannon.openvoice.business.creation
 * @ClassName:      ModelLikesFragment
 * @Description:     作用描述
 * @Author:         czhen
 * @CreateDate:     2022/8/24 11:30
 */
class ModelLikesFragment : KBaseFragment<FragmentModelLikesBinding, CreationViewModel>(),
    ModelLikesAdapter.AdapterHandler, RefreshableFragment {
    private lateinit var mAdapter: ModelLikesAdapter
    private lateinit var accountId: String
    private var otherUser = false
    private var pageNum = 0
    override fun onInit() {
        EventBus.getDefault().register(this)
        otherUser = arguments?.getBoolean(OTHER_USER)!!
        accountId = arguments?.getString(ACCOUNT_ID)!!
        mAdapter = ModelLikesAdapter(otherUser, this)
        binding.run {
            recyclerView.layoutManager = LinearLayoutManager(requireContext())
            recyclerView.adapter = mAdapter
            recyclerView.clearAnimations()
            recyclerView.itemDivider(footerNoShowSize = 2)
            recyclerView.setOnRefreshListener { refresh() }
            recyclerView.setOnLoadMoreListener {
                pageNum++
                viewModel.fetchLikes(
                    otherUser,
                    accountId,
                    pageNum,
                    this@ModelLikesFragment::loadMore
                )
            }
        }
    }

    override fun refreshContent() {
        if (isInitialized()) {
            binding.recyclerView.isRefreshing = true
        }
    }

    override fun onLazyInit() {
        binding.recyclerView.isRefreshing = true
    }

    private fun refresh() {
        pageNum = 0
        viewModel.fetchLikes(
            otherUser,
            accountId,
            pageNum,
            this@ModelLikesFragment::loadData
        )
    }

    private fun loadData(data: List<Likes>) {
        if (data.isEmpty()) {
            val drawable =
                if (otherUser) R.drawable.ic_development_model_list else R.drawable.ic_development
            binding.recyclerView.bindEmptyView(
                drawable, R.string.desc_wilderness,
                height = ViewGroup.LayoutParams.MATCH_PARENT
            )
        } else {
            binding.recyclerView.removeEmptyView()
        }
        mAdapter.setNewData(data)
    }

    private fun loadMore(data: List<Likes>) {
        mAdapter.addMoreData(data)
    }

    companion object {
        private const val OTHER_USER = "otherUser"
        private const val ACCOUNT_ID = "accountId"

        fun newInstance(): ModelLikesFragment {
            return ModelLikesFragment().apply {
                arguments = Bundle().apply {
                    putBoolean(OTHER_USER, false)
                    putString(ACCOUNT_ID, AccountManager.accountManager.getAccountId())
                }
            }
        }

        fun newInstance(accountId: String): ModelLikesFragment {
            return ModelLikesFragment().apply {
                arguments = Bundle().apply {
                    putBoolean(OTHER_USER, true)
                    putString(ACCOUNT_ID, accountId)
                }
            }
        }
    }

    override fun buy(position: Int) {
        val item = mAdapter.getItemData(position)
        viewModel.modelDetail(item.modelId) {
            if (it.isModelOwner) {
                ToastUtil.showCenter(getString(R.string.tips_owned_model))
            } else {
                requireContext().startActivity(
                    PaymentActivity.newIntent(
                        requireContext(),
                        item.modelId
                    )
                )
//                ModelBuyDialog(requireContext(), viewModel, it) {
//                    viewModel.buyModel(item.modelId) {}
//                }.show()
            }

        }
    }

    override fun activate(position: Int) {
        val item = mAdapter.getItemData(position)
        viewModel.activateVoiceModel(item.modelId) {
            if (it.isModelOwner || it.isOfficial) {
                EventBus.getDefault().post(EventActivateVoice(item.modelId, hashCode()))
                mAdapter.setActivatedPosition(position)
            } else {
                mAdapter.data[position] = item.copy(modelOwner = false, account = it.ownerAccount)
                mAdapter.refreshNotifyItemChanged(position)
            }
        }
    }

    override fun like(position: Int) {
        val item = mAdapter.getItemData(position)
        viewModel.likeVoiceModel(item.modelId) {
            mAdapter.data[position] = item.copy(isLiked = true)
            mAdapter.refreshNotifyItemChanged(position)
            EventBus.getDefault().post(EventLikeVoice(item.modelId, true, hashCode()))
        }
    }

    override fun unlike(_position: Int) {
        /** removeData方法  当列表有多条数据的时候，从上往下移除 当移除到最后一条数据的时候会 出现position>0的情况 **/
        var position = _position
        var item = mAdapter.getItemData(position)
        if (isBlankPlus(item)) {
            position = 0
            item = mAdapter.getItemData(position)
        }

        item?.let {
            viewModel.unlikeVoiceModel(item.modelId) {
                if (otherUser) {
                    mAdapter.data[position] = item.copy(isLiked = false)
                    mAdapter.refreshNotifyItemChanged(position)
                    EventBus.getDefault().post(EventLikeVoice(item.modelId, false, hashCode()))
                } else {
                    mAdapter.removeData(position)
                    if (mAdapter.itemCount == 0) loadData(arrayListOf())
                }
            }
        }

    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun subscribeEvent(event: EventActivateVoice) {
        if (event.hashCode == hashCode()) return
        val position = mAdapter.data.indexOfFirst { event.modelId == it.id }
        if (position == -1) {
            binding.recyclerView.isRefreshing = true
        } else {
            mAdapter.setActivatedPosition(position)
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun subscribePurchasedModel(event: EventPurchasedModel) {
        val position = mAdapter.data.indexOfFirst { it.modelId == event.modelId }
        if (position != -1) {
            mAdapter.data[position] = mAdapter.data[position].copy(modelOwner = true)
            mAdapter.refreshNotifyItemChanged(position)
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun subscribeLikeEvent(event: EventLikeVoice) {
        if (event.hashCode == hashCode()) return
        binding.recyclerView.isRefreshing = true
    }

    override fun onDestroy() {
        super.onDestroy()
        EventBus.getDefault().unregister(this)
    }
}