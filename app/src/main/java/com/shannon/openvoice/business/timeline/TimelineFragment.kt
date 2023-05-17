package com.shannon.openvoice.business.timeline

import android.os.Bundle
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.shannon.android.lib.base.activity.KBaseFragment
import com.shannon.android.lib.extended.*
import com.shannon.android.lib.util.PreferencesUtil
import com.shannon.android.lib.util.ToastUtil
import com.shannon.openvoice.R
import com.shannon.openvoice.business.creation.ModelListFragment
import com.shannon.openvoice.business.guide.GuidePagesActivity
import com.shannon.openvoice.business.guide.GuideViewModel
import com.shannon.openvoice.business.player.MediaInfo
import com.shannon.openvoice.business.player.PlayerHelper
import com.shannon.openvoice.business.player.listener.OnDataSourceListener
import com.shannon.openvoice.business.player.listener.OnPlayerViewShownListener
import com.shannon.openvoice.business.player.listener.OnPlaylistListener
import com.shannon.openvoice.business.timeline.listener.FragmentListener
import com.shannon.openvoice.business.timeline.listener.RefreshableFragment
import com.shannon.openvoice.business.timeline.listener.StatusActionListener
import com.shannon.openvoice.databinding.FragmentTimelineBinding
import com.shannon.openvoice.event.UniversalEvent
import com.shannon.openvoice.model.*
import com.shannon.openvoice.util.SmoothTopScroller
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import timber.log.Timber

/**
 *
 * @ProjectName:    OpenVoice
 * @Package:        com.shannon.openvoice.business.timeline
 * @ClassName:      TimelineFragment
 * @Description:     java类作用描述
 * @Author:         czhen
 * @CreateDate:     2022/8/1 10:00
 */

class TimelineFragment : KBaseFragment<FragmentTimelineBinding, TimelineViewModel>(),
    StatusActionListener by StatusActionImpl(), RefreshableFragment, OnPlaylistListener,
    FragmentListener, OnDataSourceListener {

    private val layoutManager by lazy { LinearLayoutManager(context) }
    private val mAdapter by lazy { TimelineAdapter(requireContext(), this) }
    private lateinit var timelineKind: TimelineViewModel.Kind
    private val mPageId by lazy { generatePageId() }

    companion object {
        private const val KIND_ARGS = "kind"
        private const val ACCOUNT_ID_ARGS = "accountId"
        private const val HASHTAGS_ARG = "hashtags"
        private const val MODEL_ID_ARGS = "modelId"


        fun newInstance(kind: TimelineViewModel.Kind): TimelineFragment {
            val fragment = TimelineFragment()
            fragment.arguments = Bundle().apply {
                putString(KIND_ARGS, kind.name)
            }
            return fragment
        }

        fun newInstanceWithId(kind: TimelineViewModel.Kind, accountId: String): TimelineFragment {
            val fragment = TimelineFragment()
            fragment.arguments = Bundle().apply {
                putString(KIND_ARGS, kind.name)
                putString(ACCOUNT_ID_ARGS, accountId)
            }
            return fragment
        }

        @JvmStatic
        fun newHashtagInstance(hashtags: List<String>): TimelineFragment {
            val fragment = TimelineFragment()
            val arguments = Bundle(2)
            arguments.putString(KIND_ARGS, TimelineViewModel.Kind.TAG.name)
            arguments.putStringArrayList(HASHTAGS_ARG, ArrayList(hashtags))
            fragment.arguments = arguments
            return fragment
        }

        fun newInstanceWithModel(kind: TimelineViewModel.Kind, modelId: Long): TimelineFragment {
            val fragment = TimelineFragment()
            fragment.arguments = Bundle().apply {
                putString(KIND_ARGS, kind.name)
                putLong(MODEL_ID_ARGS, modelId)
            }
            return fragment
        }
    }

    private var mHashtags = arrayListOf<String>()
    private var mAccountId: String? = null
    private var mModelId = 0L
    override fun onInit() {
        EventBus.getDefault().register(this)
        timelineKind = TimelineViewModel.Kind.valueOf(arguments?.getString(KIND_ARGS)!!)
        mHashtags = if (timelineKind == TimelineViewModel.Kind.TAG) {
            arguments?.getStringArrayList(HASHTAGS_ARG)!!
        } else arrayListOf()
        mAccountId = if (timelineKind == TimelineViewModel.Kind.USER ||
            timelineKind == TimelineViewModel.Kind.USER_PINNED ||
            timelineKind == TimelineViewModel.Kind.USER_WITH_REPLIES
        ) {
            arguments?.getString(ACCOUNT_ID_ARGS)!!
        } else null
        mModelId = if (timelineKind == TimelineViewModel.Kind.MODEL_ASS) {
            arguments?.getLong(MODEL_ID_ARGS)!!
        } else 0L
        viewModel.init(viewLifecycleOwner, timelineKind, mAdapter, mHashtags, mAccountId, mModelId)
        bindHost(timelineKind, this, viewModel, mAdapter)
        binding.run {
            recyclerView.layoutManager = layoutManager
            recyclerView.adapter = mAdapter
            recyclerView.clearAnimations()
            recyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
                override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                    super.onScrollStateChanged(recyclerView, newState)
                    Timber.d("newState: $newState")
                    if (newState == 1) return
                    val shownListener = if (parentFragment is OnPlayerViewShownListener) {
                        parentFragment as OnPlayerViewShownListener
                    } else if (requireActivity() is OnPlayerViewShownListener) {
                        requireActivity() as OnPlayerViewShownListener
                    } else {
                        null
                    }
                    shownListener?.onShown(newState != 2)
                }
            })
