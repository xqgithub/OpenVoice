package com.shannon.openvoice.components

import android.graphics.Rect
import android.view.View
import androidx.recyclerview.widget.RecyclerView

/**
 * Date:2022/2/9
 * Time:10:34
 * author:dimple
 * RecyclerView 分割距离
 */
class CommentDialogDecoration : RecyclerView.ItemDecoration() {

    private var mLeft: Int = 0
    private var mTop: Int = 0
    private var mRight: Int = 0
    private var mBottom: Int = 0
    private var specifiedPosition: Int = -1

    private lateinit var commentdialogdecoration: CommentDialogDecoration

    fun getInstance(): CommentDialogDecoration {
        return CommentDialogDecoration()
    }

//    companion object {
//        val commentdialogdecoration: CommentDialogDecoration by lazy(mode = LazyThreadSafetyMode.SYNCHRONIZED) {
//            CommentDialogDecoration()
//        }
//    }

    /**
     * 设置间距
     */
    fun setSpaceItemDecoration(
        left: Int,
        top: Int,
        right: Int,
        bottom: Int
    ): CommentDialogDecoration {
        this.mLeft = left
        this.mTop = top
        this.mRight = right
        this.mBottom = bottom
        return this
    }


    /**
     * 排除指定的item不按照规则
     */
    fun excludeDesignationItem(specifiedPosition: Int): CommentDialogDecoration {
        this.specifiedPosition = specifiedPosition
        return this
    }


    override fun getItemOffsets(
        outRect: Rect,
        view: View,
        parent: RecyclerView,
        state: RecyclerView.State
    ) {
        if (specifiedPosition != -1) {
            if (parent.getChildLayoutPosition(view) == specifiedPosition) {
                outRect.apply {
                    left = 0
                    top = 0
                    right = 0
                    bottom = 0
                }
            } else {
                outRect.apply {
                    left = mLeft
                    top = mTop
                    right = mRight
                    bottom = mBottom
                }
            }
        } else {
            outRect.apply {
                left = mLeft
                top = mTop
                right = mRight
                bottom = mBottom
            }
        }
    }


}