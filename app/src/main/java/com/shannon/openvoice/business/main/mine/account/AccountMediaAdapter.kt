package com.shannon.openvoice.business.main.mine.account

import android.content.Context
import com.shannon.android.lib.base.adapter.BaseFunBindingRecyclerViewAdapter
import com.shannon.android.lib.extended.gone
import com.shannon.android.lib.extended.visible
import com.shannon.openvoice.R
import com.shannon.openvoice.databinding.AdapterAccountmediaBinding
import com.shannon.openvoice.extended.loadImage
import com.shannon.openvoice.model.Attachment
import com.shannon.openvoice.model.AttachmentViewData
import me.jingbin.library.adapter.BaseByViewHolder

/**
 * Date:2022/8/22
 * Time:10:33
 * author:dimple
 *  用户媒体列表适配器
 */
class AccountMediaAdapter(val mContext: Context) :
    BaseFunBindingRecyclerViewAdapter<AttachmentViewData, AdapterAccountmediaBinding>() {

    override fun bindView(
        holder: BaseByViewHolder<AttachmentViewData>,
        binding: AdapterAccountmediaBinding,
        bean: AttachmentViewData,
        position: Int
    ) {
        binding.apply {
            if (bean.attachment.type == Attachment.Type.VIDEO) {
                mediaPlayDisplay.visible()
            } else {
                mediaPlayDisplay.gone()
            }
            sivMediaImagePreview.loadImage(bean.attachment.previewUrl ?: "")
        }
        
        holder.addOnClickListener(R.id.cl_main)
    }

    override fun bindView(
        binding: AdapterAccountmediaBinding,
        bean: AttachmentViewData,
        position: Int
    ) {
    }


}