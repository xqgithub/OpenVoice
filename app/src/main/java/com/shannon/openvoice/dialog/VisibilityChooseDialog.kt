package com.shannon.openvoice.dialog

import android.content.Context
import android.os.Bundle
import android.view.View
import com.shannon.android.lib.base.activity.BottomSheetFixedHeightDialog
import com.shannon.android.lib.extended.addArray
import com.shannon.android.lib.extended.inflate
import com.shannon.android.lib.extended.singleClick
import com.shannon.openvoice.R
import com.shannon.openvoice.databinding.DialogChooseVisibilityBinding
import com.shannon.openvoice.model.StatusModel

/**
 *
 * @Package:        com.shannon.openvoice.dialog
 * @ClassName:      VisibilityChooseDialog
 * @Description:     选择发布声文可见类型
 * @Author:         czhen
 * @CreateDate:     2022/8/10 16:17
 */
class VisibilityChooseDialog(
    mContext: Context,
    val visibility: StatusModel.Visibility,
    val onActivation: (StatusModel.Visibility) -> Unit
) :
    BottomSheetFixedHeightDialog(mContext, R.style.TransparentBottomDialog) {

    private val binding by inflate<DialogChooseVisibilityBinding>()
    private var defaultVisibility = visibility
    private val visibilityTextList = arrayListOf<View>()
    init {
        setContentView(binding.root)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding.apply {
            visibilityTextList.addArray(publicView, unlistedView, privateView, directView)
            selectView()
            publicView.singleClick(this@VisibilityChooseDialog::onClick)
            unlistedView.singleClick(this@VisibilityChooseDialog::onClick)
            privateView.singleClick(this@VisibilityChooseDialog::onClick)
            directView.singleClick(this@VisibilityChooseDialog::onClick)
            cancelButton.singleClick { dismiss() }
        }
    }

    private fun selectView() {
        visibilityTextList.withIndex().forEach {
            it.value.isSelected = defaultVisibility.num == it.index + 1
            it.value.isActivated = defaultVisibility.num == it.index + 1
        }
    }

    private fun onClick(v: View) {
        when (v.id) {
            R.id.publicView -> defaultVisibility = StatusModel.Visibility.PUBLIC
            R.id.unlistedView -> defaultVisibility = StatusModel.Visibility.UNLISTED
            R.id.privateView -> defaultVisibility = StatusModel.Visibility.PRIVATE
            R.id.directView -> defaultVisibility = StatusModel.Visibility.DIRECT
        }
        selectView()
        onActivation(defaultVisibility)
        dismiss()
    }

}