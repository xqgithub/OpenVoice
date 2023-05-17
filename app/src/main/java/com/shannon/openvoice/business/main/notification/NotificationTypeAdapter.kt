package com.shannon.openvoice.business.main.notification

import android.content.Context
import android.graphics.Color
import androidx.core.content.ContextCompat
import com.shannon.android.lib.base.adapter.BaseFunBindingRecyclerViewAdapter
import com.shannon.openvoice.R
import com.shannon.openvoice.databinding.AdapterNotificationTypeBinding
import com.shannon.openvoice.model.NotificationTypeSelectedBean
import me.jingbin.library.adapter.BaseByViewHolder

/**
 * Date:2022/9/2
 * Time:14:55
 * author:dimple
 *  通知类型 适配器
 */
class NotificationTypeAdapter(val mContext: Context) :
    BaseFunBindingRecyclerViewAdapter<NotificationTypeSelectedBean, AdapterNotificationTypeBinding>() {

    override fun bindView(
        holder: BaseByViewHolder<NotificationTypeSelectedBean>,
        binding: AdapterNotificationTypeBinding,
        bean: NotificationTypeSelectedBean,
        position: Int
    ) {
        binding.apply {
//            tvName.text = bean.typeDisplay
//            ivSelectionBox.setImageDrawable(
//                ContextCompat.getDrawable(
//                    mContext,
//                    if (bean.isTypeSelected) R.drawable.ic_opv_signup_on else R.drawable.ic_opv_signup_off
//                )
//            )

            tsbNotificationType.apply {
                setTvContentData(
                    content = bean.typeDisplay,
                    _textSize = 16f,
                    _textColorInt = R.color.white
                )


                val slideButton = setSlideButton(
                    strokeCheckedSolidColor = Color.parseColor("#7FF7EC"),
                    strokeNoCheckedSolidColor = Color.parseColor("#CED3DE"),
                    circleCheckedColor = Color.parseColor("#FFFFFFFF"),
                    circleNoCheckedColor = Color.parseColor("#FFFFFFFF")
                ) { select_state ->
                    bean.isTypeSelected = select_state
                }
                slideButton.setChecked(bean.isTypeSelected)
            }
//            holder.addOnClickListener(R.id.cl_main)
        }
    }

    override fun bindView(
        binding: AdapterNotificationTypeBinding,
        bean: NotificationTypeSelectedBean,
        position: Int
    ) {
    }


}