package com.shannon.android.lib.components

import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.drawable.Drawable
import android.text.TextUtils
import android.util.AttributeSet
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.ColorInt
import androidx.annotation.DrawableRes
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.animation.addListener
import com.shannon.android.lib.R

/**
 *
 * @ProjectName:    AIFun
 * @Package:        com.shannon.common.ui.widgets
 * @ClassName:      ItemSwitchCell
 * @Description:     java类作用描述
 * @Author:         czhen
 * @CreateDate:     2021/3/19 18:56
 */
class ItemSwitchCell : ViewGroup {

    companion object {
        const val TAG = "ItemSwitchCell"
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
     * Switch track的画笔
     */
    private val switchTrackPaint by lazy {
        val paint = Paint(Paint.ANTI_ALIAS_FLAG)
        paint.style = Paint.Style.FILL
        paint
    }

    private var switchTrackDefaultColor = Color.parseColor("#D9D9D9")
    private var switchTrackCheckedColor = Color.parseColor("#0A7EF2")
    private var switchThumbColor = Color.WHITE

    /**
     * Switch thumb的画笔
     */
    private val switchThumbPaint by lazy {
        val paint = Paint(Paint.ANTI_ALIAS_FLAG)
        paint.style = Paint.Style.FILL
        paint.color = switchThumbColor
        paint
    }
    private val switchRect = RectF()

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

    private val dp13 by lazy {
        dp(13F)
    }
    private val dp11 by lazy {
        dp(11F)
    }

    private var mIconView: ImageView? = null
    private var mTitleView: TextView? = null
    private var mSubTitleView: TextView? = null

    private var contentInsert: Int = 0
    private var titleTextColor: Int? = null
    private var titleTextSize: Float? = null
    private var subtitleTextColor: Int? = null
    private var subtitleTextSize: Float? = null

    private var onCheckChangeListener: OnCheckChangeListener? = null

    constructor(context: Context?) : this(context, null)
    constructor(context: Context?, attrs: AttributeSet?) : this(
        context,
        attrs,
        R.attr.switchCellStyle
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
            val typed = obtainStyledAttributes(attrs, R.styleable.ItemSwitchCell, defStyleAttr, 0)
            contentInsert =
                typed.getDimensionPixelOffset(R.styleable.ItemSwitchCell_contentInset, dp15)

            if (typed.hasValue(R.styleable.ItemSwitchCell_icon)) {
                val icon = typed.getDrawable(R.styleable.ItemSwitchCell_icon)
                setItemIcon(icon)
            }

            val title = typed.getString(R.styleable.ItemSwitchCell_title)
            setItemTitle(title)


            val subtitle = typed.getString(R.styleable.ItemSwitchCell_subtitle)
            setItemSubTitle(subtitle)

            if (typed.hasValue(R.styleable.ItemSwitchCell_trackDefaultColor)) {
                switchTrackDefaultColor = typed.getColor(
                    R.styleable.ItemSwitchCell_trackDefaultColor,
                    switchTrackDefaultColor
                )
            }
            if (typed.hasValue(R.styleable.ItemSwitchCell_trackCheckedColor)) {
                switchTrackCheckedColor = typed.getColor(
                    R.styleable.ItemSwitchCell_trackCheckedColor,
                    switchTrackCheckedColor
                )
            }
            if (typed.hasValue(R.styleable.ItemSwitchCell_thumbColor)) {
                switchThumbColor =
                    typed.getColor(R.styleable.ItemSwitchCell_thumbColor, switchThumbColor)
            }

            showDivider = typed.getBoolean(R.styleable.ItemSwitchCell_showItemDivider, false)
            dividerColor =
                typed.getColor(R.styleable.ItemSwitchCell_dividerColor, Color.parseColor("#F0F0F0"))

            if (typed.hasValue(R.styleable.ItemSwitchCell_titleTextColor)) {
                setTitleTextColor(
                    typed.getColor(
                        R.styleable.ItemSwitchCell_titleTextColor,
                        Color.BLACK
                    )
                )
            }
            if (typed.hasValue(R.styleable.ItemSwitchCell_titleTextSize)) {
                val dimensionPixelSize = typed.getDimension(
                    R.styleable.ItemSwitchCell_titleTextSize,
                    12F
                )
                setTitleTextSize(
                    sp(dimensionPixelSize)
                )
            }

            if (typed.hasValue(R.styleable.ItemSwitchCell_subtitleTextColor)) {
                setSubtitleTextColor(
                    typed.getColor(
                        R.styleable.ItemSwitchCell_subtitleTextColor,
                        Color.BLACK
                    )
                )
            }
            if (typed.hasValue(R.styleable.ItemSwitchCell_subtitleTextSize)) {
                val dimensionPixelSize = typed.getDimension(
                    R.styleable.ItemSwitchCell_subtitleTextSize,
                    12F
                )
                setSubtitleTextSize(
                    sp(dimensionPixelSize)
                )
            }

            typed.recycle()
        }

        setOnClickListener {
            if (switchThumbOffsetProgress == 0f || animatorNormal?.isStarted == true) {
                animatorCheck()
            } else if (switchThumbOffsetProgress == 1f || animatorCheck?.isStarted == true) {
                animatorNormal()
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

            switchTrackPaint.color = evaluate(
                switchThumbOffsetProgress,
                switchTrackDefaultColor,
                switchTrackCheckedColor
            )
            drawRoundRect(switchRect, dp13, dp13, switchTrackPaint)
            drawCircle(
                switchRect.left + dp13 + (switchThumbOffset * switchThumbOffsetProgress),
                switchRect.bottom - dp13,
                dp11,
                switchThumbPaint
            )
        }
    }

    private var switchThumbOffsetProgress = 0.0F
    private val switchThumbOffset by lazy {
        switchRect.right - switchRect.left - (dp(13F) * 2)
    }
    private var animatorCheck: ValueAnimator? = null
    private var animatorNormal: ValueAnimator? = null
    var isChecked = false
        set(value) {
            switchThumbOffsetProgress = if (value) 1F else 0F
            invalidate()
            field = value
        }

    private fun animatorCheck() {
        animatorNormal?.apply {
            if (isStarted) {
                cancel()
            }
        }
        animatorCheck = ObjectAnimator.ofFloat(switchThumbOffsetProgress, 1F).apply {
            duration = 300
            addUpdateListener {
                switchThumbOffsetProgress = it.animatedValue as Float
                if (switchThumbOffsetProgress <= 1F)
                    invalidate()
            }
            addListener(onEnd = {
                if (switchThumbOffsetProgress == 1F) {
                    if (!isChecked) {
                        onCheckChangeListener?.apply {
                            onCheckChange(true)
                        }
                    }
                    isChecked = true
                }
            })
            start()
        }
    }

    private fun animatorNormal() {
        animatorCheck?.apply {
            if (isStarted) {
                cancel()
            }
        }
        animatorNormal = ObjectAnimator.ofFloat(switchThumbOffsetProgress, 0F).apply {
            duration = 300
            addUpdateListener {
                switchThumbOffsetProgress = it.animatedValue as Float
                if (switchThumbOffsetProgress >= 0F)
                invalidate()
            }
            addListener(onEnd = {
                if (switchThumbOffsetProgress == 0F) {
                    if (isChecked) {
                        onCheckChangeListener?.apply {
                            onCheckChange(false)
                        }
                    }
                    isChecked = false
                }
            })
            start()
        }
    }


    override fun onLayout(changed: Boolean, l: Int, t: Int, r: Int, b: Int) {
        var widthUsed = contentInsert
        var heightUsed = 0
        val height = b - t
        val width = r - l
        Log.e(TAG, "onLayout: left: $l; top: $t; right: $r; bottom: $b")
        val switchRight = r - contentInsert.toFloat()
        val switchCenterY = height / 2F
        val switchTop = switchCenterY - (dp(26F) / 2f)
        switchRect.set(
            switchRight - dp(44f).toInt(),
            switchTop,
            switchRight,
            switchTop + dp(26F).toInt()
        )

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
                Log.e(TAG, "onLayout: rightMargin = ${lp.rightMargin}")

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

    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        var measureHeight = 0
        var widthUsed = dp(50F).toInt()
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
                Log.e(
                    TAG,
                    "mIconView measuredWidth: ${iconView.measuredWidth}; measuredHeight: ${iconView.measuredHeight}"
                )
            }
        }

