package com.shannon.openvoice.components

import android.content.Context
import android.graphics.Color
import android.util.AttributeSet
import android.view.View
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import com.shannon.android.lib.extended.*
import com.shannon.openvoice.R
import com.shannon.openvoice.databinding.ViewErrorpageBinding

/**
 * Date:2022/7/28
 * Time:15:38
 * author:dimple
 * 自定义错误页面，可以根据值显示不同的信息
 */
class ErrorPageView @JvmOverloads constructor(
    private var mContext: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : ConstraintLayout(mContext, attrs, defStyleAttr) {

    private lateinit var mBinding: ViewErrorpageBinding

    //点击事件
    private lateinit var viewOnClickListener: () -> Unit

    init {
        val root = View.inflate(context, R.layout.view_errorpage, this)
        mBinding = ViewErrorpageBinding.bind(root)
    }

    /**
     * 设置图片
     */
    fun setErrorIcon(
        img: Int,
        width: Int = 36.dp,
        height: Int = 36.dp
    ) {
        mBinding.ivError.setImageDrawable(ContextCompat.getDrawable(mContext, img))
        val params = mBinding.ivError.layoutParams
        params.height = height
        params.width = width
        mBinding.ivError.layoutParams = params
    }

    /**
     * 设置内容
     */
    fun setErrorContent(
        content: String = "",
        _textSize: Float = 12f,
        _textColor: String = "#57493B"
    ) {
        mBinding.tvError.apply {
            text = content
            textSize = _textSize
            setTextColor(Color.parseColor(_textColor))
        }
    }

    /**
     * 设置去操作
     */
    fun setToOperation(content: String) {
        content.isNotNull({
            mBinding.tvToOperate.apply {
                visibility = View.VISIBLE
                text = it
                setDynamicShapeRectangle(
                    arrayOf(this),
                    -1,
                    "",
                    24f.dp,
                    24f.dp,
                    24f.dp,
                    24f.dp,
                    null,
                    bgColor = arrayOf("#4EEAC4")
                )
            }
        }, {
            mBinding.tvToOperate.visibility = View.GONE
        })
    }


    /**
     * 点击事件
     */
    fun toOperateOnClickListener(viewOnClickListener: () -> Unit) {
        this.viewOnClickListener = viewOnClickListener
        mBinding.tvToOperate.singleClick {
            this.viewOnClickListener.invoke()
        }
    }

    /**
     * 修改图片距离高度
     */
    fun changeErrorIconPositionToTOP(offsetTop: Int) {
        val layout = LayoutParams(
            LayoutParams.WRAP_CONTENT,
            LayoutParams.WRAP_CONTENT
        )
        layout.startToStart = R.id.cl_main
        layout.endToEnd = R.id.cl_main
        layout.topToTop = R.id.cl_main
        layout.topMargin = offsetTop
        mBinding.ivError.layoutParams = layout
    }

    /**
     * 修改文字距离高度
     */
    fun changeErrorTextPositionToTOP(offsetTop: Int) {
        val layout = LayoutParams(
            LayoutParams.WRAP_CONTENT,
            LayoutParams.WRAP_CONTENT
        )
        layout.startToStart = R.id.cl_main
        layout.endToEnd = R.id.cl_main
        layout.topToBottom = R.id.iv_error
        layout.topMargin = offsetTop
        mBinding.tvError.layoutParams = layout
    }
}