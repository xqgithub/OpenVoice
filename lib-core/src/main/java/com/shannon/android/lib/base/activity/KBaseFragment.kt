package com.shannon.android.lib.base.activity

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.activity.OnBackPressedCallback
import androidx.activity.OnBackPressedDispatcher
import androidx.annotation.ColorInt
import androidx.annotation.StringRes
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.viewbinding.ViewBinding
import com.gyf.immersionbar.ImmersionBar
import com.shannon.android.lib.R
import com.shannon.android.lib.base.viewmodel.BaseViewModel
import com.shannon.android.lib.components.LoadingDialog
import com.shannon.android.lib.extended.singleClick
import com.shannon.android.lib.util.ThemeUtil
import java.lang.reflect.ParameterizedType


abstract class KBaseFragment<VB : ViewBinding, VM : ViewModel> : Fragment() {
    protected lateinit var binding: VB
    protected lateinit var viewModel: VM
    protected var lazyInit = true
    private val loadingDialog by lazy { LoadingDialog(requireContext()) }
    private lateinit var backPressedDispatcher: OnBackPressedDispatcher
    private lateinit var onBackPressedCallback: OnBackPressedCallback

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val type = javaClass.genericSuperclass as ParameterizedType
        val clazz1 = type.actualTypeArguments[1] as Class<VM>
        val modelProvider = ViewModelProvider(this)
        viewModel = modelProvider.get(clazz1)
        backPressedDispatcher = requireActivity().onBackPressedDispatcher
        onBackPressedCallback = InnerOnBackPressedCallback()
        backPressedDispatcher.addCallback(this, onBackPressedCallback)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val type = javaClass.genericSuperclass as ParameterizedType
        val clazz0 = type.actualTypeArguments[0] as Class<VB>
        val method = clazz0.getMethod("inflate", LayoutInflater::class.java)
        binding = method.invoke(null, inflater) as VB
        bindViewModelEvent(viewModel)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        defaultBindToolbar()
        onInit()
    }

    override fun onResume() {
        super.onResume()
        if(isInitialized()) onShown()
        if (lazyInit) {
            lazyInit = false
            onLazyInit()
        }
    }

    protected fun isInitialized() = !lazyInit

    protected open fun onLazyInit() {}

    abstract fun onInit()

    protected open fun onShown(){}


    protected fun bindViewModelEvent(viewModel: ViewModel) {
        if (viewModel is BaseViewModel) {
            (viewModel as BaseViewModel).apply {
                responseErrorLiveData.observe(viewLifecycleOwner) {
                    handleResponseError(it)
                }
                showLoadingLiveData.observe(viewLifecycleOwner) {
                    this@KBaseFragment.showLoading()
                }
                dismissLoadingLiveData.observe(viewLifecycleOwner) {
                    this@KBaseFragment.dismissLoading()
                }
            }
        }
    }

    protected open fun handleResponseError(errorCode: Int) {

    }

    open fun showLoading(resId: Int = -1) {
        loadingDialog.run {
            show()
            setLoadingMessage(resId)
        }
    }

    open fun dismissLoading() {
        loadingDialog.run {
            if (isShowing) dismiss()
        }
    }

    private fun defaultBindToolbar() {
        val toolbarId = ThemeUtil.getAttrResourceId(requireContext(), R.attr.toolbarId)
        val toolbar = binding.root.findViewById<View>(toolbarId)
        toolbar?.run { bindToolbar(this) }

        val navigationButtonId =
            ThemeUtil.getAttrResourceId(requireContext(), R.attr.navigationButtonId)
        val navigationButton = binding.root.findViewById<View>(navigationButtonId)
        navigationButton?.run { singleClick { doOnBackPressed() } }
    }

    protected fun setTitleText(@StringRes resId: Int) {
        val titleViewId =
            ThemeUtil.getAttrResourceId(requireContext(), R.attr.titleViewId)
        val titleView = binding.root.findViewById<TextView>(titleViewId)
        titleView?.run { setText(resId) }
    }


    protected open fun bindToolbar(titleBar: View) {
        bindToolbar(titleBar, !ThemeUtil.isDarkMode(requireContext()))
    }

    protected open fun bindToolbar(titleBar: View, isDarkFont: Boolean) {
        ImmersionBar.with(this)
            .transparentStatusBar()
            .titleBar(titleBar)
            .statusBarDarkFont(isDarkFont)
            .navigationBarDarkIcon(isDarkFont)
            .init()
    }

    fun setNavigationBarColor(@ColorInt color: Int) {
        val isDarkFont = !ThemeUtil.isDarkMode(requireContext())
        ImmersionBar.with(this)
            .statusBarDarkFont(isDarkFont)
            .navigationBarDarkIcon(isDarkFont)
            .navigationBarColorInt(color)
            .init()
    }

    private fun doOnBackPressed() {
        if (!handleOnBackPressed()) {
            onBackPressed()
        }
    }

    protected fun onBackPressed() {
        onBackPressedCallback.isEnabled = false
        backPressedDispatcher.onBackPressed()
    }

    open fun handleOnBackPressed() = false

    private inner class InnerOnBackPressedCallback : OnBackPressedCallback(true) {
        override fun handleOnBackPressed() {
            doOnBackPressed()
        }
    }

}