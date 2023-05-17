package com.shannon.openvoice.business.player.ui

import android.content.Context
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import com.shannon.android.lib.extended.dp
import com.shannon.android.lib.extended.inflateBinding
import com.shannon.android.lib.extended.newShapeDrawable
import com.shannon.android.lib.extended.visibility
import com.shannon.openvoice.R
import com.shannon.openvoice.databinding.ItemSpeedBinding
import com.shannon.openvoice.databinding.ItemTimeBinding

/**
 *
 * @Package:        com.shannon.openvoice.business.main.trade
 * @ClassName:      SpeedAdapter
 * @Description:     作用描述
 * @Author:         czhen
 * @CreateDate:     2022/8/31 17:53
 */
class SpeedAdapter(val context: Context, private val dataList: List<String>) : BaseAdapter() {

    private var selectedPosition = 2

    fun setSelected(position: Int) {
        selectedPosition = position
        notifyDataSetChanged()
    }

    override fun getCount() = dataList.size

    override fun getItem(position: Int) = dataList[position]

    override fun getItemId(position: Int) = position.toLong()

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
            speedView.text = getItem(position)
            val isSelected = position == selectedPosition
            speedView.isSelected = isSelected
            dividingLine.visibility(position != (count - 1))
        }

        return rootView
    }


    override fun areAllItemsEnabled() = false


    private class CurrencyViewHolder(parent: ViewGroup) {
        val binding = inflateBinding<ItemSpeedBinding>(parent)
    }
}