package com.shannon.openvoice.business.creation

import android.os.Bundle
import android.text.TextUtils
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import com.shannon.android.lib.base.activity.KBaseFragment
import com.shannon.android.lib.extended.*
import com.shannon.android.lib.util.ToastUtil
import com.shannon.openvoice.R
import com.shannon.openvoice.business.creation.models.OfficialModelListAdapter
import com.shannon.openvoice.business.pay.SellActivity
import com.shannon.openvoice.business.player.MediaInfo
import com.shannon.openvoice.business.player.PlayerHelper
import com.shannon.openvoice.business.player.listener.OnPlaylistListener
import com.shannon.openvoice.business.timeline.TimelineViewModel
import com.shannon.openvoice.business.timeline.listener.RefreshableFragment
import com.shannon.openvoice.databinding.FragmentVoiceModelListBinding
import com.shannon.openvoice.event.UniversalEvent
import com.shannon.openvoice.model.*
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import timber.log.Timber

/**
 *
 * @ProjectName:    OpenVoice
 * @Package:        com.shannon.openvoice.business.creation
 * @ClassName:      ModelListFragment
 * @Description:     声音模型列表
 * @Author:         czhen
 * @CreateDate:     2022/7/25 16:50
 */
class ModelListFragment : KBaseFragment<FragmentVoiceModelListBinding, CreationViewModel>(),
    RefreshableFragment, OnPlaylistListener {
    //    private val adapter by lazy { ModelListAdapter(true, this::onActivation) }
    private lateinit var adapter: ModelListAdapter

    //    private val officialModelListAdapter by lazy { OfficialModelListAdapter(requireActivity()) }
    private lateinit var officialModelListAdapter: OfficialModelListAdapter

    private var pageNum = 0

    private var modelListType = MODELLIST_NORMAL

    //官方模型标签id
    private var categoryId = -1

    companion object {
        const val MODELLISTTYPE = "modellisttype"

        const val CATEGORYID = "categoryId"

        //模型模式-正常
        const val MODELLIST_NORMAL = "modellist_normal"

        //模型模式-设置模型
        const val MODELLIST_SETUP = "modellist_setup"

        //模型模式-模型修改-官方
        const val MODELLIST_MODIFY_OFFICAL = "modellist_modify_offical"

        //模型模式-模型修改-我的模型
        const val MODELLIST_MODIFY_MYMODEL = "modellist_modify_mymodel"


        fun newInstance(modelListType: String, categoryId: Int = -1): ModelListFragment {
            val modelListFragment = ModelListFragment()
            modelListFragment.arguments = Bundle().apply {
                putString(MODELLISTTYPE, modelListType)
                putInt(CATEGORYID, categoryId)
            }
            return modelListFragment
        }
    }

    override fun onInit() {
        EventBus.getDefault().register(this)

        arguments?.getString(MODELLISTTYPE)?.let {
            modelListType = it
        }
        arguments?.getInt(CATEGORYID)?.let {
            categoryId = it
        }

        binding.run {
            recyclerView.layoutManager = LinearLayoutManager(requireContext())
            if (modelListType == MODELLIST_SETUP || modelListType == MODELLIST_MODIFY_OFFICAL) {
                officialModelListAdapter =
                    OfficialModelListAdapter(modelListType == MODELLIST_SETUP, requireActivity())
                recyclerView.adapter = officialModelListAdapter
                officialModelListAdapter.setOnActivation(this@ModelListFragment::onActivationOffical)
                officialModelListAdapter.setOnPlay(this@ModelListFragment::onPlayOfficial)
                recyclerView.clearAnimations()

                recyclerView.setOnRefreshListener {
                    pageNum = 0
                    refreshOffcialModels()
                }
                recyclerView.setOnLoadMoreListener {
                    pageNum++
                    viewModel.voiceModelsOfficialList(pageNum, categoryId) {
                        officialModelListAdapter.addMoreData(it)
                    }
                }
            } else {
                if (modelListType == MODELLIST_NORMAL) {
                    adapter = ModelListAdapter(
                        true,
                        this@ModelListFragment::onRegistration,
                        this@ModelListFragment::onActivation
                    )
                    adapter.setOnDeleteAction(this@ModelListFragment::onDelete)
                    adapter.setOnPlayAction(this@ModelListFragment::onPlay)
                } else {
                    adapter = ModelListAdapter(
                        false,
                        this@ModelListFragment::onRegistration,
                        this@ModelListFragment::onActivation
                    )
                }
                recyclerView.adapter = adapter
                recyclerView.clearAnimations()
                recyclerView.setOnRefreshListener { refresh() }
                recyclerView.setOnLoadMoreListener {
                    pageNum++
                    val status = if (modelListType == MODELLIST_MODIFY_MYMODEL) "unused" else ""
                    viewModel.fetchVoiceModels(pageNum, adapter.itemCount, status) {
                        adapter.addMoreData(it)
                    }
                }
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
        if (modelListType == MODELLIST_NORMAL) {
            viewModel.fetchVoiceModels(pageNum) { beans ->
                bindEmptyView(beans.isEmpty())
                adapter.setNewData(beans)
                refreshedPlayStatus()
            }
        } else if (modelListType == MODELLIST_MODIFY_MYMODEL) {
            viewModel.fetchVoiceModels(pageNum = pageNum, status = "unused") { beans ->
                bindEmptyView(beans.isEmpty())
                adapter.setNewData(beans)
            }
        }
    }

    override fun onResume() {
        super.onResume()
        refreshedPlayStatus()
    }

    private fun refreshedPlayStatus() {
        PlayerHelper.instance.refreshed(
            null,
            generatePageId(
                if (modelListType == MODELLIST_NORMAL) TimelineViewModel.Kind.MODEL_AUDITION else TimelineViewModel.Kind.MODEL_AUDITION_OFFICIAL,
                PlayerHelper.instance.decodeParentId()
            ),
            this
        )
    }


    /**
     * 刷新官方模型数据
     */
    private fun refreshOffcialModels() {
        viewModel.voiceModelsOfficialList(pageNum, categoryId) { beans ->
            bindEmptyView(beans.isEmpty())
            officialModelListAdapter.setNewData(beans)
            refreshedPlayStatus()
        }
    }

    private fun bindEmptyView(isEmpty: Boolean) {
        if (isEmpty) {
            binding.recyclerView.bindEmptyView(
                getEmptyHintDrawable(), R.string.desc_wilderness,
                height = ViewGroup.LayoutParams.MATCH_PARENT
            )
        } else {
            binding.recyclerView.removeEmptyView()
        }
    }

    private fun getEmptyHintDrawable(): Int {
        return when (modelListType) {
            MODELLIST_NORMAL -> R.drawable.ic_development_model_list
            MODELLIST_MODIFY_OFFICAL -> R.drawable.ic_development_model_offical
            else -> R.drawable.ic_development
        }
    }

    private fun onRegistration(position: Int) {
        if (modelListType == MODELLIST_NORMAL) {
            val bean = adapter.getItemData(position)
            if (bean.payload.isNullOrEmpty()) {
                startActivity(SellActivity.newIntent(requireContext(), bean.id))
            } else {
                requireContext().showConfirmDialog(
                    message = getString(R.string.unsell_model),
                    doConfirm = {
                        viewModel.updateVoiceModel(bean.id, payload = "", saleCycle = "") {
                            adapter.data[position] = bean.copy(payload = "")
                            adapter.refreshNotifyItemChanged(position)
                        }
                    }
                )
            }
        }
    }


    private fun onActivation(position: Int) {
        if (modelListType == MODELLIST_NORMAL) {
            val bean = adapter.getItemData(position)
            viewModel.activateVoiceModel(bean.id) {
                if (it.isModelOwner || it.isOfficial) {
                    EventBus.getDefault().post(EventActivateVoice(bean.id, hashCode()))
                    adapter.setActivatedPosition(position)
                } else {
                    adapter.removeData(position)
                }
            }
        } else if (modelListType == MODELLIST_MODIFY_MYMODEL) {
            EventBus.getDefault()
                .post(
                    UniversalEvent(
                        UniversalEvent.modifySelectedSoundModel,
                        VoiceModelSelected(position, adapter.data)
                    )
                )
        }
    }

    /**
     * 官方模型列表激活使用
     */
    private fun onActivationOffical(position: Int) {
        val bean = officialModelListAdapter.getItemData(position)
        if (modelListType == MODELLIST_SETUP) {
            viewModel.activateVoiceModel(bean.id) {
                if (it.isOfficial) {
                    EventBus.getDefault().post(EventActivateVoice(bean.id, hashCode()))
                    officialModelListAdapter.setActivatedPosition(position)
                    EventBus.getDefault()
                        .post(UniversalEvent(UniversalEvent.officialModelTopData, it))
                }
            }
        } else if (modelListType == MODELLIST_MODIFY_OFFICAL) {
            EventBus.getDefault()
                .post(
                    UniversalEvent(
                        UniversalEvent.modifySelectedSoundModel,
                        VoiceModelSelected(position, officialModelListAdapter.data)
                    )
                )
        }
    }

    private fun onDelete(position: Int) {
        requireContext().showConfirmDialog(message = getString(R.string.sure_delete_model)) {
            val bean = adapter.getItemData(position)
            viewModel.deleteVoiceModel(bean.id) {
                if (bean.isPlaying == true) {
                    PlayerHelper.instance.releaseMusic()
                }
                EventBus.getDefault().post(EventDeleteModel(bean.id))
            }
        }
    }

    /**
     * 播放列表中声音文件
     */
    private fun onPlay(position: Int) {
        val item = adapter.getItemData(position)
        if (item.isPlaying == true) {
            PlayerHelper.instance.onPause()
        } else {
            //如果数据为空就重新拉取数据播放
            if (item.getPlaylist().isEmpty()) {
                viewModel.modelStatus(item.id, null) {
                    if (it.isEmpty()) {
                        ToastUtil.showCenter(getString(R.string.tips_noaudition))
                        return@modelStatus
                    }
                    adapter.data[position] = item.copy(playlist = it)
                    playModel(position)
                }
            } else {
                playModel(position)
            }
        }
    }

    private fun playModel(position: Int) {
        val item = adapter.getItemData(position)
        if (!PlayerHelper.instance.isCurrentPlaylist(
                generatePageId(
                    TimelineViewModel.Kind.MODEL_AUDITION,
                    item.id.toString()
                )
            )
        ) {
            PlayerHelper.instance.setPlaylist(
                item.getPlaylist(),
                generatePageId(
                    TimelineViewModel.Kind.MODEL_AUDITION,
                    item.id.toString()
                ),
                this
            )
        }
        PlayerHelper.instance.onPlay(item.id.toString())
    }

    private fun generatePageId(kind: TimelineViewModel.Kind, id: String): String {
        return PlayerHelper.generatePageId(kind, id)
    }

    /**
     * 官方模型列表播放
     */
    private fun onPlayOfficial(position: Int) {
        val item = officialModelListAdapter.getItemData(position)
        if (item.isPlaying == true) {
            PlayerHelper.instance.onPause()
        } else {
            //如果数据为空 就重新拉取数据播放
            if (item.getPlaylist().isEmpty()) {
                if (item.isOfficial && !TextUtils.isEmpty(item.auditionFile)) {
                    val media = MediaInfo(
                        id = item.auditionFile!!,
                        resUrl = item.auditionFile,
                        "https://oss.openvoiceover.com/avatars/logo.png"
                    )

                    val dataList = arrayListOf(media)
                    officialModelListAdapter.data[position] = item.copy(playlist = dataList)
                    playOfficial(position)
                } else {
                    viewModel.modelStatus(item.id, null) {
                        if (it.isEmpty()) {
                            ToastUtil.showCenter(getString(R.string.tips_noaudition))
                            return@modelStatus
                        }
                        officialModelListAdapter.data[position] = item.copy(playlist = it)
                        playOfficial(position)
                    }
                }
            } else {
                playOfficial(position)
            }
        }
    }

    private fun playOfficial(position: Int) {
        val item = officialModelListAdapter.getItemData(position)

        if (!PlayerHelper.instance.isCurrentPlaylist(
                generatePageId(
                    TimelineViewModel.Kind.MODEL_AUDITION_OFFICIAL,
                    item.id.toString()
                )
            )
        ) {
            PlayerHelper.instance.setPlaylist(
                item.getPlaylist(),
                generatePageId(
                    TimelineViewModel.Kind.MODEL_AUDITION_OFFICIAL,
                    item.id.toString()
                ),
                this
            )
        }
        PlayerHelper.instance.onPlay(item.id.toString())
    }


    @Subscribe(threadMode = ThreadMode.MAIN)
    fun subscribeEvent(event: EventRecorder) {
        event.voiceModel?.run {
            refreshContent()
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun subscribeEvent(event: EventActivateVoice) {
        if (event.hashCode == hashCode()) return
        if (modelListType == MODELLIST_NORMAL) {
            val position = adapter.data.indexOfFirst { event.modelId == it.id }
            if (position == -1) {
                binding.recyclerView.isRefreshing = true
            } else {
                adapter.setActivatedPosition(position)
            }
        } else if (modelListType == MODELLIST_SETUP || modelListType == MODELLIST_MODIFY_OFFICAL) {
            val position = officialModelListAdapter.data.indexOfFirst { event.modelId == it.id }
            if (position == -1) {
                binding.recyclerView.isRefreshing = true
            } else {
                officialModelListAdapter.setActivatedPosition(position)
            }
        }


    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun subscribeEvent(event: EventUpdateModel) {
        if (::adapter.isInitialized) {
            val position = adapter.data.indexOfFirst { event.modelId == it.id }
            if (position == -1) {
                binding.recyclerView.isRefreshing = true
            } else {
                adapter.data[position] = adapter.getItemData(position).copy(price = event.price)
                adapter.refreshNotifyItemChanged(position)
            }
        }
    }


    @Subscribe(threadMode = ThreadMode.MAIN)
    fun subscribeEvent(event: EventSellModel) {
        if (modelListType == MODELLIST_NORMAL) {
            val position = adapter.data.indexOfFirst { event.modelId == it.id }
            if (position == -1) {
                binding.recyclerView.isRefreshing = true
            } else {
                adapter.data[position] =
                    adapter.getItemData(position).copy(price = event.price, payload = event.sign)
                adapter.refreshNotifyItemChanged(position)
            }
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun subscribeEvent(event: EventDeleteModel) {
        val position = adapter.data.indexOfFirst { event.modelId == it.id }
        if (position == -1) {
            binding.recyclerView.isRefreshing = true
        } else {
            adapter.removeData(position)
        }

        if (adapter.itemCount == 0)
            binding.recyclerView.isRefreshing = true
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun subscribePurchasedModel(event: EventPurchasedModel) {
        binding.recyclerView.isRefreshing = true
    }

    override fun onDestroy() {
        super.onDestroy()
        EventBus.getDefault().unregister(this)
    }


    override fun onIsPlayingChanged(item: MediaInfo, isPlaying: Boolean) {
        Timber.d("isPlaying = $isPlaying")
        if (modelListType == MODELLIST_NORMAL) {
            adapter.playingStateChange(PlayerHelper.instance.decodeParentId(), isPlaying)
        } else if (modelListType == MODELLIST_SETUP || modelListType == MODELLIST_MODIFY_OFFICAL) {
            officialModelListAdapter.playingStateChange(
                PlayerHelper.instance.decodeParentId(),
                isPlaying
            )
        }
    }

    override fun onPlaylistDestroy(item: MediaInfo) {
        onIsPlayingChanged(item, false)
        Timber.d("onPlaylistDestroy ")
    }

    override fun navigateTo(item: MediaInfo) {
    }

}