package com.shannon.openvoice.business.main.trade

import android.view.View
import android.widget.AdapterView
import android.widget.ListPopupWindow
import androidx.fragment.app.Fragment
import com.shannon.android.lib.base.activity.KBaseFragment
import com.shannon.android.lib.base.adapter.SimpleFragmentPageAdapter
import com.shannon.android.lib.extended.dp
import com.shannon.android.lib.extended.singleClick
import com.shannon.openvoice.R
import com.shannon.openvoice.business.timeline.listener.RefreshableFragment
import com.shannon.openvoice.databinding.FragmentTradeBinding
import com.shannon.openvoice.extended.getDrawableKt
import com.shannon.openvoice.util.tabs.TabLayoutMediatorFun
import timber.log.Timber

/**
 *
 * @ProjectName:    OpenVoice
 * @Package:        com.shannon.openvoice.business.main.trade
 * @ClassName:      TradeFragment
 * @Description:     交易
 * @Author:         czhen
 * @CreateDate:     2022/7/25 16:52
 */
class TradeFragment : KBaseFragment<FragmentTradeBinding, TradeViewModel>(), RefreshableFragment {

    private val timeFilterArray = arrayListOf(
        R.string.all_time,
        R.string.twenty_four,
        R.string.seven_days,
        R.string.fourteen_days,
        R.string.thirty_days
    )
    private val timeAdapter by lazy { TimeAdapter(requireContext(), timeFilterArray) }
    private val listPopupWindow by lazy {
        ListPopupWindow(requireContext(), null).apply {
            setAdapter(timeAdapter)
            anchorView = binding.timeFilterLayout
            verticalOffset = 8.dp
            isModal = true
            setOnItemClickListener(this@TradeFragment::onPopupItemClick)
            setBackgroundDrawable(requireContext().getDrawableKt(R.drawable.shap_trade_time_popup_background))
        }
    }
    private val fragments by lazy { initFragments() }
    private val nameList by lazy { initTitles() }

    companion object {
        fun newInstance(): TradeFragment {
            return TradeFragment()
        }
    }

    override fun onInit() {
        binding.run {
            viewPager.adapter = SimpleFragmentPageAdapter(this@TradeFragment, fragments)
            viewPager.offscreenPageLimit = 2
            TabLayoutMediatorFun(nameList, tabLayout, viewPager)
            timeFilterLayout.singleClick { listPopupWindow.show() }
        }
        viewModel.timeSelectedPosition.observe(viewLifecycleOwner) {
            binding.timeView.text = getString(timeFilterArray[it])
        }
    }

    private fun initFragments(): MutableList<Fragment> {
        return mutableListOf(
            TradeListFragment.newInstance(TradeListFragment.Kind.SELL),
            TradeListFragment.newInstance(TradeListFragment.Kind.USE)
        )
    }

    private fun initTitles(): MutableList<String> {
        return mutableListOf(getString(R.string.tab_sell), getString(R.string.tab_use))
    }

    override fun refreshContent() {
        if (isInitialized())
            (fragments[binding.viewPager.currentItem] as RefreshableFragment).refreshContent()
    }


    private fun onPopupItemClick(parent: AdapterView<*>, view: View, position: Int, id: Long) {
        listPopupWindow.dismiss()
        timeAdapter.setSelected(position)
        viewModel.timeSelectedPosition.postValue(position)
    }

    override fun onLazyInit() {
        Timber.d("onLazyInit")
    }

}