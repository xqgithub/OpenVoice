package com.shannon.openvoice.components

import android.annotation.SuppressLint
import android.content.Context
import android.text.DynamicLayout
import android.text.StaticLayout
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatTextView
import com.google.android.material.shape.Shapeable
import com.google.android.material.theme.overlay.MaterialThemeOverlay
import com.shannon.android.lib.R
import com.shannon.android.lib.components.ShapeableTextView
import java.lang.reflect.Field

/**
 * Date:2023/3/23
 * Time:10:13
 * author:dimple
 * 解决 用Spannable后,末尾省略号不显示的问题
 */
class TextViewSpannable : AppCompatTextView {

    companion object {
        private val DEF_STYLE_RES = R.style.Widget_Material_ShapeableButton
    }


    constructor(context: Context) : this(context, null, 0)
    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)

    @SuppressLint("RestrictedApi")
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(
        MaterialThemeOverlay.wrap(context, attrs, defStyleAttr, DEF_STYLE_RES),
        attrs,
        defStyleAttr
    )


    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        var layout: StaticLayout? = null
        var field: Field? = null
        try {
            val staticField = DynamicLayout::class.java!!.getDeclaredField("sStaticLayout")
            staticField.setAccessible(true)
            layout = staticField.get(DynamicLayout::class.java) as StaticLayout
        } catch (e: NoSuchFieldException) {
            e.printStackTrace()
        } catch (e: IllegalAccessException) {
            e.printStackTrace()
        }
        if (layout != null) {
            try {
                field = StaticLayout::class.java!!.getDeclaredField("mMaximumVisibleLineCount")
                field!!.isAccessible = true
                field.setInt(layout, maxLines)
            } catch (e: NoSuchFieldException) {
                e.printStackTrace()
            } catch (e: IllegalAccessException) {
                e.printStackTrace()
            }
        }
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        if (layout != null && field != null) {
            try {
                field.setInt(layout, Integer.MAX_VALUE)
            } catch (e: IllegalAccessException) {
                e.printStackTrace()
            }
        }
    }

}