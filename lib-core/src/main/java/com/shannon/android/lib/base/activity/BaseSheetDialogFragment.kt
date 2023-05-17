package com.shannon.android.lib.base.activity

import android.os.Bundle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.shannon.android.lib.base.viewmodel.BaseViewModel
import com.shannon.android.lib.components.LoadingDialog
import java.lang.reflect.ParameterizedType

/**
 *
 * @ClassName:      BaseSheetDialogFragment
 * @Description:     java类作用描述
 * @Author:         czhen
 */
open class BaseSheetDialogFragment<VM : ViewModel> : BottomSheetDialogFragment() {
    protected lateinit var viewModel: VM
    private val loadingDialog by lazy { LoadingDialog(requireContext()) }

    private var isStarted = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val type = javaClass.genericSuperclass as ParameterizedType
        val clazz1 = type.actualTypeArguments[0] as Class<VM>
        val modelProvider = ViewModelProvider(this)
        viewModel = modelProvider.get(clazz1)

    }

    override fun onStart() {
        super.onStart()
        if (!isStarted) {
            isStarted = true
            bindViewModelEvent()
        }
    }

    /**
     * 展示等待的动画
     */
    open fun showLoading() {
        loadingDialog.run {
            if (!isShowing) show()
        }
    }

    /**
     * 取消等待的动画
     */
    open fun dismissLoading() {
        loadingDialog.run {
            if (isShowing) dismiss()
        }
    }

    private fun bindViewModelEvent() {
        if (viewModel is BaseViewModel) {
            (viewModel as BaseViewModel).apply {
                responseErrorLiveData.observe(viewLifecycleOwner) {
                    handleResponseError(it)
                }
                showLoadingLiveData.observe(viewLifecycleOwner) {
                    this@BaseSheetDialogFragment.showLoading()
                }
                dismissLoadingLiveData.observe(viewLifecycleOwner) {
                    this@BaseSheetDialogFragment.dismissLoading()
                }
            }
        }
    }

    protected open fun handleResponseError(errorCode: Int) {

    }
}