//            recyclerView.itemDivider(
//                colorRes = R.color.transparent,
//                size = 16.dp,
//                headerNoShowSize = 1,
//                footerNoShowSize = 2
//            )
            recyclerView.setOnRefreshListener {
                viewModel.fetchTimeline(
                    true,
                    onResult = this@TimelineFragment::refreshData
                )
            }
            recyclerView.setOnLoadMoreListener {
                viewModel.fetchTimeline(
                    false,
                    onResult = this@TimelineFragment::loadData
                )
            }
            if (timelineKind == TimelineViewModel.Kind.MODEL_ASS || timelineKind == TimelineViewModel.Kind.PUBLIC_LOCAL || timelineKind == TimelineViewModel.Kind.HOME) {
                recyclerView.setLoadingMoreBottomHeight(20F)
            }
        }
    }

    private fun resetData() {
        viewModel.fetchTimeline(
            true,
            isUserInput = false,
            onResult = this@TimelineFragment::refreshData
        )
    }

    private fun refreshData(list: List<StatusModel>) {
        if (list.isEmpty()) {
            binding.recyclerView.bindEmptyView(
                getEmptyHintDrawable(), getEmptyHintText(),
                height = ViewGroup.LayoutParams.MATCH_PARENT
            )
        } else {
            binding.recyclerView.removeEmptyView()
        }

        mAdapter.setNewDataIgnoreSize(list)
        if (list.size < TimelineViewModel.LIMIT) binding.recyclerView.loadMoreEnd()
        PlayerHelper.instance.refreshed(PlayerHelper.convert2MediaInfo(list), mPageId, this)
    }

    private fun loadData(list: List<StatusModel>) {
        PlayerHelper.instance.addMediaSource(
            PlayerHelper.convert2MediaInfo(list),
            mPageId,
            false
        )
        mAdapter.addMoreData(list, TimelineViewModel.LIMIT)
    }

    override fun onResume() {
        super.onResume()
        viewModel.scheduleTtsTimer()
        refreshedPlayStatus()
    }

    private fun refreshedPlayStatus() {
        PlayerHelper.instance.refreshed(
            null,
            mPageId,
            this
        )
    }


    override fun onLazyInit() {
        resetData()
        //判断是否显示推荐页新手引导
        if (PreferencesUtil.getBool(
                PreferencesUtil.Constant.RECOMMENDED_PAGE_DISPLAYED_STATUS,
                true
            )
        ) {
            intentToJump(
                requireActivity(), GuidePagesActivity::class.java,
                bundle = Bundle().apply {
                    putString(
                        GuidePagesActivity.kind_type,
                        GuideViewModel.Kind.RecommendedPage.name
                    )
                }
            )
        }
    }

    override fun handleResponseError(errorCode: Int) {
        binding.recyclerView.run {
            if (isRefreshing) isRefreshing = false
            if (isLoadingMore) loadMoreFail()
        }
    }

    private fun observePlayingState(state: Pair<String, Boolean>) {
        val position = mAdapter.findStatusById(state.first)
        if (position == -1) return
        Timber.e("playingState = $position ; ${state.second}")
        mAdapter.playingStateChange(state.second, position)
    }

    private fun observerScrollable(mediaId: String) {
        val position = mAdapter.findStatusById(mediaId)
        if (position == -1 || layoutManager.isSmoothScrolling) return
        val smoothScroller = SmoothTopScroller(requireContext())
        Timber.e("observerScrollable = $position ")

        smoothScroller.targetPosition = position + binding.recyclerView.customTopItemViewCount
        if (!layoutManager.isSmoothScrolling)
            layoutManager.startSmoothScroll(smoothScroller)
    }

    override fun refreshContent() {
        binding.recyclerView.isRefreshing = true
    }


    @Subscribe(threadMode = ThreadMode.MAIN)
    fun subscribeBlackList(event: EventBlackList) {
        val removedList = mAdapter.removeAllByAccountId(event.accountId)
        removedList.forEach { PlayerHelper.instance.removeMediaItem(it.id) }

        if (mAdapter.itemCount == 0) resetData()

    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun subscribePurchasedModel(event: EventPurchasedModel) {
//        mAdapter.onPurchasedVoiceModel(event.modelId)
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun subscribeStatus(event: EventComposeStatus) {
        if (event.fromPage == timelineKind || !isInitialized()) return
        if (event.type == EventComposeStatus.Type.CREATE || event.type == EventComposeStatus.Type.REPLAY) {
            viewModel.fetchTimeline(
                true,
                onResult = this@TimelineFragment::refreshData
            )
        } else if (event.type == EventComposeStatus.Type.DELETE) {
            mAdapter.removeItem(event.status)
            PlayerHelper.instance.removeMediaItem(event.status.id)
            if (mAdapter.itemCount == 0) {
                viewModel.fetchTimeline(true, onResult = this@TimelineFragment::refreshData)
            }
        } else if (event.type == EventComposeStatus.Type.REBLOG) {
            val position =
                mAdapter.data.indexOfFirst { it.actionableId == event.status.actionableId }
            if (position != -1)
                mAdapter.onReblog(event.status, position)
        } else if (event.type == EventComposeStatus.Type.PIN) {
            if (timelineKind == TimelineViewModel.Kind.USER_PINNED) {
                binding.recyclerView.isRefreshing = true
            } else {
                mAdapter.onPin(event.status)
            }
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

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun eventBusUniversalEvent(event: UniversalEvent) {
        if (event.actionType == UniversalEvent.closeAudioPlayer) {
            PlayerHelper.instance.releaseMusic()
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        var data = mAdapter.data.toMutableList()
        if (data.isNotEmpty()) {
            data = if (data.size > 120) data.subList(0, 120).toMutableList() else data
            viewModel.onSaveInstanceState(data)
        }
    }

    private fun getEmptyHintText(): Int {
        return when (timelineKind) {
            TimelineViewModel.Kind.HOME -> R.string.desc_blank_follow
            else -> R.string.desc_wilderness
        }
    }

    private fun getEmptyHintDrawable(): Int {
        return when (timelineKind) {
            TimelineViewModel.Kind.HOME -> R.drawable.ic_development_follow
            TimelineViewModel.Kind.PUBLIC_LOCAL -> R.drawable.ic_development_public
            else -> R.drawable.ic_development
        }
    }

    override fun handleOnBackPressed(): Boolean {
//        PlayerHelper.instance.releaseMusic()
        return super.handleOnBackPressed()
    }

    override fun requireAct() = requireActivity()

    override fun deleted() {
        if (mAdapter.itemCount == 0) {
            viewModel.fetchTimeline(true, onResult = this@TimelineFragment::refreshData)
        }
    }

    override fun getTimelinePageId(): String {
        return mPageId
    }

    private fun generatePageId(): String {
        return when (timelineKind) {
            TimelineViewModel.Kind.USER, TimelineViewModel.Kind.USER_WITH_REPLIES, TimelineViewModel.Kind.USER_PINNED -> {
                PlayerHelper.generatePageId(timelineKind, mAccountId ?: "")
            }
            TimelineViewModel.Kind.MODEL_ASS -> {
                PlayerHelper.generatePageId(timelineKind, mModelId.toString())
            }
            TimelineViewModel.Kind.TAG -> {
                PlayerHelper.generatePageId(timelineKind, mHashtags.toString())
            }
            else -> {
                PlayerHelper.generatePageId(timelineKind)
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        EventBus.getDefault().unregister(this)
    }

    override fun onIsPlayingChanged(item: MediaInfo, isPlaying: Boolean) {
        playingStateChange(item.id, isPlaying)
    }


    private fun playingStateChange(id: String, isPlaying: Boolean) {
        val position = mAdapter.findStatusById(id)
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

    override fun bindPlaylist(type: Int): Boolean {
        val mediaList = PlayerHelper.convert2MediaInfo(mAdapter.getLogicData())
        return if (mAdapter.itemCount == 0 || mediaList.isEmpty()) {
            ToastUtil.showCenter(getString(R.string.tips_no_content))
            false
        } else {
            if (type == 0 && !PlayerHelper.instance.isCurrentPlaylist(getTimelinePageId())) {
                PlayerHelper.instance.setPlaylist(
                    mediaList,
                    getTimelinePageId(),
                    this
                )
            }
            true
        }
    }

}