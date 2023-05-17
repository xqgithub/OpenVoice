package com.shannon.openvoice.business.timeline.menu

import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import com.shannon.android.lib.extended.inflateBinding
import com.shannon.android.lib.extended.visibility
import com.shannon.openvoice.databinding.ItemStatusMenuBinding

/**
 *
 * @Package:        com.shannon.openvoice.business.timeline.menu
 * @ClassName:      MenuAdapter
 * @Description:     作用描述
 * @Author:         czhen
 * @CreateDate:     2022/8/18 15:27
 */
class MenuAdapter() : BaseAdapter() {


//    private val menuSelf =
//        arrayListOf(StatusMenuItem.pin(), StatusMenuItem.delete(), StatusMenuItem.republish())
//    private val menuOther =
//        arrayListOf(StatusMenuItem.report(), StatusMenuItem.blackList())
    private var resultList = arrayListOf<StatusMenuItem>()


//    fun isMe(isMe: Boolean) {
//        resultList = if (isMe) menuSelf else menuOther
//        notifyDataSetChanged()
//    }

    fun setData(data:List<StatusMenuItem>){
        resultList.clear()
        resultList.addAll(data)
        notifyDataSetChanged()
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
            iconView.setImageResource(getItem(position).drawableId)
            titleView.text = titleView.context.getString(getItem(position).stringId)
            vDividingLine.visibility(position != (count - 1))
        }

        return rootView
    }


    override fun areAllItemsEnabled() = false


    private class CurrencyViewHolder(parent: ViewGroup) {
        val binding = inflateBinding<ItemStatusMenuBinding>(parent)
    }
}