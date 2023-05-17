package com.shannon.openvoice.business.creation.models

import android.content.Context
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import com.shannon.android.lib.base.adapter.BaseFunBindingRecyclerViewAdapter
import com.shannon.android.lib.extended.*
import com.shannon.android.lib.util.ThemeUtil
import com.shannon.openvoice.R
import com.shannon.openvoice.business.main.trade.TimeAdapter
import com.shannon.openvoice.databinding.ItemTimeBinding
import com.shannon.openvoice.model.OfficalModelLableBean
import me.jingbin.library.adapter.BaseByViewHolder

/**
 * Date:2022/11/4
 * Time:14:43
 * author:dimple
 * 官方模型标签适配器
 */
class OfficalModelLabelAdapter(val mContext: Context, val dataList: List<OfficalModelLableBean>) :
    BaseAdapter() {

    private var selectedCurrency = dataList[0]

    fun setSelected(position: Int) {
        selectedCurrency = getItem(position)
        notifyDataSetChanged()
    }

    override fun getCount(): Int {
        return dataList.size
    }

    override fun getItem(position: Int): OfficalModelLableBean {
        return dataList[position]
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        var rootView = convertView
        val holder: CurrencyViewHolder?
        if (rootView == null) {
            holder = CurrencyViewHolder(parent!!)
            rootView = holder.binding.root
            rootView.tag = holder
        } else {
            holder = rootView.tag as CurrencyViewHolder
        }

        holder.binding.run {
            timeView.text = dataList[position].name
            val isSelected = getItem(position) == selectedCurrency
            if (isSelected) {
                val topLeftCorner: Float = if (position == 0) 2.0f.dp else 0f
                val bottomLeftCorner: Float = if (position == dataList.lastIndex) 2.0f.dp else 0f
                val topRightCorner: Float = if (position == 0) 2.0f.dp else 0f
                val bottomRightCorner: Float = if (position == dataList.lastIndex) 2.0f.dp else 0f
                rootView.newShapeDrawable(
                    R.color.color_737AAE,
                    topLeftCorner,
                    bottomLeftCorner,
                    topRightCorner,
                    bottomRightCorner
                )
            } else {
                rootView.background = null
            }
            dividingLine.visibility(position != (count - 1))
        }
        return rootView
    }


    private class CurrencyViewHolder(parent: ViewGroup) {
        val binding = inflateBinding<ItemTimeBinding>(parent)
    }
}