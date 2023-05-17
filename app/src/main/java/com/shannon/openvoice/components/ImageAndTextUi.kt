package com.shannon.openvoice.components

import android.content.Context
import android.graphics.Color
import android.text.SpannableString
import android.text.method.LinkMovementMethod
import android.util.AttributeSet
import android.view.View
import androidx.annotation.ColorInt
import androidx.annotation.DrawableRes
import androidx.annotation.StyleRes
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import com.google.android.material.shape.ShapeAppearanceModel
import com.shannon.android.lib.extended.*
import com.shannon.openvoice.R
import com.shannon.openvoice.databinding.ViewImgTextUiBinding

/**
 * Date:2022/7/29
 * Time:10:42
 * author:dimple
 * 自定义图片和文字控件
 * 上下样式 和 左右样式
 */
class ImageAndTextUi @JvmOverloads constructor(
    private var mContext: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : ConstraintLayout(mContext, attrs, defStyleAttr) {

    private lateinit var mBinding: ViewImgTextUiBinding

    //控件类型，默认是上下格式
    private var imageAndTextType: ImageAndTextType = ImageAndTextType.top_bottom

    //Item点击事件
    private lateinit var itemOnClickListener: () -> Unit


    enum class ImageAndTextType {
        top_bottom, left_right,
    }

    init {
        val root = View.inflate(context, R.layout.view_img_text_ui, this)
        mBinding = ViewImgTextUiBinding.bind(root)
    }


    /**
     * 设置UI控件格式类型
     */
    fun setUIFormatType(imageAndTextType: ImageAndTextType) {
        this.imageAndTextType = imageAndTextType

        if (imageAndTextType == ImageAndTextType.top_bottom) {
            mBinding.tvContent.gone()
            mBinding.tvContent2.visible()
        } else {
            mBinding.tvContent2.gone()
            mBinding.tvContent.visible()
        }
    }

    /**
     * 设置图片信息
     */
    fun setAvatarDataFromRes(
        @DrawableRes imgId: Int = R.drawable.icon_default_avatar,
        width: Int = 36.dp,
        height: Int = 36.dp,
        @StyleRes shapeAppearanceModelInt: Int = -1
    ) {
        mBinding.sivAvatar.apply {
            //设置ShapeableImageView的模式
            if (shapeAppearanceModelInt != -1) {
                shapeAppearanceModel =
                    ShapeAppearanceModel.builder(mContext, shapeAppearanceModelInt, 0).build()
            }
            setImageDrawable(ContextCompat.getDrawable(mContext, imgId))
            layoutParams.height = height
            layoutParams.width = width
        }
    }

    /**
     * 设置文字信息
     */
    fun setTextData(
        content: String = "",
        _textSize: Float = 12f,
        @ColorInt _textColorInt: Int? = null,
        _textColorString: String = "#57493B",
        _spannableString: SpannableString? = null
    ) {
        var textView =
            if (imageAndTextType == ImageAndTextType.top_bottom) mBinding.tvContent2 else mBinding.tvContent
        textView.apply {
            text = if (isBlankPlus(_spannableString)) content else _spannableString
            textSize = _textSize
            _textColorInt.isNotNull({
//                setTextColor(ContextCompat.getColor(mContext, it))
                setTextColor(it)
            }, {
                setTextColor(Color.parseColor(_textColorString))
            })

            if (!isBlankPlus(_spannableString)) {
                movementMethod = LinkMovementMethod.getInstance()
                highlightColor = ContextCompat.getColor(mContext, R.color.transparent)
            }
        }
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
     * 修改文字距离图片的位置
     */
    fun changeTvContentPositionToAvatar(offset: Int = 0) {
        val layout = LayoutParams(
            LayoutParams.WRAP_CONTENT,
            LayoutParams.WRAP_CONTENT
        )
        layout.startToEnd = R.id.sivAvatar
        layout.topToTop = R.id.sivAvatar
        layout.bottomToBottom = R.id.sivAvatar
        layout.leftMargin = offset
        mBinding.tvContent.layoutParams = layout
    }

    /**
     * 修改文字距离图片的位置
     */
    fun changeTvContentPositionToAvatar2(offset: Int = 0) {
        val layout = LayoutParams(
            LayoutParams.WRAP_CONTENT,
            LayoutParams.WRAP_CONTENT
        )
        layout.startToStart = R.id.sivAvatar
        layout.endToEnd = R.id.sivAvatar
        layout.topToBottom = R.id.sivAvatar
        layout.topMargin = offset
        mBinding.tvContent2.layoutParams = layout
    }


    /**
     * 修改图片距离左边的距离
     */
    fun changeSivAvatarPosition() {
        val layout = LayoutParams(
            LayoutParams.WRAP_CONTENT,
            LayoutParams.WRAP_CONTENT
        )
        layout.startToStart = R.id.cl_main
        layout.endToEnd = R.id.cl_main
        layout.topToTop = R.id.cl_main
        mBinding.sivAvatar.layoutParams = layout
    }
}