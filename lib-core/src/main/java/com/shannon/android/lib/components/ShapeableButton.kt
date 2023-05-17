package com.shannon.android.lib.components

import android.annotation.SuppressLint
import android.content.Context
import android.content.res.ColorStateList
import android.graphics.*
import android.util.AttributeSet
import android.view.Gravity
import androidx.appcompat.widget.AppCompatButton
import com.google.android.material.resources.MaterialResources
import com.google.android.material.shape.ShapeAppearanceModel
import com.google.android.material.shape.ShapeAppearancePathProvider
import com.google.android.material.shape.Shapeable
import com.google.android.material.theme.overlay.MaterialThemeOverlay
import com.shannon.android.lib.R

/**
 *
 * @ProjectName:    OpenVoice
 * @Package:        com.shannon.android.lib.components
 * @ClassName:      ShapeableButton
 * @Description:     java类作用描述
 * @Author:         czhen
 * @CreateDate:     2022/7/28 17:26
 */
class ShapeableButton : AppCompatButton, Shapeable {

    companion object {
        private val DEF_STYLE_RES = R.style.Widget_Material_ShapeableButton
    }

    private var shapeAppearanceModel: ShapeAppearanceModel
    private var hasAdjustedPaddingAfterLayoutDirectionResolved = false

    @SuppressLint("RestrictedApi")
    private val pathProvider = ShapeAppearancePathProvider.getInstance()
    private val destination: RectF = RectF()
    private val maskRect: RectF = RectF()
    private val clearPaint = Paint()
    private val maskPath = Path()
    private val path = Path()

    private val borderPaint: Paint = Paint()
    private var strokeColor: ColorStateList? = null
    private var strokeWidth = 0f

    constructor(context: Context) : this(context, null, 0)
    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)

    @SuppressLint("RestrictedApi")
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(
        MaterialThemeOverlay.wrap(context, attrs, defStyleAttr, DEF_STYLE_RES),
        attrs,
        defStyleAttr
    ) {
        gravity = Gravity.CENTER
        clearPaint.isAntiAlias = true
        clearPaint.color = Color.WHITE
        clearPaint.xfermode = PorterDuffXfermode(PorterDuff.Mode.DST_OUT)

        val attributes = context.obtainStyledAttributes(
            attrs, R.styleable.ShapeableButton, defStyleAttr,
            DEF_STYLE_RES
        )

        strokeColor = MaterialResources.getColorStateList(
            context,
            attributes,
            R.styleable.ShapeableButton_strokeColor
        )

        strokeWidth = attributes.getDimensionPixelSize(R.styleable.ShapeableButton_strokeWidth, 0).toFloat()
        attributes.recycle()

        borderPaint.style = Paint.Style.STROKE
        borderPaint.isAntiAlias = true

        shapeAppearanceModel = ShapeAppearanceModel.builder(
            context, attrs, defStyleAttr,
            DEF_STYLE_RES
        ).build()
    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
        canvas!!.drawPath(maskPath, clearPaint)
        drawStroke(canvas)
    }

    override fun onDetachedFromWindow() {
        setLayerType(LAYER_TYPE_NONE, null)
        super.onDetachedFromWindow()
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        setLayerType(LAYER_TYPE_HARDWARE, null)
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        if (hasAdjustedPaddingAfterLayoutDirectionResolved) return

        if (!isLayoutDirectionResolved) return

        hasAdjustedPaddingAfterLayoutDirectionResolved = true


        // Update the super padding to be the combined `android:padding` and
        // `app:contentPadding`, keeping with ShapeableImageView's internal padding contract:
        if ((isPaddingRelative || isContentPaddingRelative())) {
            setPaddingRelative(
                super.getPaddingStart(),
                super.getPaddingTop(),
                super.getPaddingEnd(),
                super.getPaddingBottom()
            )
            return
        }

        setPadding(
            super.getPaddingLeft(),
            super.getPaddingTop(),
            super.getPaddingRight(),
            super.getPaddingBottom()
        )
    }

    private fun isContentPaddingRelative(): Boolean {
        return false
    }

    override fun setShapeAppearanceModel(shapeAppearanceModel: ShapeAppearanceModel) {
        this.shapeAppearanceModel = shapeAppearanceModel
        updateShapeMask(width, height)
        invalidate()
        invalidateOutline()
    }

    override fun getShapeAppearanceModel(): ShapeAppearanceModel {
        return shapeAppearanceModel
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        updateShapeMask(w, h)
    }

    private fun updateShapeMask(width: Int, height: Int) {
        destination[0f, 0f, width.toFloat()] =
            height.toFloat()
        pathProvider.calculatePath(shapeAppearanceModel, 1f /*interpolation*/, destination, path)
        // Remove path from rect to draw with clear paint.
        maskPath.rewind()
        maskPath.addPath(path)
        // Do not include padding to clip the background too.
        maskRect[0f, 0f, width.toFloat()] = height.toFloat()
        maskPath.addRect(maskRect, Path.Direction.CCW)
    }

    private fun drawStroke(canvas: Canvas) {
        if (strokeColor == null) {
            return
        }
        borderPaint.strokeWidth = strokeWidth
        val colorForState =
            strokeColor!!.getColorForState(drawableState, strokeColor!!.defaultColor)
        if (strokeWidth > 0 && colorForState != Color.TRANSPARENT) {
            borderPaint.color = colorForState
            canvas.drawPath(path, borderPaint)
        }
    }
}