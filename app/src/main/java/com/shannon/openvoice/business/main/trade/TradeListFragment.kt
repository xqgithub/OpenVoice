package com.shannon.openvoice.business.main.trade

import android.os.Bundle
import android.text.TextUtils
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.shannon.android.lib.base.activity.KBaseFragment
import com.shannon.android.lib.base.viewmodel.EmptyViewModel
import com.shannon.android.lib.extended.addMoreData
import com.shannon.android.lib.extended.bindEmptyView
import com.shannon.android.lib.extended.clearAnimations
import com.shannon.android.lib.extended.removeEmptyView
import com.shannon.android.lib.util.ToastUtil
import com.shannon.openvoice.R
import com.shannon.openvoice.business.player.MediaInfo
import com.shannon.openvoice.business.player.PlayerHelper
import com.shannon.openvoice.business.player.listener.OnPlaylistListener
import com.shannon.openvoice.business.timeline.TimelineViewModel
import com.shannon.openvoice.business.timeline.listener.RefreshableFragment
import com.shannon.openvoice.databinding.FragmentTradeListBinding
import com.shannon.openvoice.extended.startActivityIntercept
import com.shannon.openvoice.model.EventPurchasedModel
import com.shannon.openvoice.model.TradeList
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode

/**
 *
 * @Package:        com.shannon.openvoice.business.main.trade
 * @ClassName:      TradeListFragment
 * @Description:     作用描述
 * @Author:         czhen
 * @CreateDate:     2022/9/1 10:20
 */
class TradeListFragment : KBaseFragment<FragmentTradeListBinding, EmptyViewModel>(),
    RefreshableFragment, OnPlaylistListener {

    private val mViewModel by viewModels<TradeViewModel>({ requireParentFragment() })

    private lateinit var kind: Kind
    private lateinit var mAdapter: TradeListAdapter
    private var pageNum = 0

    companion object {
        private const val ARGS_KIND = "kind"

        fun newInstance(kind: Kind): TradeListFragment {
            return TradeListFragment().apply {
                arguments = Bundle().apply {
                    putString(ARGS_KIND, kind.name)
                }
            }
        }
    }

    override fun onInit() {
        EventBus.getDefault().register(this)
        bindViewModelEvent(mViewModel)
        kind = Kind.valueOf(arguments?.getString(ARGS_KIND)!!)
        mAdapter = TradeListAdapter(requireContext(), kind, this::onLike, this::onPlay)
        binding.run {
            recyclerView.layoutManager = LinearLayoutManager(requireContext())
            recyclerView.adapter = mAdapter
            recyclerView.clearAnimations()
            recyclerView.setOnRefreshListener { refresh(false) }
            recyclerView.setOnLoadMoreListener {
                pageNum++
                mViewModel.getTradeList(
                    pageNum,
                    type = kind.num,
                    onResult = this@TradeListFragment::loadMore
                )
            }
        }
        mViewModel.timeSelectedPosition.observe(viewLifecycleOwner) {
            if (isInitialized()) {
                refresh(true)
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

    private fun refresh(showLoading: Boolean) {
        pageNum = 0
        mViewModel.getTradeList(
            showLoading = showLoading,
            type = kind.num,
            onResult = this@TradeListFragment::loadData
        )
    }

    private fun loadData(data: List<TradeList>) {
        if (data.isEmpty()) {
            binding.recyclerView.bindEmptyView(
                R.drawable.ic_development_model_offical, R.string.desc_wilderness,
                height = ViewGroup.LayoutParams.MATCH_PARENT
            )
        } else {
            binding.recyclerView.removeEmptyView()
        }
        mAdapter.setNewData(data)
        PlayerHelper.instance.refreshed(
            null,
            generatePageId(PlayerHelper.instance.decodeParentId()),
            this
        )
    }

    private fun loadMore(data: List<TradeList>) {
        mAdapter.addMoreData(data)
    }

    private fun onLike(position: Int) {
        requireContext().startActivityIntercept {
            val item = mAdapter.getItemData(position)
            if (item.voiceModel.isLiked) {
                mViewModel.unlikeVoiceModel(item.voiceModel.id) {
                    val model = item.voiceModel
                    mAdapter.data[position] =
                        item.copy(
                            voiceModel = item.voiceModel.copy(
                                isLiked = false,
                                likeCount = (model.likeCount - 1)
                            )
                        )
                    mAdapter.refreshNotifyItemChanged(position)
                }
            } else {
                mViewModel.likeVoiceModel(item.voiceModel.id) {
                    val model = item.voiceModel
                    mAdapter.data[position] =
                        item.copy(
                            voiceModel = item.voiceModel.copy(
                                isLiked = true,
                                likeCount = (model.likeCount + 1)
                            )
                        )
                    mAdapter.refreshNotifyItemChanged(position)
                }
            }
        }
    }

    private fun onPlay(position: Int) {
        val item = mAdapter.getItemData(position)
        if (item.isPlaying) {
            PlayerHelper.instance.onPause()
        } else {
            //如果数据为空 或者没有主动暂停 就重新拉取数据播放
            if (item.getPlaylist().isEmpty()) {
                val auditionFile = item.voiceModel.auditionFile ?: ""
                if (item.voiceModel.isOfficial && !TextUtils.isEmpty(auditionFile)) {
                    val media = MediaInfo(
                        id = auditionFile,
                        resUrl = auditionFile,
                        "https://oss.openvoiceover.com/avatars/logo.png"
                    )

                    val dataList = arrayListOf(media)
                    mAdapter.data[position] = item.copy(playlist = dataList)
                    play(position)
                } else {
                    mViewModel.modelStatus(item.id.toString(), item.voiceModel.id, null) {
                        if(it.isEmpty()){
                            ToastUtil.showCenter(getString(R.string.tips_noaudition))
                            return@modelStatus
                        }
                        mAdapter.data[position] = item.copy(playlist = it)
                        play(position)
                    }
                }
            } else {
                play(position)
            }
        }
    }

    private fun play(position: Int) {
        val item = mAdapter.getItemData(position)
        if (!PlayerHelper.instance.isCurrentPlaylist(generatePageId(item.id.toString()))) {
            PlayerHelper.instance.setPlaylist(
                item.getPlaylist(),
                generatePageId(item.id.toString()),
                this
            )
        }
        PlayerHelper.instance.onPlay(item.id.toString())//2564_110024873336751211
    }

    private fun getPlayerKind() =
        if (kind == Kind.SELL) TimelineViewModel.Kind.MODEL_SELL else TimelineViewModel.Kind.MODEL_USED

    private fun generatePageId(id: String): String {
        return PlayerHelper.generatePageId(getPlayerKind(), id)
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun subscribePurchasedModel(event: EventPurchasedModel) {
        val position = mAdapter.data.indexOfFirst { it.voiceModel.id == event.modelId }
        if (position != -1) {
            val item = mAdapter.data[position]
            mAdapter.data[position] =
                item.copy(voiceModel = item.voiceModel.copy(isModelOwner = true))
            mAdapter.refreshNotifyItemChanged(position)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        EventBus.getDefault().unregister(this)
    }

    enum class Kind(val num: Int) {
        SELL(0),
        USE(1)
    }

    override fun onIsPlayingChanged(item: MediaInfo, isPlaying: Boolean) {
        mAdapter.playingStateChange(PlayerHelper.instance.decodeParentId(), isPlaying)
    }

    override fun onPlaylistDestroy(item: MediaInfo) {
        onIsPlayingChanged(item, false)
    }

    override fun navigateTo(item: MediaInfo) {
    }

}