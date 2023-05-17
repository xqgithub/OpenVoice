package com.shannon.openvoice.business.compose.models

import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import com.shannon.android.lib.base.activity.KBaseFragment
import com.shannon.android.lib.extended.addMoreData
import com.shannon.android.lib.extended.bindEmptyView
import com.shannon.android.lib.extended.clearAnimations
import com.shannon.android.lib.extended.removeEmptyView
import com.shannon.openvoice.R
import com.shannon.openvoice.business.creation.CreationViewModel
import com.shannon.openvoice.business.creation.ModelListFragment
import com.shannon.openvoice.databinding.FragmentVoiceModelListBinding
import com.shannon.openvoice.event.UniversalEvent
import com.shannon.openvoice.model.EventActivateVoice
import com.shannon.openvoice.model.VoiceModelSelected
import org.greenrobot.eventbus.EventBus

/**
 *
 * @Package:        com.shannon.openvoice.business.compose.models
 * @ClassName:      ModelsFragment
 * @Description:     作用描述
 * @Author:         czhen
 * @CreateDate:     2023/3/8 9:34
 */
class ModelsFragment : KBaseFragment<FragmentVoiceModelListBinding, CreationViewModel>() {
    private val modelsAdapter by lazy { ModelsAdapter(this::onActivation) }
    private var pageNum = 0
    override fun onInit() {
        binding.run {
            recyclerView.layoutManager = LinearLayoutManager(requireContext())
            recyclerView.adapter = modelsAdapter
            recyclerView.clearAnimations()
            recyclerView.setOnRefreshListener { refresh() }
            recyclerView.setOnLoadMoreListener {
                pageNum++
                viewModel.fetchVoiceModels(pageNum, modelsAdapter.itemCount, "unused") {
                    modelsAdapter.addMoreData(it)
                }
            }
        }
    }

    override fun onLazyInit() {
        super.onLazyInit()
        binding.recyclerView.isRefreshing = true
    }

    private fun refresh() {
        pageNum = 0
        viewModel.fetchVoiceModels(pageNum = pageNum, status = "unused") { beans ->
            bindEmptyView(beans.isEmpty())
            modelsAdapter.setNewData(beans)
        }
    }

    private fun bindEmptyView(isEmpty: Boolean) {
        if (isEmpty) {
            binding.recyclerView.bindEmptyView(
                R.drawable.ic_development_model_list, R.string.desc_wilderness,
                height = ViewGroup.LayoutParams.MATCH_PARENT
            )
        } else {
            binding.recyclerView.removeEmptyView()
        }
    }

    private fun onActivation(position: Int) {
        EventBus.getDefault()
            .post(
                UniversalEvent(
                    UniversalEvent.modifySelectedSoundModel,
                    VoiceModelSelected(position, modelsAdapter.data)
                )
            )
    }

}