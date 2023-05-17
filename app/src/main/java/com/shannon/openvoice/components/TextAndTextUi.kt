package com.shannon.openvoice.components

import android.content.Context
import android.graphics.Color
import android.util.AttributeSet
import android.view.View
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import com.shannon.android.lib.extended.*
import com.shannon.openvoice.R
import com.shannon.openvoice.databinding.ViewTextTextUiBinding

/**
 * Date:2022/8/1
 * Time:9:56
 * author:dimple
 * 文字描述+文字描述的组合
 */
class TextAndTextUi @JvmOverloads constructor(
    private var mContext: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : ConstraintLayout(mContext, attrs, defStyleAttr) {

    private lateinit var mBinding: ViewTextTextUiBinding

    //Item点击事件
    private lateinit var itemOnClickListener: () -> Unit

    private var textAndTextType = TextAndTextType.top_bottom

    enum class TextAndTextType {
        top_bottom, left_right,
    }

    init {
        val root = View.inflate(context, R.layout.view_text_text_ui, this)
        mBinding = ViewTextTextUiBinding.bind(root)
    }

    /**
     * 设置UI控件格式类型
     */
    fun setUIFormatType(textAndTextType: TextAndTextType) {
        this.textAndTextType = textAndTextType
        if (this.textAndTextType == TextAndTextType.top_bottom) {
            mBinding.tvTextTwo.gone()
            mBinding.tvTextThree.visible()
        } else {
            mBinding.tvTextThree.gone()
            mBinding.tvTextTwo.visible()
        }
    }


    /**
     * 设置textone的值
     */
    fun setTextOneData(
        _visibility: Int = View.VISIBLE,
        content: String = "",
        _textSize: Float = 12f,
        _textColorInt: Int? = null,
        _textColorString: String = "#57493B",
    ) {

        if (textAndTextType == TextAndTextType.left_right) {
            val layout = LayoutParams(
                LayoutParams.WRAP_CONTENT,
                LayoutParams.WRAP_CONTENT
            )
            layout.startToStart = R.id.cl_main
            layout.topToTop = R.id.cl_main
            layout.endToStart = R.id.tvTextTwo
            layout.topMargin = 0
            mBinding.tvTextOne.layoutParams = layout
        }

        mBinding.tvTextOne.apply {
            visibility = _visibility
            text = content
            textSize = _textSize
            _textColorInt.isNotNull({
//                setTextColor(ContextCompat.getColor(mContext, it))
                setTextColor(_textColorInt!!)
            }, {
                setTextColor(Color.parseColor(_textColorString))
            })
        }
    }

    /**
     * 设置TextTwo或TextThree的值
     */
    fun setTextData(
        _visibility: Int = VISIBLE,
        content: String = "",
        _textSize: Float = 12f,
        _textColorInt: Int? = null,
        _textColorString: String = "#57493B",
    ) {
        with(if (textAndTextType == TextAndTextType.top_bottom) mBinding.tvTextThree else mBinding.tvTextTwo) {
            apply {
                visibility = _visibility
                text = content
                textSize = _textSize
                _textColorInt.isNotNull({
//                    setTextColor(ContextCompat.getColor(mContext, it))
                    setTextColor(_textColorInt!!)
                }, {
                    setTextColor(Color.parseColor(_textColorString))
                })
            }
        }
    }


    /**
     * 获取 TextTwo 或者 TextThree的值
     */
    fun getTextData(): TextView {
        return if (textAndTextType == TextAndTextType.top_bottom) mBinding.tvTextThree else mBinding.tvTextTwo
    }

    /**
     * item点击事件
     */
    fun setItemOnClickListener(itemOnClickListener: () -> Unit) {
        this.itemOnClickListener = itemOnClickListener
        mBinding.clMain.singleClick {
            this.itemOnClickListener.invoke()
        }
    }


    /**
     * 修改文字间的距离
     */
    fun changeTvTextTwoPositionToTextOne(offset: Int = 0) {
        val layout = LayoutParams(
            LayoutParams.WRAP_CONTENT,
            LayoutParams.WRAP_CONTENT
        )
        layout.startToEnd = R.id.tvTextOne
        layout.topToTop = R.id.tvTextOne
        layout.bottomToBottom = R.id.tvTextOne
        layout.marginStart = offset
        mBinding.tvTextTwo.layoutParams = layout
    }

    /**
     * 修改文字间的距离
     */
    fun changeTvTextThreePositionToTextOne(offset: Int = 0) {
        val layout = LayoutParams(
            LayoutParams.WRAP_CONTENT,
            LayoutParams.WRAP_CONTENT
        )
        layout.startToStart = R.id.cl_main
        layout.topToBottom = R.id.tvTextOne
        layout.endToEnd = R.id.cl_main
        layout.topMargin = offset
        mBinding.tvTextThree.layoutParams = layout
    }
    
}