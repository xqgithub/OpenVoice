package com.shannon.openvoice.business.report

import android.content.Context
import android.text.Spanned
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.ViewGroup
import com.shannon.android.lib.base.adapter.BaseFunBindingRecyclerViewAdapter
import com.shannon.android.lib.base.adapter.BaseFunBindingViewHolder
import com.shannon.android.lib.extended.gone
import com.shannon.android.lib.extended.visible
import com.shannon.openvoice.databinding.ItemReportStatusBinding
import com.shannon.openvoice.databinding.ItemStatusBinding
import com.shannon.openvoice.model.HashTag
import com.shannon.openvoice.model.StatusModel
import com.shannon.openvoice.util.setClickableText

/**
 *
 * @Package:        com.shannon.openvoice.business.report
 * @ClassName:      ReportAdapter
 * @Description:     作用描述
 * @Author:         czhen
 * @CreateDate:     2022/8/23 9:50
 */
class ReportAdapter(
    private val mContext: Context,
    private val adapterHandler: AdapterHandler
) : BaseFunBindingRecyclerViewAdapter<StatusModel, ItemReportStatusBinding>() {


    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): BaseFunBindingViewHolder<StatusModel, ItemReportStatusBinding> {
        val binding = ItemReportStatusBinding.inflate(LayoutInflater.from(mContext), parent, false)
        return ReportViewHolder(binding, adapterHandler)
    }

    override fun bindView(binding: ItemReportStatusBinding, bean: StatusModel, position: Int) {

    }

}