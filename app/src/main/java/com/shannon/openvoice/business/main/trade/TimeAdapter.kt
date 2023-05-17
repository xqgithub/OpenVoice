package com.shannon.openvoice.business.main.trade

import android.content.Context
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import com.shannon.android.lib.extended.dp
import com.shannon.android.lib.extended.inflateBinding
import com.shannon.android.lib.extended.newShapeDrawable
import com.shannon.android.lib.extended.visibility
import com.shannon.openvoice.R
import com.shannon.openvoice.databinding.ItemTimeBinding

/**
 *
 * @Package:        com.shannon.openvoice.business.main.trade
 * @ClassName:      TimeAdapter
 * @Description:     作用描述
 * @Author:         czhen
 * @CreateDate:     2022/8/31 17:53
 */
class TimeAdapter(val context: Context, private val dataList: List<Int>) : BaseAdapter() {

    private var selectedCurrency = dataList[0]

    fun setSelected(position: Int) {
        selectedCurrency = getItem(position)
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
            timeView.text = context.getString(getItem(position))
            val isSelected = getItem(position) == selectedCurrency
            if (isSelected) {
                val topLeftCorner: Float = if (position == 0) 8.0f.dp else 0f
                val bottomLeftCorner: Float = if (position == dataList.lastIndex) 8.0f.dp else 0f
                val topRightCorner: Float = if (position == 0) 8.0f.dp else 0f
                val bottomRightCorner: Float = if (position == dataList.lastIndex) 8.0f.dp else 0f
                rootView.newShapeDrawable(
                    R.color.color_111A19,
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


    override fun areAllItemsEnabled() = false


    private class CurrencyViewHolder(parent: ViewGroup) {
        val binding = inflateBinding<ItemTimeBinding>(parent)
    }
}