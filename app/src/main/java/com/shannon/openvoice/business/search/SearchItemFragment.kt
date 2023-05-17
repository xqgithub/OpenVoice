package com.shannon.openvoice.business.search

import android.os.Bundle
import android.text.TextUtils
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.blankj.utilcode.util.LogUtils
import com.shannon.android.lib.base.activity.KBaseFragment
import com.shannon.android.lib.extended.*
import com.shannon.android.lib.util.ThemeUtil
import com.shannon.android.lib.util.ToastUtil
import com.shannon.openvoice.R
import com.shannon.openvoice.business.creation.detail.ModelDetailActivity
import com.shannon.openvoice.business.main.mine.account.AccountActivity
import com.shannon.openvoice.business.main.trade.TradeViewModel
import com.shannon.openvoice.business.pay.PaymentActivity
import com.shannon.openvoice.business.player.MediaInfo
import com.shannon.openvoice.business.player.PlayerHelper
import com.shannon.openvoice.business.player.listener.OnPlaylistListener
import com.shannon.openvoice.business.search.SearchActivity.Companion.searchContent
import com.shannon.openvoice.business.status.StatusListActivity
import com.shannon.openvoice.business.timeline.StatusActionImpl
import com.shannon.openvoice.business.timeline.TimelineViewModel
import com.shannon.openvoice.business.timeline.listener.FragmentListener
import com.shannon.openvoice.business.timeline.listener.StatusActionListener
import com.shannon.openvoice.components.ErrorPageView
import com.shannon.openvoice.databinding.FragmentSearchItemBinding
import com.shannon.openvoice.extended.startActivityIntercept
import com.shannon.openvoice.model.EventComposeStatus
import com.shannon.openvoice.model.EventPurchasedModel
import com.shannon.openvoice.model.StatusModel
import com.shannon.openvoice.model.TradeList
import com.shannon.openvoice.util.SmoothTopScroller
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import timber.log.Timber

/**
 * Date:2022/8/29
 * Time:14:07
 * author:dimple
 * 搜索账号、话题、模型 页面
 */
