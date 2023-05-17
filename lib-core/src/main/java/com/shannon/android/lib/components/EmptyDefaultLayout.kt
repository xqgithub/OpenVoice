package com.shannon.android.lib.components

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.constraintlayout.widget.ConstraintLayout
import com.shannon.android.lib.R
import com.shannon.android.lib.extended.dp
import com.shannon.android.lib.util.ThemeUtil

/**
 *
 * @ClassName:      EmptyDefaultLayout
 * @Description:    列表无数据时的占位UI
 * @Author:         czhen
 *
 *     <com.shannon.aifun.components.widgets.EmptyDefaultLayout
 *           android:id="@+id/defaultLayout"
 *           android:layout_width="0dp"
 *           android:layout_height="0dp"
 *           app:layout_constraintBottom_toBottomOf="parent"
 *           app:layout_constraintEnd_toEndOf="parent"
 *           app:layout_constraintStart_toStartOf="parent"
 *           app:layout_constraintTop_toTopOf="parent" />
 *
 *          setImageResource(R.drawable.image_empty_search)
 *          setPromptText(R.string.recommend_prompt_text)
 *          setButtonText(R.string.to_recommend_text)
 *
 */
class EmptyDefaultLayout : ConstraintLayout {

    private lateinit var imageView: ImageView
    private lateinit var textView: TextView
    private lateinit var buttonView: Button

    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(
            context,
            attrs,
            defStyleAttr
    )

    constructor(
            context: Context,
            attrs: AttributeSet?,
            defStyleAttr: Int,
            defStyleRes: Int
    ) : super(context, attrs, defStyleAttr, defStyleRes)


    init {
        addImageView()
        addTextView()
        addButtonView()
    }

    private fun addButtonView() {
        val layoutParams = LayoutParams(200.dp, 40.dp)
        layoutParams.topToBottom = R.id.emptyTextView
        layoutParams.startToStart = LayoutParams.PARENT_ID
        layoutParams.endToEnd = LayoutParams.PARENT_ID
        layoutParams.bottomToBottom = LayoutParams.PARENT_ID
        layoutParams.topMargin = 15.dp

        buttonView =  createButtonView()
        buttonView.visibility = View.INVISIBLE
        addView(buttonView, layoutParams)
    }

    private fun addTextView() {
        val layoutParams = LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT)
        layoutParams.topToBottom = R.id.emptyImageView
        layoutParams.startToStart = LayoutParams.PARENT_ID
        layoutParams.endToEnd = LayoutParams.PARENT_ID
        layoutParams.bottomToTop = R.id.emptyButtonView
        layoutParams.topMargin = 4.dp

        textView = createTextView()
        addView(textView, layoutParams)
    }

    private fun addImageView() {
        val layoutParams = LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT)
        layoutParams.topToTop = LayoutParams.PARENT_ID
        layoutParams.startToStart = LayoutParams.PARENT_ID
        layoutParams.endToEnd = LayoutParams.PARENT_ID
        layoutParams.bottomToTop = R.id.emptyTextView
        layoutParams.verticalChainStyle = LayoutParams.CHAIN_PACKED

        imageView = createImageView()
        addView(imageView, layoutParams)
    }

    private fun createImageView(): ImageView {
        return ImageView(context).apply {
            id = R.id.emptyImageView
            scaleType = ImageView.ScaleType.CENTER_CROP
        }
    }

    private fun createTextView(): TextView {
        return TextView(context).apply {
            id = R.id.emptyTextView
            textSize = 12F
            setTextColor(ThemeUtil.getColor(context, R.attr.emptyLayoutButtonTextColor))
        }
    }

    private fun createButtonView(): Button {
        return Button(context, null, 0, R.style.S_Button).apply {
            id = R.id.emptyButtonView
        }
    }


    /**
     * 设置占位图片
     * @param resId Int
     */
    fun setImageResource(@DrawableRes resId: Int) {
        imageView.setImageResource(resId)
    }

    /**
     * 设置提示文本
     * @param resId Int
     */
    fun setPromptText(@StringRes resId: Int) {
        textView.setText(resId)
    }

    /**
     * 设置按钮文本
     * @param resId Int
     */
    fun setButtonText(@StringRes resId: Int) {
        if (resId != 0) {
            buttonView.visibility = View.VISIBLE
            buttonView.setText(resId)
        }
    }

    fun setButtonClick(click: () -> Unit) {
        buttonView.setOnClickListener { click() }
    }

}