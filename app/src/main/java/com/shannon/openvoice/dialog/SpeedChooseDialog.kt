package com.shannon.openvoice.dialog

import android.content.Context
import android.graphics.Color
import android.os.Bundle
import android.view.View
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.shannon.android.lib.extended.inflate
import com.shannon.android.lib.extended.singleClick
import com.shannon.android.lib.util.ThemeUtil
import com.shannon.openvoice.R
import com.shannon.openvoice.databinding.DialogSpeedListBinding

/**
 *
 * @ClassName:      SpeedChooseDialog
 * @Description:     java类作用描述
 * @Author:         czhen
 */
class SpeedChooseDialog(private val mContext: Context, private val speed: Float, val onCallback: (Float) -> Unit) :
    BottomSheetDialog(mContext, R.style.TransparentBottomDialog) {

    private val binding by inflate<DialogSpeedListBinding>()
    private val normalColor = ThemeUtil.getColor(mContext, android.R.attr.textColorPrimary)
    private val selectableColor = ThemeUtil.getColor(mContext, android.R.attr.colorPrimary)

    init {
        setContentView(binding.root)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding.apply {
            speedView1.singleClick(this@SpeedChooseDialog::onClick)
            speedView2.singleClick(this@SpeedChooseDialog::onClick)
            speedView3.singleClick(this@SpeedChooseDialog::onClick)
            speedView4.singleClick(this@SpeedChooseDialog::onClick)
            speedView5.singleClick(this@SpeedChooseDialog::onClick)
            speedView6.singleClick(this@SpeedChooseDialog::onClick)
            cancelButton.singleClick(this@SpeedChooseDialog::onClick)
        }
        setTextColor(speed)
    }

    private fun onClick(v: View) {
        when (v.id) {
            R.id.speedView1 -> {
                val speed = 0.5f
                onChoose(speed)
            }
            R.id.speedView2 -> {
                val speed = 0.75f
                onChoose(speed)
            }
            R.id.speedView3 -> {
                val speed = 1.0f
                onChoose(speed)
            }
            R.id.speedView4 -> {
                val speed = 1.25f
                onChoose(speed)
            }
            R.id.speedView5 -> {
                val speed = 1.5f
                onChoose(speed)
            }
            R.id.speedView6 -> {
                val speed = 2.0f
                onChoose(speed)
            }
            R.id.cancelButton -> {
                dismiss()
            }
        }
    }

    private fun onChoose(speed: Float) {
        setTextColor(speed)
        onCallback(speed)
        dismiss()
    }

    private fun setTextColor(speed: Float) {
        binding.apply {
            speedView1.setTextColor(if (speed == 0.5f) selectableColor else normalColor)
            speedView2.setTextColor(if (speed == 0.75f) selectableColor else normalColor)
            speedView3.setTextColor(if (speed == 1.0f) selectableColor else normalColor)
            speedView4.setTextColor(if (speed == 1.25f) selectableColor else normalColor)
            speedView5.setTextColor(if (speed == 1.5f) selectableColor else normalColor)
            speedView6.setTextColor(if (speed == 2.0f) selectableColor else normalColor)
        }
    }
}