class SearchItemFragment : KBaseFragment<FragmentSearchItemBinding, SearchModel>(),
    StatusActionListener by StatusActionImpl(), FragmentListener, OnPlaylistListener {

    private var pageNum: Int = 0

    private var searchContent = ""

    private var searchTypeValue: String = SearchActivity.postes

    private val timelineViewModel: TimelineViewModel by viewModels()

    //    private val mViewModel by viewModels<TradeViewModel>({ requireParentFragment() })
    private val mViewModel: TradeViewModel by viewModels()


    //用户搜索适配器
    private val searchAccountsAdapter: SearchAccountsAdapter by lazy {
        SearchAccountsAdapter(requireContext())
    }

    //话题搜索适配器
    private val searchTopicsAdapter: SearchTopicsAdapter by lazy {
        SearchTopicsAdapter(requireContext(), this)
    }

    //模型搜索 适配器
    private val searchModelsAdapter: SearchModelsAdapter by lazy {
//        TradeListAdapter(TradeListFragment.Kind.SELL, this::onBuy, this::onPlay1)
        SearchModelsAdapter(requireContext(), this, this::onPlay1)
    }

    //声文搜索 适配器
    private val searchPostesAdapter by lazy {
//        TimelineAdapter(requireContext(), this)
        SearchVoicesAdapter(requireContext(), this)
    }
    private lateinit var mLayoutManager: LinearLayoutManager


    companion object {
        fun newInstance(searchTypeValue: String): SearchItemFragment {
            val searchItemFragment = SearchItemFragment()
            searchItemFragment.arguments = Bundle().apply {
                putString(searchContent, searchTypeValue)
            }
            return searchItemFragment
        }
    }

    override fun onInit() {
        searchTypeValue = arguments?.getString(SearchActivity.searchContent).toString()
        EventBus.getDefault().register(this)
        timelineViewModel.init(
            viewLifecycleOwner,
            TimelineViewModel.Kind.SEARCH,
            dataAdapter = when (searchTypeValue) {
                SearchActivity.postes -> searchPostesAdapter
                SearchActivity.topics -> searchTopicsAdapter
                SearchActivity.model -> searchModelsAdapter
                else -> searchTopicsAdapter
            },
            arrayListOf(),
            null,
            0L
        )

        initAccountsRecyclerView()
        dataEcho()
    }


    private fun timelineKind(): TimelineViewModel.Kind {
        return when (searchTypeValue) {
            SearchActivity.postes -> TimelineViewModel.Kind.SEARCH_VOICEOVER
            SearchActivity.topics -> TimelineViewModel.Kind.SEARCH_TOPIC_VOICEOVER
            SearchActivity.model -> TimelineViewModel.Kind.SEARCH_MODEL_VOICEOVER
            else -> TimelineViewModel.Kind.SEARCH_VOICEOVER
        }
    }

    /**
     * 初始化用户列表数据
     */
    private fun initAccountsRecyclerView() {
        binding.apply {
            recyclerView.apply {
                mLayoutManager = LinearLayoutManager(requireContext())
                layoutManager = mLayoutManager
                when (searchTypeValue) {
                    SearchActivity.account -> {
                        adapter = searchAccountsAdapter
                    }
                    SearchActivity.topics -> {
                        adapter = searchTopicsAdapter
                    }
                    SearchActivity.model -> {
                        adapter = searchModelsAdapter
                    }
                    SearchActivity.postes -> {
                        adapter = searchPostesAdapter
                    }
                }

                recyclerView.itemDivider(
                    colorRes = R.color.transparent,
                    size = 14.dp,
                    headerNoShowSize = 1,
                    footerNoShowSize = 0
                )

                clearAnimation()
//                setLoadingMoreBottomHeight(100f)

                setOnItemChildClickListener { view, position ->
                    when (view.id) {
                        R.id.cl_main -> {
                            when (searchTypeValue) {
                                SearchActivity.account -> {
                                    val bean = searchAccountsAdapter.getItemData(position)
                                    startActivity(
                                        AccountActivity.newIntent(
                                            requireContext(),
                                            bean.id
                                        )
                                    )
                                }
                                SearchActivity.topics -> {
                                    val bean = searchTopicsAdapter.getItemData(position)
                                    startActivity(
                                        StatusListActivity.newHashtagIntent(
                                            requireContext(),
                                            bean.name
                                        )
                                    )
                                }
                                SearchActivity.model -> {
                                    val bean = searchModelsAdapter.getItemData(position)
                                    startActivity(ModelDetailActivity.newIntent(context, bean.id))
                                }
                                else -> searchAccountsAdapter.getItemData(position)
                            }
                        }
                        R.id.sbFocusOnOperation -> {
                            val bean = searchAccountsAdapter.getItemData(position)
                            if (bean.following) {
                                requireContext().showConfirmDialog(
                                    message = getString(R.string.desc_sure_unfollow)
                                ) {
                                    viewModel.unfollowAccount(bean.id, this@SearchItemFragment) {
                                        searchAccountsAdapter.apply {
                                            getItemData(position).following = false
                                            refreshNotifyItemChanged(position)
                                        }
                                    }
                                }
                            } else {
                                viewModel.followAccount(
                                    bean.id,
                                    owner = this@SearchItemFragment
                                ) {
                                    searchAccountsAdapter.apply {
                                        getItemData(position).following = true
                                        refreshNotifyItemChanged(position)
                                    }
                                }
                            }
                        }
                        R.id.sivAvatar, R.id.tvUserNameNickName, R.id.tvForwarderUsername -> {
                            var bean: StatusModel? = null
                            when (searchTypeValue) {
                                SearchActivity.topics -> {
                                    bean = searchTopicsAdapter.getLogicData()[position]
                                }
                                SearchActivity.postes -> {
                                    bean = searchPostesAdapter.getLogicData()[position]
                                }
                                SearchActivity.model -> {
                                    bean = searchModelsAdapter.getLogicData()[position]
                                }
                            }
                            startActivity(
                                AccountActivity.newIntent(
                                    requireContext(),
                                    bean!!.account.id
                                )
                            )
                        }
                        R.id.status_trade_layout -> {
                            when (searchTypeValue) {
                                SearchActivity.topics -> {
                                    searchTopicsAdapter.getLogicData()[position].apply {
                                        startActivity(
                                            ModelDetailActivity.newIntent(
                                                context,
                                                this.voiceModel!!.id
                                            )
                                        )
                                    }
                                }
                                SearchActivity.postes -> {
                                    searchPostesAdapter.getLogicData()[position].apply {
                                        startActivity(
                                            ModelDetailActivity.newIntent(
                                                context,
                                                this.voiceModel!!.id
                                            )
                                        )
                                    }

                                }
                                SearchActivity.model -> {
                                    searchModelsAdapter.getLogicData()[position].apply {
                                        startActivity(
                                            ModelDetailActivity.newIntent(
                                                context,
                                                this.voiceModel!!.id
                                            )
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                setOnRefreshListener {
                    pageNum = 0
                    viewModel.searchObservable(
                        searchContent,
                        searchTypeValue,
                        20,
                        pageNum,
                        requireActivity()
                    )
                }


                setOnLoadMoreListener {
                    pageNum += 20
                    viewModel.searchObservable(
                        searchContent,
                        searchTypeValue,
                        20,
                        pageNum,
                        requireActivity()
                    )
                    LogUtils.i("pageNum =-= ${pageNum}")
                }
            }
        }
    }

    /**
     * 刷新用户数据
     */
    fun refreshData(searchContent: String) {
        this.searchContent = searchContent

        pageNum = 0
        viewModel.searchObservable(
            searchContent,
            searchTypeValue,
            20,
            pageNum,
            requireActivity()
        )
    }

    override fun onLazyInit() {
        if (searchTypeValue == SearchActivity.topics) {
            pageNum = 0
            viewModel.searchObservable(
                searchContent,
                searchTypeValue,
                20,
                pageNum,
                requireActivity()
            )
        }
        bindHost(
            timelineKind = timelineKind(),
            this,
            timelineViewModel,
            adapter = when (searchTypeValue) {
                SearchActivity.postes -> searchPostesAdapter
                SearchActivity.topics -> searchTopicsAdapter
                SearchActivity.model -> searchModelsAdapter
                else -> searchTopicsAdapter
            }
        )
    }


    /**
     * 数据回显
     */
    private fun dataEcho() {
        //搜索用户数据
        viewModel.accountsLive.observe(requireActivity()) { beans ->
            if (pageNum == 0) {
                if (beans.isEmpty()) {
                    bindEmptyView()
                } else {
                    binding.recyclerView.removeEmptyView()
                    searchAccountsAdapter.setNewData(beans)
                    binding.recyclerView.isLoadMoreEnabled = true
                }
            } else {
                searchAccountsAdapter.addMoreData(beans)
            }
        }
        //推荐用户
        viewModel.recommendAccountsLive.observe(requireActivity()) { beans ->
            if (beans.isEmpty()) {
                bindEmptyView()
            } else {
                binding.recyclerView.removeEmptyView()
                searchAccountsAdapter.setNewData(beans)
                binding.recyclerView.isLoadMoreEnabled = false
            }
        }

        //搜索话题数据
        viewModel.topicsLive.observe(requireActivity()) { beans ->
            if (pageNum == 0) {
                if (beans.isEmpty()) {
                    bindEmptyView()
                } else {
                    binding.recyclerView.removeEmptyView()
                    searchTopicsAdapter.apply {
                        setStatuses(mutableListOf(), false)
                        setNewData(beans)
                        binding.recyclerView.isLoadMoreEnabled = true
                    }
                }
            } else {
                searchTopicsAdapter.addMoreData(beans)
            }
        }
        //推荐话题数据
        viewModel.recommendTopicsLive.observe(requireActivity()) { bean ->
            if (bean.hashtags.isEmpty()) {
                bindEmptyView()
            } else {
                binding.recyclerView.removeEmptyView()
                searchTopicsAdapter.apply {
                    setStatuses(bean.statuses, true)
                    setNewData(bean.hashtags)
                    binding.recyclerView.isLoadMoreEnabled = false
                }
                PlayerHelper.instance.refreshed(
                    PlayerHelper.convert2MediaInfo(bean.statuses),
                    getTimelinePageId(),
                    this
                )
            }
        }

        //搜索模型数据
        viewModel.voiceModelsLive.observe(requireActivity()) { beans ->
            binding.recyclerView.isLoadMoreEnabled = true
            if (pageNum == 0) {
                if (beans.isEmpty()) {
                    bindEmptyView()
                } else {
                    binding.recyclerView.removeEmptyView()
                    val tradeList = mutableListOf<TradeList>()
                    beans.forEach { bean ->
                        tradeList.add(
                            TradeList(
                                bean.id, StatusModel.VoiceModel(
                                    bean.id,
                                    bean.name,
                                    bean.account,
                                    bean.status,
                                    bean.isOfficial,
                                    bean.isLiked,
                                    bean.activated,
                                    bean.isModelOwner,
                                    bean.price,
                                    bean.currency,
                                    bean.payload,
                                    bean.auditionFile,
                                    bean.likeCount.toLong(),
                                    bean.usageCount.toLong(),
                                    bean.tradingCount
                                ), bean.tradingCount, false, arrayListOf(), "", 0
                            )
                        )
                    }
                    searchModelsAdapter.setStatuses(mutableListOf(), false)
                    searchModelsAdapter.setNewData(tradeList)
                    PlayerHelper.instance.refreshed(
                        null,
                        generatePageId(PlayerHelper.instance.decodeParentId()),
                        this
                    )
                }
            } else {
                val tradeList = mutableListOf<TradeList>()
                beans.forEach { bean ->
                    tradeList.add(
                        TradeList(
                            bean.id, StatusModel.VoiceModel(
                                bean.id,
                                bean.name,
                                bean.account,
                                bean.status,
                                bean.isOfficial,
                                bean.isLiked,
                                bean.activated,
                                bean.isModelOwner,
                                bean.price,
                                bean.currency,
                                bean.payload,
                                bean.auditionFile,
                                bean.likeCount.toLong(),
                                bean.usageCount.toLong(),
                                bean.tradingCount
                            ), bean.tradingCount, false, arrayListOf(), "", 0
                        )
                    )
                }
                searchModelsAdapter.addMoreData(tradeList)
            }
        }

        //推荐模型数据
        viewModel.recommendVoiceModelsLive.observe(requireActivity()) { bean ->
            if (bean.voiceModels.isEmpty()) {
                bindEmptyView()
            } else {
                binding.recyclerView.removeEmptyView()
                val tradeList = mutableListOf<TradeList>()
                bean.voiceModels.forEach { _beans ->
                    tradeList.add(
                        TradeList(
                            _beans.id, StatusModel.VoiceModel(
                                _beans.id,
                                _beans.name,
                                _beans.account,
                                _beans.status,
                                _beans.isOfficial,
                                _beans.isLiked,
                                _beans.activated,
                                _beans.isModelOwner,
                                _beans.price,
                                _beans.currency,
                                _beans.payload,
                                _beans.auditionFile,
                                _beans.likeCount.toLong(),
                                _beans.usageCount.toLong(),
                                _beans.tradingCount
                            ), _beans.tradingCount, false, arrayListOf(), "", 0
                        )
                    )
                }
                searchModelsAdapter.setStatuses(bean.statuses, true)
                searchModelsAdapter.setNewData(tradeList)
                binding.recyclerView.isLoadMoreEnabled = false
                PlayerHelper.instance.refreshed(
                    PlayerHelper.convert2MediaInfo(bean.statuses),
                    getTimelinePageId(),
                    this
                )
                PlayerHelper.instance.refreshed(
                    null,
                    generatePageId(PlayerHelper.instance.decodeParentId()),
                    this
                )
            }

        }


        //搜索推文和推荐推文
        viewModel.postesLive.observe(requireActivity()) { bean ->
            val beans = bean.statuses
            if (pageNum == 0) {
                if (beans.isEmpty()) {
                    bindEmptyView()
                } else {
                    binding.recyclerView.removeEmptyView()
                    val temporaryStatusModels = mutableListOf<StatusModel>()
                    beans.forEach { statusModel ->
                        temporaryStatusModels.add(statusModel.copy(playingButtonDisappears = false))
                    }
//                    searchPostesAdapter.setNewData(temporaryStatusModels)
                    searchPostesAdapter.setNewDataIgnoreSize(temporaryStatusModels)
                    timelineViewModel.findNonGenerated(temporaryStatusModels)
                    timelineViewModel.scheduleTimer()
                }
                PlayerHelper.instance.refreshed(
                    PlayerHelper.convert2MediaInfo(bean.statuses),
                    getTimelinePageId(),
                    this
                )
            } else {
                val temporaryStatusModels = mutableListOf<StatusModel>()
                beans.forEach { statusModel ->
                    temporaryStatusModels.add(statusModel.copy(playingButtonDisappears = false))
                }
//                searchPostesAdapter.addMoreData(temporaryStatusModels)
                searchPostesAdapter.addData(temporaryStatusModels)
                binding.recyclerView.loadMoreComplete()
                timelineViewModel.findNonGenerated(temporaryStatusModels)
//                LogUtils.i("searchPostesAdapter.data.size =-= ${searchPostesAdapter.data.size}")
            }
            searchPostesAdapter.setRecommendedState(bean.isRecommended)
            binding.recyclerView.isLoadMoreEnabled = !bean.isRecommended

        }
    }

    /**
     * 购买
     */
    private fun onBuy(position: Int) {
        requireContext().startActivityIntercept {
            val item = searchModelsAdapter.getItemData(position)
            mViewModel.modelDetail(item.voiceModel.id) {
                if (it.isModelOwner) {
                    ToastUtil.showCenter(getString(R.string.tips_owned_model))
                } else {
                    requireContext().startActivity(
                        PaymentActivity.newIntent(
                            requireContext(),
                            item.voiceModel.id
                        )
                    )
                }
            }
        }
    }

    /**
     * 模型搜索播放
     */
    private fun onPlay1(position: Int, playType: String) {
        val item = searchModelsAdapter.getItemData(position)
        if (item.isPlaying) {
            PlayerHelper.instance.onPause()
        } else {
            //如果数据为空  就重新拉取数据播放
            if (item.getPlaylist().isEmpty()) {
                val auditionFile = item.voiceModel.auditionFile ?: ""
                if (item.voiceModel.isOfficial && !TextUtils.isEmpty(auditionFile)) {
                    val media = MediaInfo(
                        id = auditionFile,
                        resUrl = auditionFile,
                        "https://oss.openvoiceover.com/avatars/logo.png"
                    )
                    val dataList = arrayListOf(media)
                    searchModelsAdapter.data[position] = item.copy(playlist = dataList)
                    playModel(position)
                } else {
                    mViewModel.modelStatus(item.id.toString(), item.voiceModel.id, null) {
                        if (it.isEmpty()) {
                            ToastUtil.showCenter(getString(R.string.tips_noaudition))
                            return@modelStatus
                        }
                        searchModelsAdapter.data[position] = item.copy(playlist = it)
                        playModel(position)
                    }
                }
            } else {
                playModel(position)
            }
        }
    }

    private fun playModel(position: Int) {
        val item = searchModelsAdapter.getItemData(position)
        if (!PlayerHelper.instance.isCurrentPlaylist(generatePageId(item.id.toString()))) {
            PlayerHelper.instance.setPlaylist(
                item.getPlaylist(),
                generatePageId(item.id.toString()),
                this
            )
        }
        PlayerHelper.instance.onPlay(item.id.toString())
    }

    private fun generatePageId(id: String): String {
        return PlayerHelper.generatePageId(getPlayerKind(), id, searchContent)
    }

    private fun getPlayerKind(): TimelineViewModel.Kind {
        return when (searchTypeValue) {
            SearchActivity.topics -> TimelineViewModel.Kind.SEARCH_TOPIC
            SearchActivity.model -> TimelineViewModel.Kind.SEARCH_MODEL
            else -> TimelineViewModel.Kind.SEARCH_MODEL
        }
    }


    /**
     * 当列表数据为空的时候，显示
     */
    private fun bindEmptyView() {
        val errorPageView = ErrorPageView(requireContext())
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
                        requireContext(),
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
            isLoadMoreEnabled = false
            when (searchTypeValue) {
                SearchActivity.account -> searchAccountsAdapter.setNewData(arrayListOf())
                SearchActivity.topics -> searchTopicsAdapter.setNewData(arrayListOf())
                SearchActivity.model -> searchModelsAdapter.setNewData(arrayListOf())
                SearchActivity.postes -> searchPostesAdapter.setNewData(arrayListOf())
                else -> searchAccountsAdapter.setNewData(arrayListOf())
            }
        }
    }

    override fun requireAct() = requireActivity()

    override fun deleted() {

    }

    override fun getTimelinePageId(): String {
        return PlayerHelper.generatePageId(timelineKind(), searchCondition = searchContent)
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun subscribePurchasedModel(event: EventPurchasedModel) {
        if (searchTypeValue == SearchActivity.model) {
            val position =
                searchModelsAdapter.data.indexOfFirst { it.voiceModel.id == event.modelId }
            if (position != -1) {
                val model = searchModelsAdapter.data[position].voiceModel
                searchModelsAdapter.data[position] =
                    searchModelsAdapter.data[position].copy(voiceModel = model.copy(isModelOwner = true))
                searchModelsAdapter.refreshNotifyItemChanged(position)
            }
        }
    }


    @Subscribe(threadMode = ThreadMode.MAIN)
    fun subscribeStatus(event: EventComposeStatus) {
        if (event.type == EventComposeStatus.Type.REBLOG) {
            if (event.type == EventComposeStatus.Type.FAVOURITES) {
                if (searchTypeValue == SearchActivity.postes) {
                    searchPostesAdapter.onFavourite(event.status)
                } else if (searchTypeValue == SearchActivity.topics) {
                    searchTopicsAdapter.onFavourite(event.status)
                } else if (searchTypeValue == SearchActivity.model) {
                    searchModelsAdapter.onFavourite(event.status)
                }
            }
        }
    }


    override fun onDestroy() {
        super.onDestroy()
        EventBus.getDefault().unregister(this)
    }

    override fun onIsPlayingChanged(item: MediaInfo, isPlaying: Boolean) {
        observePlayingState(item.id, isPlaying)
    }

    private fun observePlayingState(id: String, isPlaying: Boolean) {
        if (searchTypeValue == SearchActivity.postes) {
            val position = searchPostesAdapter.findStatusById(id)
            if (position == -1) return
            Timber.e("playingState = $position ; $isPlaying")
            searchPostesAdapter.playingStateChange(isPlaying, position)
        } else if (searchTypeValue == SearchActivity.model) {
            if (PlayerHelper.instance.isCurrentPlaylist(getTimelinePageId())) {//如果包含
                val position = searchModelsAdapter.findStatusById(id)
                if (position == -1) return
                Timber.e("playingState = $position ; $isPlaying")
                searchModelsAdapter.playingStateChange(isPlaying, position)
            } else {
                searchModelsAdapter.playingStateChangeToModel(
                    PlayerHelper.instance.decodeParentId(),
                    isPlaying
                )
            }
        } else if (searchTypeValue == SearchActivity.topics) {
            val position = searchTopicsAdapter.findStatusById(id)
            if (position == -1) return
            Timber.e("playingState = $position ; $isPlaying")
            searchTopicsAdapter.playingStateChange(isPlaying, position)
        }
    }

    override fun onPlaylistDestroy(item: MediaInfo) {
        onIsPlayingChanged(item, false)
    }

    override fun navigateTo(item: MediaInfo) {
        if (searchTypeValue == SearchActivity.postes) {
            observerScrollable(item.id)
        }
    }

    private fun observerScrollable(mediaId: String) {
        val position = searchPostesAdapter.findStatusById(mediaId)
        if (position == -1 || mLayoutManager.isSmoothScrolling) return
        val smoothScroller = SmoothTopScroller(requireContext())
        Timber.e("observerScrollable = $position ")

        smoothScroller.targetPosition = position + binding.recyclerView.customTopItemViewCount
        if (!mLayoutManager.isSmoothScrolling)
            mLayoutManager.startSmoothScroll(smoothScroller)
    }
}