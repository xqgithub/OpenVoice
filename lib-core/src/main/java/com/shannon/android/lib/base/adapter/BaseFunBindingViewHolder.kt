package com.shannon.android.lib.base.adapter

import androidx.viewbinding.ViewBinding
import me.jingbin.library.adapter.BaseByViewHolder

/**
 *
 * @ClassName:      BaseFunViewHolder
 * @Description:     java类作用描述
 * @Author:         czhen
 */
abstract class BaseFunBindingViewHolder<T, VB : ViewBinding>(val viewBinding: VB) : BaseByViewHolder<T>(viewBinding.root) {

    override fun onBaseBindView(holder: BaseByViewHolder<T>?, bean: T, position: Int) {
        onBindView(viewBinding, bean, position)
    }

    protected abstract fun onBindView(binding: VB, bean: T, position: Int)
}