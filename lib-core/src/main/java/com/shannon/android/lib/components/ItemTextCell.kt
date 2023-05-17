package com.shannon.android.lib.components

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.drawable.Drawable
import android.text.TextUtils
import android.util.AttributeSet
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.ColorInt
import androidx.annotation.DrawableRes
import androidx.appcompat.content.res.AppCompatResources
import com.shannon.android.lib.R

/**
 *
 * @ProjectName:    AIFun
 * @Package:        com.shannon.common.ui.widgets
 * @ClassName:      ItemTextCell
 * @Description:     java类作用描述
 * @Author:         czhen
 * @CreateDate:     2021/3/19 18:56
 *
 *     <com.shannon.common.components.widgets.ItemTextCell
 *           android:id="@+id/itemCell"
 *           android:layout_width="0dp"
 *           android:layout_height="wrap_content"
 *          android:layout_marginTop="@dimen/dp20"
 *          app:icon="@drawable/icon_bar_logo"
 *          app:layout_constraintEnd_toEndOf="parent"
 *          app:layout_constraintStart_toStartOf="parent"
 *          app:layout_constraintTop_toBottomOf="@id/toolbar"
 *          app:divider="true"
 *          app:title="@string/app_name" />
 *
 *      设置字体颜色、大小在themes.xml - DefaultTextCellStyle
 */
class ItemTextCell : ViewGroup {

    companion object {
        const val TAG = "ItemTextCell"
    }

    /**
     * 分割线的画笔
     */
    private val dividerPaint by lazy {
        val paint = Paint(Paint.ANTI_ALIAS_FLAG)
        paint.style = Paint.Style.FILL
        paint.strokeWidth = dp(0.5f)
        paint.color = dividerColor
        paint
    }
    private var showDivider = false
    private var dividerColor = Color.parseColor("#F0F0F0")

    /**
     * 红点标记的画笔
     */
    private val badgePaint by lazy {
        val paint = Paint(Paint.ANTI_ALIAS_FLAG)
        paint.style = Paint.Style.FILL
        paint.color = Color.RED
        paint
    }
    var showBadge = false
        set(value) {
            invalidate()
            field = value
        }

    private var badgeX = 0

    /**
     * 默认的最小高度
     */
    private val defaultMinHeight by lazy {
        dp(50F)
    }

    private val dp20 by lazy {
        dp(20F).toInt()
    }

    private val dp15 by lazy {
        dp(15F).toInt()
    }

    private var mIconView: ImageView? = null
    private var mRightIconView: ImageView? = null
    private var mTitleView: TextView? = null
    private var mSubTitleView: TextView? = null
    private var mValueView: TextView? = null

    private var contentInsert: Int = 0
    private var titleTextColor: Int? = null
    private var titleTextSize: Float? = null
    private var subtitleTextColor: Int? = null
    private var subtitleTextSize: Float? = null
    private var valueTextColor: Int? = null
    private var valueTextSize: Float? = null


