package com.shannon.openvoice.business.compose

import android.content.res.Resources
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.Filter
import android.widget.Filterable
import com.shannon.android.lib.extended.inflateBinding
import com.shannon.android.lib.extended.visibility
import com.shannon.openvoice.R
import com.shannon.openvoice.databinding.ItemAutocompleteAccountBinding
import com.shannon.openvoice.databinding.ItemAutocompleteHashtagBinding
import com.shannon.openvoice.extended.loadAvatar

/**
 *
 * @Package:        com.shannon.openvoice.business.compose
 * @ClassName:      AutoCompleteAdapter
 * @Description:     作用描述
 * @Author:         czhen
 * @CreateDate:     2022/8/10 11:27
 */
class AutoCompleteAdapter(private val autocompletionProvider: AutoCompletionProvider) :
    BaseAdapter(), Filterable {

    companion object {
        private const val DEFAULT_VIEW_TYPE = 0
        private const val ACCOUNT_VIEW_TYPE = 1
        private const val HASHTAG_VIEW_TYPE = 2
    }

    private val resultList = arrayListOf<AutoCompleteResult>()

    override fun getCount() = resultList.size

    override fun getItem(position: Int) = resultList[position]

    override fun getItemId(position: Int) = position.toLong()

    override fun getViewTypeCount() = 3

    override fun getItemViewType(position: Int): Int {
        return when (resultList[position]) {
            is AccountResult -> {
                ACCOUNT_VIEW_TYPE
            }
            is HashtagResult -> {
                HASHTAG_VIEW_TYPE
            }
            else -> {
                DEFAULT_VIEW_TYPE
            }
        }
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        var rootView = convertView
        when (getItemViewType(position)) {
            ACCOUNT_VIEW_TYPE -> {
                val holder: AccountViewHolder?
                if (rootView == null) {
                    holder = AccountViewHolder(parent!!)
                    rootView = holder.binding.root
                    rootView.tag = holder
                } else {
                    holder = rootView.tag as AccountViewHolder
                }
                val context = holder.binding.root.context
                holder.binding.run {
                    val account = (getItem(position) as AccountResult).account
                    avatarView.loadAvatar(account.avatar)
                    username.text =
                        context.getString(R.string.post_username_format, account.username)
                    displayName.text = account.name
                    vDividingLine.visibility(position != resultList.lastIndex)
                }
            }
            HASHTAG_VIEW_TYPE -> {
                val holder: HashtagViewHolder?
                if (rootView == null) {
                    holder = HashtagViewHolder(parent!!)
                    rootView = holder.binding.root
                    rootView.tag = holder
                } else {
                    holder = rootView.tag as HashtagViewHolder
                }
                holder.binding.run {
                    val hashTag = (getItem(position) as HashtagResult).hashTag
                    hashtagView.text = "#".plus(hashTag.name)
                    vDividingLine.visibility(position != resultList.lastIndex)
                }
            }
            else -> {
                throw Resources.NotFoundException("Unknow View Type")
            }
        }

        return rootView
    }

    override fun getFilter(): Filter {
        return AutoCompleteResultFilter(autocompletionProvider) {
            if (it.isNotEmpty()) {
                resultList.clear()
                resultList.addAll(it)
                notifyDataSetChanged()
            } else {
                notifyDataSetInvalidated()
            }
        }
    }

    override fun areAllItemsEnabled() = false

    private class AccountViewHolder(parent: ViewGroup) {
        val binding = inflateBinding<ItemAutocompleteAccountBinding>(parent)
    }

    private class HashtagViewHolder(parent: ViewGroup) {
        val binding = inflateBinding<ItemAutocompleteHashtagBinding>(parent)
    }
}