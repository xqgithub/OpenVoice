package com.shannon.android.lib.extended

import android.graphics.Paint
import android.view.View
import android.widget.Checkable
import androidx.annotation.ColorRes
import androidx.core.content.ContextCompat
import com.google.android.material.shape.CornerFamily
import com.google.android.material.shape.MaterialShapeDrawable
import com.google.android.material.shape.RoundedCornerTreatment
import com.google.android.material.shape.ShapeAppearanceModel

fun View.newShapeDrawable(@ColorRes colorId: Int, corner: Float = 2.0f.dp) {
    val shapeModel = ShapeAppearanceModel.builder()
        .setAllCorners(RoundedCornerTreatment())
        .setAllCornerSizes(corner)
        .build()
    MaterialShapeDrawable(shapeModel).apply {
        setTint(ContextCompat.getColor(context, colorId))
        paintStyle = Paint.Style.FILL
    }.also {
        background = it
    }
}

fun View.newShapeDrawable(
    @ColorRes colorId: Int,
    topLeftCorner: Float = 2.0f.dp,
    bottomLeftCorner: Float = 2.0f.dp,
    topRightCorner: Float = 2.0f.dp,
    bottomRightCorner: Float = 2.0f.dp
) {
    val shapeModel = ShapeAppearanceModel.builder()
        .setAllCorners(RoundedCornerTreatment())
        .setTopLeftCornerSize(topLeftCorner)
        .setBottomLeftCornerSize(bottomLeftCorner)
        .setTopRightCornerSize(topRightCorner)
        .setBottomRightCornerSize(bottomRightCorner)
        .build()
    MaterialShapeDrawable(shapeModel).apply {
        setTint(ContextCompat.getColor(context, colorId))
        paintStyle = Paint.Style.FILL
    }.also {
        background = it
    }
}

fun View.gone() {
    if (visibility != View.GONE)
        visibility = View.GONE
}

fun View.visible() {
    if (visibility != View.VISIBLE)
        visibility = View.VISIBLE
}

fun View.visibility(visible: Boolean, vis: Int = View.GONE) {
    visibility = if (visible) View.VISIBLE else vis
}

fun View.invisible() {
    if (visibility != View.INVISIBLE)
        visibility = View.INVISIBLE
}

inline fun <T : View> T.singleClick(crossinline onClick: (T) -> Unit) {
    singleClick(block = onClick)
}

inline fun <T : View> T.singleClick(time: Long = 400, crossinline block: (T) -> Unit) {
    setOnClickListener {
        val currentTimeMillis = System.currentTimeMillis()
        if (currentTimeMillis - lastClickTime > time || this is Checkable) {
            lastClickTime = currentTimeMillis
            block(this)
        }
    }
}

fun <T : View> T.singleClick(onClickListener: View.OnClickListener, time: Long = 400) {
    setOnClickListener {
        val currentTimeMillis = System.currentTimeMillis()
        if (currentTimeMillis - lastClickTime > time || this is Checkable) {
            lastClickTime = currentTimeMillis
            onClickListener.onClick(this)
        }
    }
}

var <T : View> T.lastClickTime: Long
    set(value) = setTag(1766613352, value)
    get() = getTag(1766613352) as? Long ?: 0
