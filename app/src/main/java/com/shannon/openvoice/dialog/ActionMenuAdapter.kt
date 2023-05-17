package com.shannon.openvoice.dialog

import com.shannon.android.lib.base.adapter.BaseFunBindingRecyclerViewAdapter
import com.shannon.openvoice.business.timeline.menu.StatusMenuItem
import com.shannon.openvoice.databinding.LayoutActionMenuItemBinding

/**
 *
 * @Package:        com.shannon.openvoice.dialog
 * @ClassName:      ActionMenuAdapter
 * @Description:     作用描述
 * @Author:         czhen
 * @CreateDate:     2023/3/3 10:03
 */
class ActionMenuAdapter :
    BaseFunBindingRecyclerViewAdapter<StatusMenuItem, LayoutActionMenuItemBinding>() {
    override fun bindView(
        binding: LayoutActionMenuItemBinding,
        bean: StatusMenuItem,
        position: Int
    ) {

        binding.run {
            binding.menuIconView.setImageResource(bean.drawableId)
            binding.menuNameView.text = root.context.getString(bean.stringId)
        }
    }
}