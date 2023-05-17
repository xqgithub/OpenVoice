package com.shannon.android.lib.base.activity

import android.content.Context
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog

/**
 *
 * @ClassName:      BottomSheetFixedHeightDialog
 * @Description:    BottomSheet 固定高度
 * @Author:         czhen
 */
open class BottomSheetFixedHeightDialog : BottomSheetDialog {

    private var defaultHeight = ViewGroup.LayoutParams.WRAP_CONTENT

    constructor(context: Context) : super(context)
    constructor(context: Context, theme: Int) : super(context, theme)
    constructor(context: Context, theme: Int, height: Int) : super(context, theme) {
        defaultHeight = height
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        fixedHeight()
        super.onCreate(savedInstanceState)
    }

    override fun onStart() {
        super.onStart()

    }

    /**
     * 固定高度
     */
    private fun fixedHeight() {
        window?.apply {
            setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
            decorView.findViewById<FrameLayout>(com.google.android.material.R.id.design_bottom_sheet)?.apply {
                layoutParams.height = getFixedHeight()
                layoutParams.width = ViewGroup.LayoutParams.MATCH_PARENT
            }
            if (getFixedHeight() != ViewGroup.LayoutParams.WRAP_CONTENT) {
                behavior.peekHeight = getFixedHeight()
            }
            behavior.state = BottomSheetBehavior.STATE_EXPANDED
        }
    }

    protected open fun getFixedHeight(): Int {
        return defaultHeight
    }

    /**
     * 禁止手势滑动
     */
    fun disableBottomSheetBehavior() {
        val view: View? = window?.findViewById(com.google.android.material.R.id.design_bottom_sheet)
        view?.let {
            val behavior = BottomSheetBehavior.from(view)
            behavior.addBottomSheetCallback(object : BottomSheetBehavior.BottomSheetCallback() {
                override fun onStateChanged(bottomSheet: View, newState: Int) {
                    if (newState == BottomSheetBehavior.STATE_DRAGGING || newState == BottomSheetBehavior.STATE_SETTLING) {
                        behavior.state = BottomSheetBehavior.STATE_COLLAPSED
                    }
                }

                override fun onSlide(bottomSheet: View, slideOffset: Float) {
                }
            })
        }
    }

    /**
     * 禁止半展开
     */
    fun disableBottomSheetBehaviorHalfExpanded() {
        val view: View? = window?.findViewById(com.google.android.material.R.id.design_bottom_sheet)
        view?.let {
            val behavior = BottomSheetBehavior.from(view)
            behavior.skipCollapsed = true
            behavior.addBottomSheetCallback(object : BottomSheetBehavior.BottomSheetCallback() {
                override fun onStateChanged(bottomSheet: View, newState: Int) {
                    if (newState == BottomSheetBehavior.STATE_DRAGGING || newState == BottomSheetBehavior.STATE_SETTLING) {
                    }
                }

                override fun onSlide(bottomSheet: View, slideOffset: Float) {
                }
            })
        }
    }

    fun setSheetState(@BottomSheetBehavior.State state: Int) {
        val view: View? = window?.findViewById(com.google.android.material.R.id.design_bottom_sheet)
        view?.let {
            val behavior = BottomSheetBehavior.from(view)
            behavior.state = state
        }
    }
}