    constructor(context: Context?) : this(context, null)
    constructor(context: Context?, attrs: AttributeSet?) : this(
            context,
            attrs,
            R.attr.textCellStyle
    )

    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int) : super(
            context,
            attrs,
            defStyleAttr
    ) {
        //设置最小高度
        minimumHeight = defaultMinHeight.toInt()

        setWillNotDraw(false)
        // Need to use getContext() here so that we use the themed context
        context?.apply {
            val typed = obtainStyledAttributes(attrs, R.styleable.ItemTextCell, defStyleAttr, 0)
            contentInsert =
                    typed.getDimensionPixelOffset(R.styleable.ItemTextCell_contentInset, dp15)
            badgeX = contentInsert

            if (typed.hasValue(R.styleable.ItemTextCell_icon)) {
                val icon = typed.getDrawable(R.styleable.ItemTextCell_icon)
                setItemIcon(icon)
            }

            if (typed.hasValue(R.styleable.ItemTextCell_rightIcon)) {
                val icon = typed.getDrawable(R.styleable.ItemTextCell_rightIcon)
                setItemRightIcon(icon)
            }

            val title = typed.getString(R.styleable.ItemTextCell_title)
            setItemTitle(title)


            val subtitle = typed.getString(R.styleable.ItemTextCell_subtitle)
            setItemSubTitle(subtitle)

            val value = typed.getString(R.styleable.ItemTextCell_valueText)
            setItemValue(value)

            showDivider = typed.getBoolean(R.styleable.ItemTextCell_showItemDivider, false)
            dividerColor =
                    typed.getColor(R.styleable.ItemTextCell_dividerColor, Color.parseColor("#F0F0F0"))

            showBadge = typed.getBoolean(R.styleable.ItemTextCell_showBadge, false)

            if (typed.hasValue(R.styleable.ItemTextCell_titleTextColor)) {
                setTitleTextColor(
                        typed.getColor(
                                R.styleable.ItemTextCell_titleTextColor,
                                Color.BLACK
                        )
                )
            }
            if (typed.hasValue(R.styleable.ItemTextCell_titleTextSize)) {
                val dimensionPixelSize = typed.getDimension(
                        R.styleable.ItemTextCell_titleTextSize,
                        12F
                )
                setTitleTextSize(
                        sp(dimensionPixelSize)
                )
            }

            if (typed.hasValue(R.styleable.ItemTextCell_subtitleTextColor)) {
                setSubtitleTextColor(
                        typed.getColor(
                                R.styleable.ItemTextCell_subtitleTextColor,
                                Color.BLACK
                        )
                )
            }
            if (typed.hasValue(R.styleable.ItemTextCell_subtitleTextSize)) {
                val dimensionPixelSize = typed.getDimension(
                        R.styleable.ItemTextCell_subtitleTextSize,
                        12F
                )
                setSubtitleTextSize(
                        sp(dimensionPixelSize)
                )
            }

            if (typed.hasValue(R.styleable.ItemTextCell_valueTextColor)) {
                setValueTextColor(
                        typed.getColor(
                                R.styleable.ItemTextCell_valueTextColor,
                                Color.BLACK
                        )
                )
            }
            if (typed.hasValue(R.styleable.ItemTextCell_valueTextSize)) {
                val dimensionPixelSize = typed.getDimension(
                        R.styleable.ItemTextCell_valueTextSize,
                        12F
                )
                setValueTextSize(
                        sp(dimensionPixelSize)
                )
            }
            typed.recycle()
        }

    }

    override fun onDrawForeground(canvas: Canvas?) {
        super.onDrawForeground(canvas)
        canvas?.apply {
            if (showBadge) {
                drawCircle(badgeX.toFloat(), height / 2F, dp(4F), badgePaint)
            }
        }
    }


    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
        canvas?.apply {
            //绘制分割线
            if (showDivider) {
                val startX = mTitleView?.x ?: 0f
                val y = height - (dividerPaint.strokeWidth / 2f)
                drawLine(startX, y, width.toFloat(), y, dividerPaint)
            }
        }
    }


    override fun onLayout(changed: Boolean, l: Int, t: Int, r: Int, b: Int) {
        var widthUsed = contentInsert
        var heightUsed = 0
        val height = b - t
        val width = r - l

        badgeX = width - contentInsert

        if (shouldLayout(mIconView)) {
            mIconView?.let { iconView ->
                val top = height / 2 - iconView.measuredHeight / 2
                val right = widthUsed + iconView.measuredWidth
                val lp = iconView.layoutParams as MarginLayoutParams
                iconView.layout(
                        widthUsed,
                        top,
                        right,
                        top + iconView.measuredHeight
                )

                widthUsed = (right + lp.rightMargin)
            }
        }
        if (shouldLayout(mTitleView)) {
            mTitleView?.let { titleView ->
                var top = if (shouldLayout(mSubTitleView)) {
                    height / 2 - (titleView.measuredHeight + mSubTitleView!!.measuredHeight) / 2
                } else {
                    height / 2 - titleView.measuredHeight / 2
                }.also {
                    it + heightUsed
                }
                val titleLayoutRight = widthUsed + titleView.measuredWidth
                val bottom = top + titleView.measuredHeight
                titleView.layout(
                        widthUsed,
                        top,
                        titleLayoutRight,
                        bottom
                )
                heightUsed += bottom
            }
        }
        if (shouldLayout(mSubTitleView)) {
            mSubTitleView?.let { subTitleView ->
                val titleLayoutRight = widthUsed + subTitleView.measuredWidth
                subTitleView.layout(
                        widthUsed,
                        heightUsed,
                        titleLayoutRight,
                        heightUsed + subTitleView.measuredHeight
                )
            }
        }

        var rightIconLayoutLeft = 0
        if (shouldLayout(mRightIconView)) {
            mRightIconView?.let { rightIconView ->
                val top = height / 2 - rightIconView.measuredHeight / 2
                val right = width - contentInsert
                rightIconLayoutLeft = right - rightIconView.measuredWidth
                badgeX = rightIconLayoutLeft - contentInsert
                rightIconView.layout(
                        rightIconLayoutLeft,
                        top,
                        right,
                        top + rightIconView.measuredHeight
                )
            }
        }

        if (shouldLayout(mValueView)) {
            mValueView?.let { valueView ->
                val lp = valueView.layoutParams as MarginLayoutParams
                val top = height / 2 - valueView.measuredHeight / 2
                val right = rightIconLayoutLeft - lp.rightMargin
                badgeX = right
                valueView.layout(
                        right - valueView.measuredWidth,
                        top,
                        right,
                        top + valueView.measuredHeight
                )
            }
        }
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        var measureHeight = 0
        var widthUsed = 0
        var heightUsed = 0

        if (shouldLayout(mIconView)) {
            mIconView?.let { iconView ->
                measureChildWithMargins(
                        iconView,
                        widthMeasureSpec,
                        widthUsed,
                        heightMeasureSpec,
                        heightUsed
                )
                val lp = iconView.layoutParams as MarginLayoutParams
                widthUsed += (iconView.measuredWidth + lp.rightMargin)
            }
        }

        if (shouldLayout(mRightIconView)) {
            mRightIconView?.let { iconView ->
                measureChildWithMargins(
                        iconView,
                        widthMeasureSpec,
                        widthUsed,
                        heightMeasureSpec,
                        heightUsed
                )
                val lp = iconView.layoutParams as MarginLayoutParams
                widthUsed += (iconView.measuredWidth + lp.rightMargin)
            }
        }

        if (shouldLayout(mValueView)) {
            mValueView?.let { valueView ->
                measureChildWithMargins(
                        valueView,
                        widthMeasureSpec,
                        widthUsed,
                        heightMeasureSpec,
                        heightUsed
                )
                val lp = valueView.layoutParams as MarginLayoutParams
                widthUsed += (valueView.measuredWidth + lp.rightMargin)
            }
        }

        if (shouldLayout(mTitleView)) {
            mTitleView?.let { titleView ->
                measureChildWithMargins(
                        titleView,
                        widthMeasureSpec,
                        widthUsed,
                        heightMeasureSpec,
                        0
                )
                measureHeight += titleView.measuredHeight
            }
        }

        if (shouldLayout(mSubTitleView)) {
            mSubTitleView?.let { subTitleView ->
                measureChildWithMargins(
                        subTitleView,
                        widthMeasureSpec,
                        widthUsed,
                        heightMeasureSpec,
                        0
                )
                measureHeight += subTitleView.measuredHeight
            }
        }
        //加上上下的间距
        measureHeight += dp(10f).toInt()
        setMeasuredDimension(
                widthMeasureSpec,
                resolveSizeAndState(
                        Math.max(suggestedMinimumHeight, measureHeight),
                        heightMeasureSpec,
                        0
                )
        )
    }

    fun setItemIcon(@DrawableRes resId: Int) {
        setItemIcon(AppCompatResources.getDrawable(context, resId))
    }

    fun setItemIcon(icon: Drawable?) {
        if (icon != null) {
            if (mIconView == null) {
                mIconView = makeIconView()
            }
            mIconView?.let { iconView ->
                if (!isChild(iconView)) {
                    addView(iconView)
                }
                iconView.setImageDrawable(icon)
            }
        } else if (mIconView != null) {
            removeView(mIconView)
        }
    }

    private fun makeIconView(): ImageView {
        return ImageView(context).apply {
            scaleType = ImageView.ScaleType.CENTER_CROP
            val defaultParams = generateDefaultLayoutParams()
            defaultParams.rightMargin = dp15
            layoutParams = defaultParams
        }
    }

    fun setItemTitle(title: CharSequence?) {
        if (!TextUtils.isEmpty(title)) {
            if (mTitleView == null) {
                mTitleView = makeTitleView()
            }
            mTitleView?.let { titleView ->
                if (!isChild(titleView)) {
                    addView(titleView)
                }
                titleView.text = title
            }
        } else if (mTitleView != null) {
            removeView(mTitleView)
        }
    }

    private fun makeTitleView(): TextView {
        return TextView(context).apply {
            titleTextColor?.let {
                setTextColor(it)
            }
            titleTextSize?.let {
                textSize = it
            }
            isSingleLine = true
            ellipsize = TextUtils.TruncateAt.END
            val defaultParams = generateDefaultLayoutParams()
            defaultParams.rightMargin = contentInsert
            layoutParams = defaultParams
        }
    }

    fun setTitleTextColor(@ColorInt color: Int) {
        titleTextColor = color
        mTitleView?.setTextColor(color)
    }

    fun setTitleTextSize(textSize: Float) {
        titleTextSize = textSize
        mTitleView?.textSize = textSize
    }


    fun setItemSubTitle(subTitle: CharSequence?) {
        if (!TextUtils.isEmpty(subTitle)) {
            if (mSubTitleView == null) {
                mSubTitleView = makeSubTitleView()
            }
            mSubTitleView?.let { subTitleView ->
                if (!isChild(subTitleView)) {
                    addView(subTitleView)
                }
                subTitleView.text = subTitle
            }
        } else if (mSubTitleView != null) {
            removeView(mSubTitleView)
        }
    }

    private fun makeSubTitleView(): TextView {
        return TextView(context).apply {
            subtitleTextColor?.let {
                setTextColor(it)
            }
            subtitleTextSize?.let {
                textSize = it
            }
            val defaultParams = generateDefaultLayoutParams()
            defaultParams.rightMargin = contentInsert
            layoutParams = defaultParams
        }
    }

    fun setSubtitleTextColor(@ColorInt color: Int) {
        subtitleTextColor = color
        mSubTitleView?.setTextColor(color)
    }

    fun setSubtitleTextSize(textSize: Float) {
        subtitleTextSize = textSize
        mSubTitleView?.textSize = textSize
    }

    fun setItemValue(value: CharSequence?) {
        if (!TextUtils.isEmpty(value)) {
            if (mValueView == null) {
                mValueView = makeValueView()
            }
            mValueView?.let { valueView ->
                if (!isChild(valueView)) {
                    addView(valueView)
                }
                valueView.text = value
            }
        } else if (mValueView != null) {
            removeView(mValueView)
        }
    }

    private fun makeValueView(): TextView {
        return TextView(context).apply {
            isSingleLine = true
            valueTextColor?.let {
                setTextColor(it)
            }
            valueTextSize?.let {
                textSize = it
            }
            val defaultParams = generateDefaultLayoutParams()
//            defaultParams.rightMargin = contentInsert
            layoutParams = defaultParams
        }
    }

    fun setValueTextColor(@ColorInt color: Int) {
        valueTextColor = color
        mValueView?.setTextColor(color)
    }

    fun setValueTextSize(textSize: Float) {
        valueTextSize = textSize
        mValueView?.textSize = textSize
    }

    fun setItemRightIcon(@DrawableRes resId: Int) {
        setItemRightIcon(AppCompatResources.getDrawable(context, resId))
    }

    fun setItemRightIcon(icon: Drawable?) {
        if (icon != null) {
            if (mRightIconView == null) {
                mRightIconView = makeRightIconView()
            }
            mRightIconView?.let { iconView ->
                if (!isChild(iconView)) {
                    addView(iconView)
                }
                iconView.setImageDrawable(icon)
            }
        } else if (mRightIconView != null) {
            removeView(mRightIconView)
        }
    }

    private fun makeRightIconView(): ImageView {
        return ImageView(context).apply {
            scaleType = ImageView.ScaleType.CENTER_CROP
            val defaultParams = generateDefaultLayoutParams()
            layoutParams = defaultParams
            isEnabled = false
        }
    }

    fun setRightIconClickListener(listener: OnClickListener) {
        mRightIconView?.let {
            it.isEnabled = true
            it.setOnClickListener(listener)
        }
    }

    fun setRightIconEnabled(isEnabled: Boolean) {
        mRightIconView?.let {
            it.isEnabled = isEnabled
        }
    }

    override fun measureChildWithMargins(
            child: View,
            parentWidthMeasureSpec: Int,
            widthUsed: Int,
            parentHeightMeasureSpec: Int,
            heightUsed: Int
    ) {
        val lp = child.layoutParams as MarginLayoutParams

        val childWidthMeasureSpec = getChildMeasureSpec(
                parentWidthMeasureSpec,
                (contentInsert * 2) + lp.leftMargin + lp.rightMargin
                        + widthUsed, lp.width
        )
        val childHeightMeasureSpec = getChildMeasureSpec(
                parentHeightMeasureSpec,
                (lp.topMargin + lp.bottomMargin
                        + heightUsed), lp.height
        )

        child.measure(childWidthMeasureSpec, childHeightMeasureSpec)
    }

    private fun shouldLayout(view: View?): Boolean {
        return view != null && view.parent == this && view.visibility != GONE
    }

    private fun isChild(child: View): Boolean {
        return child.parent === this
    }


    override fun generateLayoutParams(attrs: AttributeSet?): LayoutParams {
        return LayoutParams(context, attrs)
    }


    override fun generateDefaultLayoutParams(): LayoutParams {
        return LayoutParams(
                LayoutParams.WRAP_CONTENT,
                LayoutParams.WRAP_CONTENT
        )
    }

    private fun dp(px: Float): Float {
        val scale = context.resources.displayMetrics.density
        return (px * scale + 0.5f)
    }

    private fun sp(px: Float): Float {
        val scale = context.resources.displayMetrics.scaledDensity
        return (px / scale + 0.5f)
    }

    class LayoutParams : MarginLayoutParams {


        constructor(c: Context?, attrs: AttributeSet?) : super(c, attrs)
        constructor(width: Int, height: Int) : super(width, height)


        companion object {
            /**
             * Special value for the height or width requested by a View.
             * MATCH_PARENT means that the view wants to be as big as its parent,
             * minus the parent's padding, if any. Introduced in API Level 8.
             */
            const val MATCH_PARENT = -1

            /**
             * Special value for the height or width requested by a View.
             * WRAP_CONTENT means that the view wants to be just large enough to fit
             * its own internal content, taking its own padding into account.
             */
            const val WRAP_CONTENT = -2
        }
    }
}