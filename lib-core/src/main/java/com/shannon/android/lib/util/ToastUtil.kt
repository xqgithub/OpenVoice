package com.shannon.android.lib.util

import android.annotation.SuppressLint
import android.content.Context
import android.view.Gravity
import android.view.LayoutInflater
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.DrawableRes
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import com.shannon.android.lib.R
import com.shannon.android.lib.extended.dp
import com.shannon.android.lib.extended.setDynamicShapeRectangle

/**
 *
 * @ClassName:      ToastUtil
 * @Description:     java类作用描述
 * @Author:         czhen
 */
object ToastUtil {

    private lateinit var sApplicationContext: Context

    fun init(applicationContext: Context) {
        sApplicationContext = applicationContext
    }

    /**
     * 短时间显示Toast【居下】
     * @param message 显示的内容-字符串*/
    fun showToast(message: String) {
        createToast().apply {
            setGravity(Gravity.BOTTOM or Gravity.CENTER_HORIZONTAL, 0, 0)
            setText(message)
            show()
        }
    }

    /**
     * 短时间显示Toast【居中】
     * @param message 显示的内容-字符串*/
    fun showCenter(message: String, bgColor: String = "#40A4ACAC") {//#E51B1F3E，#80000000
//        createToast().apply {
//            setGravity(Gravity.CENTER, 0, 0)
//            setText(message)
//            show()
//        }
        showToastBg(content = message, position = Gravity.CENTER, bgColor = bgColor)
    }

    @SuppressLint("ShowToast")
    private fun createToast(): Toast {
        return Toast.makeText(sApplicationContext, "", Toast.LENGTH_SHORT)
    }


    /**
     * 自定义Toast成功提交
     */
    fun showCustomizeToast(
        @DrawableRes icon: Int,
        content: String = "",
        content2: String = "",
        position: Int = Gravity.BOTTOM,
        length: Int = Toast.LENGTH_SHORT,
        XOffset: Int = 0,
        yOffset: Int = 0
    ) {
        val customToast = Toast(sApplicationContext)
        val inflate =
            sApplicationContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val toastView = inflate.inflate(R.layout.layout_toast, null, false)
        val cl_main = toastView.findViewById<ConstraintLayout>(R.id.cl_main)

        setDynamicShapeRectangle(
            arrayOf(cl_main),
            CornerRadiusLeftTop = 16f.dp,
            CornerRadiusRightTop = 16f.dp,
            CornerRadiusLeftBottom = 16f.dp,
            CornerRadiusRightBottom = 16f.dp,
            bgColor = arrayOf("#E51B1F3E")
        )
        val successView = toastView.findViewById<ImageView>(R.id.iv_success)
        successView.setImageDrawable(ContextCompat.getDrawable(sApplicationContext, icon))
        val tv_content = toastView.findViewById<TextView>(R.id.tv_content)
        val tv_content2 = toastView.findViewById<TextView>(R.id.tv_content2)
        tv_content.text = content
        tv_content2.text = content2

        customToast.setGravity(position, XOffset, yOffset)
        customToast.duration = length
        customToast.view = toastView
        customToast.show()
    }

    /**
     * 自定义Toast弹出框，用户修改背景
     */
    fun showToastBg(
        content: String = "",
        position: Int = Gravity.BOTTOM,
        length: Int = Toast.LENGTH_SHORT,
        XOffset: Int = 0,
        yOffset: Int = 0,
        bgColor: String
    ) {
        val customToast = Toast(sApplicationContext)
        val inflate =
            sApplicationContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val toastView = inflate.inflate(R.layout.layout_toast2, null, false)
        val cl_main = toastView.findViewById<ConstraintLayout>(R.id.cl_main)
        setDynamicShapeRectangle(
            arrayOf(cl_main),
            CornerRadiusLeftTop = 8f.dp,
            CornerRadiusRightTop = 8f.dp,
            CornerRadiusLeftBottom = 8f.dp,
            CornerRadiusRightBottom = 8f.dp,
            bgColor = arrayOf(bgColor)
        )

        val tv_content = toastView.findViewById<TextView>(R.id.tv_content)
        tv_content.text = content

        customToast.setGravity(position, XOffset, yOffset)
        customToast.duration = length
        customToast.view = toastView
        customToast.show()
    }


}