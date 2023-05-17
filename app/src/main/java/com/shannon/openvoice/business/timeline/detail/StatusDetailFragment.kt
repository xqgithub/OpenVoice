package com.shannon.openvoice.business.timeline.detail

import android.os.Bundle
import androidx.recyclerview.widget.LinearLayoutManager
import com.shannon.android.lib.base.activity.KBaseFragment
import com.shannon.android.lib.extended.clearAnimations
import com.shannon.android.lib.extended.dp
import com.shannon.openvoice.business.player.MediaInfo
import com.shannon.openvoice.business.player.PlayerHelper
import com.shannon.openvoice.business.player.listener.OnPlaylistListener
import com.shannon.openvoice.business.timeline.StatusActionImpl
import com.shannon.openvoice.business.timeline.TimelineViewModel
import com.shannon.openvoice.business.timeline.listener.FragmentListener
import com.shannon.openvoice.business.timeline.listener.StatusActionListener
import com.shannon.openvoice.databinding.FragmentTimelineBinding
import com.shannon.openvoice.model.*
import com.shannon.openvoice.util.SmoothTopScroller
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import timber.log.Timber

/**
 *
 * @Package:        com.shannon.openvoice.business.timeline.detail
 * @ClassName:      StatusDetailFragment
 * @Description:     作用描述
 * @Author:         czhen
 * @CreateDate:     2022/8/18 10:07
 */
class StatusDetailFragment : KBaseFragment<FragmentTimelineBinding, TimelineViewModel>(),
    StatusActionListener by StatusActionImpl(), OnPlaylistListener, FragmentListener {
    private lateinit var status: StatusModel
    private val mAdapter by lazy { StatusDetailAdapter(requireContext(), this) }
    private val layoutManager by lazy { LinearLayoutManager(requireContext()) }
    private val timelineKind = TimelineViewModel.Kind.DETAIL

    companion object {
        private const val EXTRA_MODEL = "model"

        fun newIntent(model: StatusModel): StatusDetailFragment {
            return StatusDetailFragment().apply {
                arguments = Bundle().apply { putParcelable(EXTRA_MODEL, model) }
            }
        }
    }

    override fun onInit() {
        status = arguments?.getParcelable(EXTRA_MODEL)!!
        EventBus.getDefault().register(this)
        viewModel.init(viewLifecycleOwner, timelineKind, mAdapter, arrayListOf(), null, 0)
        bindHost(timelineKind, this, viewModel, mAdapter)
        binding.run {
            recyclerView.layoutManager = layoutManager
            recyclerView.adapter = mAdapter
            recyclerView.clearAnimations()
            recyclerView.setPadding(0, 0, 0, 70.dp)
            recyclerView.clipToPadding = false
        }

        loadData()
    }

    private fun loadData() {
        viewModel.fetchStatusContext(status.actionableId) {
            val dataList = arrayListOf<StatusModel>()
            dataList.addAll(it.ancestors)
            dataList.add(status)
            mAdapter.detailedStatusPosition = dataList.lastIndex
            dataList.addAll(it.descendants)
            mAdapter.setNewData(dataList)
            viewModel.findNonGenerated(dataList)
            viewModel.scheduleTimer()
            PlayerHelper.instance.refreshed(PlayerHelper.convert2MediaInfo(dataList),  getTimelinePageId(),this)
        }
    }

    private fun observerScrollable(mediaId: String) {
        val position = mAdapter.findStatusById(mediaId)
        if (position == -1) return
        val smoothScroller = SmoothTopScroller(requireContext())
        smoothScroller.targetPosition = position + binding.recyclerView.customTopItemViewCount
        layoutManager.startSmoothScroll(smoothScroller)
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun subscribeBlackList(event: EventBlackList) {
        val removedList = mAdapter.removeAllByAccountId(event.accountId)
        if (mAdapter.itemCount == 0) {
            onBackPressed()
        } else {
            removedList.forEach {
                PlayerHelper.instance.removeMediaItem(it.id)
            }
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun subscribePurchasedModel(event: EventPurchasedModel) {
//        mAdapter.onPurchasedVoiceModel(event.modelId)
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun subscribeStatus(event: EventComposeStatus) {
        if (event.fromPage == timelineKind) return

        if (event.type == EventComposeStatus.Type.CREATE || event.type == EventComposeStatus.Type.REPLAY) {
            loadData()
        } else if (event.type == EventComposeStatus.Type.DELETE) {
            mAdapter.removeItem(event.status)
            PlayerHelper.instance.removeMediaItem(event.status.id)
            if (mAdapter.itemCount == 0) onBackPressed()

        } else if (event.type == EventComposeStatus.Type.REBLOG) {
            val position = mAdapter.data.indexOfFirst { it.id == event.status.actionableId }
            if (position != -1)
                mAdapter.onReblog(event.status, position)
        } else if (event.type == EventComposeStatus.Type.PIN) {
            mAdapter.onPin(event.status)
        } else if (event.type == EventComposeStatus.Type.FAVOURITES) {
            mAdapter.onFavourite(event.status)
        } else if (event.type == EventComposeStatus.Type.VOICE_MODEL) {
            mAdapter.onUpdateVoiceModel(event.status)
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun eventBusAccountBean(event: AccountBean) {
        mAdapter.updateAccountData(event)
    }

    override fun handleOnBackPressed(): Boolean {
        return super.handleOnBackPressed()
    }

    override fun requireAct() = requireActivity()

    override fun deleted() {
        if (mAdapter.itemCount == 0) requireActivity().onBackPressed()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        EventBus.getDefault().unregister(this)
    }

    override fun onIsPlayingChanged(item: MediaInfo, isPlaying: Boolean) {
        val position = mAdapter.findStatusById(item.id)
        if (position == -1) return
        Timber.e("playingState = $position ; $isPlaying")
        mAdapter.playingStateChange(isPlaying, position)
    }

    override fun onPlaylistDestroy(item: MediaInfo) {
        onIsPlayingChanged(item, false)
    }

    override fun navigateTo(item: MediaInfo) {
        observerScrollable(item.id)
    }

    override fun getTimelinePageId(): String {
        return PlayerHelper.generatePageId(timelineKind, status.id)
    }
}