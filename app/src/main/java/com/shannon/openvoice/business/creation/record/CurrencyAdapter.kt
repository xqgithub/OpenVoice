package com.shannon.openvoice.business.creation.record

import android.content.Context
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import com.shannon.android.lib.extended.inflateBinding
import com.shannon.android.lib.extended.visibility
import com.shannon.android.lib.util.ThemeUtil
import com.shannon.openvoice.databinding.ItemCurrencyBinding

/**
 *
 * @Package:        com.shannon.openvoice.business.creation.record
 * @ClassName:      CurrencyAdapter
 * @Description:     作用描述
 * @Author:         czhen
 * @CreateDate:     2022/8/10 11:27
 */
class CurrencyAdapter(val context: Context) : BaseAdapter() {


    private val resultList = arrayListOf("ETH")

    private val normalTextColor = ThemeUtil.getColor(context, android.R.attr.textColorPrimary)
    private val selectedTextColor = ThemeUtil.getColor(context, android.R.attr.colorPrimary)

    private var selectedCurrency = resultList[0]

    fun setSelectedCurrency(position: Int): String {
        selectedCurrency = getItem(position)
        notifyDataSetChanged()
        return selectedCurrency
    }

    override fun getCount() = resultList.size

    override fun getItem(position: Int) = resultList[position]

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
            currencyView.text = getItem(position)
            currencyView.setTextColor(if (getItem(position) == selectedCurrency) selectedTextColor else normalTextColor)
            vDividingLine.visibility(position != (count - 1))
        }

        return rootView
    }


    override fun areAllItemsEnabled() = false


    private class CurrencyViewHolder(parent: ViewGroup) {
        val binding = inflateBinding<ItemCurrencyBinding>(parent)
    }
}