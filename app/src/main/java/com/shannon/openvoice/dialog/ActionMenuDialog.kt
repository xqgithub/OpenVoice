package com.shannon.openvoice.dialog

import android.content.Context
import android.os.Bundle
import androidx.recyclerview.widget.LinearLayoutManager
import com.shannon.android.lib.base.activity.BottomSheetFixedHeightDialog
import com.shannon.android.lib.extended.inflate
import com.shannon.android.lib.extended.itemDivider
import com.shannon.android.lib.extended.singleClick
import com.shannon.openvoice.R
import com.shannon.openvoice.business.timeline.menu.StatusMenuItem
import com.shannon.openvoice.databinding.DialogActionMoreBinding

/**
 *
 * @Package:        com.shannon.openvoice.dialog
 * @ClassName:      ActionMenuDialog
 * @Author:         czhen
 */
class ActionMenuDialog(
    val mContext: Context,
    private val data: List<StatusMenuItem>,
    val onItemClick: (StatusMenuItem) -> Unit
) :
    BottomSheetFixedHeightDialog(mContext, R.style.TransparentBottomDialog) {

    private val binding by inflate<DialogActionMoreBinding>()

    init {
        setContentView(binding.root)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding.apply {
            actionListView.layoutManager = LinearLayoutManager(context)
            actionListView.adapter = ActionMenuAdapter().also { it.setNewData(data) }
            actionListView.setOnItemClickListener { _, position ->
                dismiss()
                onItemClick(data[position])
            }
            actionListView.itemDivider(headerNoShowSize = 0)
            dismissButton.singleClick { dismiss() }
        }
    }

}