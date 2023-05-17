package com.shannon.openvoice.components

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import androidx.core.content.ContextCompat
import at.connyduck.sparkbutton.helpers.Utils
import com.google.android.material.imageview.ShapeableImageView
import com.shannon.android.lib.extended.dp
import com.shannon.android.lib.util.ThemeUtil
import com.shannon.openvoice.R

/**
 *
 * @Package:        com.shannon.openvoice.components
 * @ClassName:      ProgressImageView
 * @Description:     作用描述
 * @Author:         czhen
 * @CreateDate:     2022/8/11 13:53
 */
class ProgressImageView : ShapeableImageView {

    private var progress = -1
    private val progressRect = RectF()
    private val biggerRect = RectF()
    private val circlePaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val clearPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val markBgPaint = Paint(Paint.ANTI_ALIAS_FLAG)

    constructor(context: Context?) : super(context)
    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs)
    constructor(context: Context?, attrs: AttributeSet?, defStyle: Int) : super(
        context,
        attrs,
        defStyle
    )

    init {
        circlePaint.color = ThemeUtil.getColor(context, android.R.attr.colorPrimary)
        circlePaint.strokeWidth = 4F.dp
        circlePaint.style = Paint.Style.STROKE

        clearPaint.xfermode = PorterDuffXfermode(PorterDuff.Mode.DST_OUT)

        markBgPaint.style = Paint.Style.FILL
        markBgPaint.color = ContextCompat.getColor(
            context,
            R.color.color_16191F
        )
    }

    fun setProgress(progress: Int) {
        this.progress = progress
        if (progress != -1) {
            setColorFilter(Color.rgb(123, 123, 123), PorterDuff.Mode.MULTIPLY)
        } else {
            clearColorFilter()
        }
        invalidate()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val angle = progress / 100f * 360 - 90
        val halfWidth = (width / 2).toFloat()
        val halfHeight = (height / 2).toFloat()
        progressRect[halfWidth * 0.75f, halfHeight * 0.75f, halfWidth * 1.25f] = halfHeight * 1.25f
        biggerRect.set(progressRect)
        val margin = 8
        biggerRect[progressRect.left - margin, progressRect.top - margin, progressRect.right + margin] =
            progressRect.bottom + margin
        canvas.saveLayer(biggerRect, null, Canvas.ALL_SAVE_FLAG)
        if (progress != -1) {
            canvas.drawOval(progressRect, circlePaint)
            canvas.drawArc(biggerRect, angle, 360 - angle - 90, true, clearPaint)
        }
        canvas.restore()
    }
}