package com.shannon.openvoice.business.draft

import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.shannon.android.lib.base.adapter.BaseFunBindingRecyclerViewAdapter
import com.shannon.android.lib.base.adapter.BaseFunBindingViewHolder
import com.shannon.android.lib.extended.dp
import com.shannon.android.lib.extended.itemDivider
import com.shannon.android.lib.extended.singleClick
import com.shannon.android.lib.extended.visibility
import com.shannon.openvoice.R
import com.shannon.openvoice.databinding.ItemDraftBinding
import com.shannon.openvoice.db.DraftAttachment
import com.shannon.openvoice.db.DraftModel
import com.shannon.openvoice.model.QueuedMedia
import me.jingbin.library.decoration.SpacesItemDecoration

/**
 *
 * @Package:        com.shannon.openvoice.business.draft
 * @ClassName:      DraftAdapter
 * @Description:     作用描述
 * @Author:         czhen
 * @CreateDate:     2022/8/15 9:29
 */
class DraftAdapter(val onItemClick: (Int) -> Unit, val onDelete: (Int) -> Unit) :
    BaseFunBindingRecyclerViewAdapter<DraftModel, ItemDraftBinding>() {

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): BaseFunBindingViewHolder<DraftModel, ItemDraftBinding> {
        val holder = super.onCreateViewHolder(parent, viewType)
        val context = holder.viewBinding.root.context
        holder.viewBinding.mediaListView.layoutManager =
            LinearLayoutManager(context, RecyclerView.HORIZONTAL, false)
        holder.viewBinding.mediaListView.itemDivider(
            SpacesItemDecoration.HORIZONTAL,
            R.color.transparent,
            10.dp
        )
        holder.viewBinding.mediaListView.adapter = DraftMediaAdapter()
        return holder
    }

    override fun bindView(binding: ItemDraftBinding, bean: DraftModel, position: Int) {
        binding.run {
            draftSendingInfo.visibility(bean.failedToSend)
            contentView.text = bean.content

            deleteButton.singleClick {
                onDelete(position)
            }
            (mediaListView.adapter as DraftMediaAdapter).setNewDataIgnoreSize(convertQueueMedia(bean.attachments))
            (mediaListView.adapter as DraftMediaAdapter).onItemClick = {
                onItemClick(position)
            }
            root.singleClick { onItemClick(position) }
        }

    }

    private fun convertQueueMedia(attachments: List<DraftAttachment>): List<QueuedMedia> {
        return attachments.map {
            val mediaType =
                if (it.type == DraftAttachment.Type.IMAGE) QueuedMedia.Type.IMAGE else QueuedMedia.Type.VIDEO
            QueuedMedia(0, it.uri, mediaType, it.mediaSize, -1, it.serverId)
        }
    }
}