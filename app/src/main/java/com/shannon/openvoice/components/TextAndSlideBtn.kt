package com.shannon.openvoice.components

import android.content.Context
import android.graphics.Color
import android.util.AttributeSet
import android.view.View
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import com.shannon.android.lib.extended.dp
import com.shannon.android.lib.extended.isNotNull
import com.shannon.openvoice.R
import com.shannon.openvoice.databinding.ViewTextSlidebtnUiBinding

/**
 * Date:2022/8/1
 * Time:11:24
 * author:dimple
 * 描述文字+滑动按钮
 */
class TextAndSlideBtn @JvmOverloads constructor(
    private var mContext: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : ConstraintLayout(mContext, attrs, defStyleAttr) {

    private lateinit var mBinding: ViewTextSlidebtnUiBinding

    init {
        val root = View.inflate(context, R.layout.view_text_slidebtn_ui, this)
        mBinding = ViewTextSlidebtnUiBinding.bind(root)
    }


    /**
     * 设置TvContent的值
     */
    fun setTvContentData(
        _visibility: Int = View.VISIBLE,
        content: String = "",
        _textSize: Float = 12f,
        _textColorInt: Int? = null,
        _textColorString: String = "#57493B",
    ) {
        mBinding.tvContent.apply {
            visibility = _visibility
            text = content
            textSize = _textSize
            _textColorInt.isNotNull({
                setTextColor(ContextCompat.getColor(mContext, it))
            }, {
                setTextColor(Color.parseColor(_textColorString))
            })
        }
    }

    /**
     * 设置滑动按钮
     * @param isBigCircle 是否是大圆
     * @param strokeLineColor           圆角矩形的边颜色
     * @param strokeCheckedSolidColor   圆角矩形的填充颜色
     * @param strokeNoCheckedSolidColor 圆角矩形非选择状态下填充颜色
     * @param circleCheckedColor        内部小圆被选中的颜色
     * @param circleNoCheckedColor      内部小圆未被选中的颜色
     */
    fun setSlideButton(
        _visibility: Int = View.VISIBLE,
        width: Int = 50.dp,
        height: Int = 30.dp,
        isBigCircle: Boolean = false,
        strokeLineColor: Int = 0,
        strokeCheckedSolidColor: Int = 0,
        strokeNoCheckedSolidColor: Int = 0,
        circleCheckedColor: Int = 0,
        circleNoCheckedColor: Int = 0,
        OnChecked: (isChecked: Boolean) -> Unit
    ): SlideButton {
        mBinding.slidebutton.apply {
            visibility = _visibility
            layoutParams.height = height
            layoutParams.width = width
            if (isBigCircle) {
                setBigCircleModel(
                    strokeLineColor,
                    strokeCheckedSolidColor,
                    strokeNoCheckedSolidColor,
                    circleCheckedColor,
                    circleNoCheckedColor
                )
            } else {
                setSmallCircleModel(
                    strokeLineColor,
                    strokeCheckedSolidColor,
                    strokeNoCheckedSolidColor,
                    circleCheckedColor,
                    circleNoCheckedColor
                )
            }
            setOnCheckedListener {
                OnChecked(it)
            }
            return this
        }
    }
}