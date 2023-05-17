package com.shannon.openvoice.business.compose

import com.shannon.android.lib.base.adapter.BaseFunBindingRecyclerViewAdapter
import com.shannon.android.lib.extended.singleClick
import com.shannon.openvoice.databinding.ItemComposeMediaBinding
import com.shannon.openvoice.extended.loadImage
import com.shannon.openvoice.model.QueuedMedia

/**
 *
 * @Package:        com.shannon.openvoice.business.compose
 * @ClassName:      ComposeMediaAdapter
 * @Description:     作用描述
 * @Author:         czhen
 * @CreateDate:     2022/8/11 13:52
 */
class ComposeMediaAdapter(
    private val onDelete: (item: QueuedMedia) -> Unit
) :
    BaseFunBindingRecyclerViewAdapter<QueuedMedia, ItemComposeMediaBinding>() {


    override fun bindView(binding: ItemComposeMediaBinding, bean: QueuedMedia, position: Int) {
        binding.run {
            coverView.setProgress(bean.uploadPercent)
            coverView.loadImage(bean.uri)
            deleteButton.singleClick { onDelete(bean) }
        }
    }
}