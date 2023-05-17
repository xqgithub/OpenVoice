package com.shannon.openvoice.business.draft

import com.shannon.android.lib.base.adapter.BaseFunBindingRecyclerViewAdapter
import com.shannon.android.lib.extended.gone
import com.shannon.android.lib.extended.singleClick
import com.shannon.android.lib.extended.visibility
import com.shannon.android.lib.util.ToastUtil
import com.shannon.openvoice.databinding.ItemComposeMediaBinding
import com.shannon.openvoice.extended.loadImage
import com.shannon.openvoice.extended.loadUri
import com.shannon.openvoice.model.QueuedMedia

/**
 *
 * @Package:        com.shannon.openvoice.business.compose
 * @ClassName:      ComposeMediaAdapter
 * @Description:     作用描述
 * @Author:         czhen
 * @CreateDate:     2022/8/11 13:52
 */
class DraftMediaAdapter() :
    BaseFunBindingRecyclerViewAdapter<QueuedMedia, ItemComposeMediaBinding>() {

    var onItemClick: () -> Unit = {}

    override fun bindView(binding: ItemComposeMediaBinding, bean: QueuedMedia, position: Int) {
        binding.run {
            coverView.setProgress(bean.uploadPercent)
            coverView.loadImage(bean.uri)
            deleteButton.gone()
            root.singleClick { onItemClick() }
        }
    }
}