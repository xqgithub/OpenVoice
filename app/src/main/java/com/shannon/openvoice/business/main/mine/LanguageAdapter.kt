package com.shannon.openvoice.business.main.mine

import android.content.Context
import androidx.core.content.ContextCompat
import com.shannon.android.lib.base.adapter.BaseFunBindingRecyclerViewAdapter
import com.shannon.android.lib.extended.singleClick
import com.shannon.android.lib.util.PreferencesUtil
import com.shannon.openvoice.FunApplication
import com.shannon.openvoice.FunApplication.Companion.appViewModel
import com.shannon.openvoice.R
import com.shannon.openvoice.databinding.AdapterLanguageBinding
import com.shannon.openvoice.model.LanguageBean

/**
 * Date:2022/8/4
 * Time:9:51
 * author:dimple
 * 语言替换适配器
 */
class LanguageAdapter(val mContext: Context, val onClick: (Int) -> Unit) :
    BaseFunBindingRecyclerViewAdapter<LanguageBean, AdapterLanguageBinding>() {

    override fun bindView(binding: AdapterLanguageBinding, bean: LanguageBean, position: Int) {
        binding.apply {
            tvName.apply {
                val currentLanguage =
                    PreferencesUtil.getString(PreferencesUtil.Constant.LANGUAGE, "default")

                text = bean.LanguageEntries
                setTextColor(
                    ContextCompat.getColor(
                        mContext,
                        if (bean.LanguageValues == currentLanguage) R.color.color_E9EBF9 else R.color.color_8C8F8F
                    )
                )
            }
            clMain.singleClick {
                onClick(position)
            }
        }
    }
}