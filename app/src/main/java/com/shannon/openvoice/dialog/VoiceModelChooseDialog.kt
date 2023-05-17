package com.shannon.openvoice.dialog

import android.content.Context
import android.os.Bundle
import androidx.recyclerview.widget.LinearLayoutManager
import com.blankj.utilcode.util.ScreenUtils
import com.shannon.android.lib.base.activity.BottomSheetFixedHeightDialog
import com.shannon.android.lib.extended.clearAnimations
import com.shannon.android.lib.extended.inflate
import com.shannon.android.lib.extended.itemDivider
import com.shannon.android.lib.extended.singleClick
import com.shannon.openvoice.R
import com.shannon.openvoice.business.creation.ModelListAdapter
import com.shannon.openvoice.databinding.DialogSoundModelListBinding
import com.shannon.openvoice.model.VoiceModelResult

/**
 *
 * @ClassName:      VoiceModelChooseDialog
 * @Description:     java类作用描述
 * @Author:         czhen
 * @CreateDate:     2022/6/11 10:44
 */
class VoiceModelChooseDialog(
    mContext: Context,
    val data: List<VoiceModelResult>,
    val onActivation: (Int) -> Unit
) :
    BottomSheetFixedHeightDialog(mContext, R.style.TransparentBottomDialog) {

    private val binding by inflate<DialogSoundModelListBinding>()
    init {
        setContentView(binding.root)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding.apply {
            recyclerView.layoutManager = LinearLayoutManager(context)
            recyclerView.adapter = ModelListAdapter(false) {
                onActivation(it)
                dismiss()
            }.also {
                it.setNewDataIgnoreSize(data)
            }
            recyclerView.clearAnimations()
            recyclerView.itemDivider(headerNoShowSize = 0)
            cancelButton.singleClick { dismiss() }
        }
    }


    override fun getFixedHeight(): Int {
        return (ScreenUtils.getAppScreenHeight() * 0.7).toInt()
    }

}