        Log.e(TAG, "widthUsed: $widthUsed")

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
                Log.e(
                    TAG,
                    "mTitleView measuredWidth: ${titleView.measuredWidth}; measuredHeight: ${titleView.measuredHeight}"
                )
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
                Log.e(
                    TAG,
                    "mSubTitleView measuredWidth: ${subTitleView.measuredWidth}; measuredHeight: ${subTitleView.measuredHeight}"
                )
            }
        }
        //加上上下的间距
        measureHeight +=  dp(10f).toInt()
        Log.e(TAG, "measureHeight: $measureHeight")
        Log.e(TAG, "suggestedMinimumHeight: $suggestedMinimumHeight")
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
        Log.e(TAG, "setTitleTextSize: $textSize")
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
        Log.e(TAG, "setTitleTextSize: $textSize")
        subtitleTextSize = textSize
        mSubTitleView?.textSize = textSize
    }

    fun setOnCheckChangeListener(onCheckChange: (Boolean) -> Unit) {
        onCheckChangeListener = object : OnCheckChangeListener {
            override fun onCheckChange(isCheck: Boolean) {
                onCheckChange(isCheck)
            }
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

    interface OnCheckChangeListener {
        fun onCheckChange(isCheck: Boolean)
    }

    private fun evaluate(fraction: Float, startValue: Int, endValue: Int): Int {
        val startA = (startValue shr 24 and 0xff) / 255.0f
        var startR = (startValue shr 16 and 0xff) / 255.0f
        var startG = (startValue shr 8 and 0xff) / 255.0f
        var startB = (startValue and 0xff) / 255.0f
        val endA = (endValue shr 24 and 0xff) / 255.0f
        var endR = (endValue shr 16 and 0xff) / 255.0f
        var endG = (endValue shr 8 and 0xff) / 255.0f
        var endB = (endValue and 0xff) / 255.0f

        // convert from sRGB to linear
        startR = Math.pow(startR.toDouble(), 2.2).toFloat()
        startG = Math.pow(startG.toDouble(), 2.2).toFloat()
        startB = Math.pow(startB.toDouble(), 2.2).toFloat()
        endR = Math.pow(endR.toDouble(), 2.2).toFloat()
        endG = Math.pow(endG.toDouble(), 2.2).toFloat()
        endB = Math.pow(endB.toDouble(), 2.2).toFloat()

        // compute the interpolated color in linear space
        var a = startA + fraction * (endA - startA)
        var r = startR + fraction * (endR - startR)
        var g = startG + fraction * (endG - startG)
        var b = startB + fraction * (endB - startB)

        // convert back to sRGB in the [0..255] range
        a *= 255.0f
        r = Math.pow(r.toDouble(), 1.0 / 2.2).toFloat() * 255.0f
        g = Math.pow(g.toDouble(), 1.0 / 2.2).toFloat() * 255.0f
        b = Math.pow(b.toDouble(), 1.0 / 2.2).toFloat() * 255.0f
        return Math.round(a) shl 24 or (Math.round(r) shl 16) or (Math.round(g) shl 8) or Math.round(
            b
        )
    }
}