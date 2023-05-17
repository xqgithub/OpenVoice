package com.shannon.openvoice.business.report

import android.text.InputFilter
import android.view.View
import androidx.fragment.app.activityViewModels
import com.shannon.android.lib.base.activity.KBaseFragment
import com.shannon.android.lib.base.viewmodel.EmptyViewModel
import com.shannon.android.lib.extended.showDialogOnlyConfirmWithIcon
import com.shannon.android.lib.extended.singleClick
import com.shannon.openvoice.R
import com.shannon.openvoice.databinding.FragmentReportCompleteBinding

/**
 *
 * @Package:        com.shannon.openvoice.business.report
 * @ClassName:      ReportCompleteFragment
 * @Description:     作用描述
 * @Author:         czhen
 * @CreateDate:     2022/8/23 13:48
 */
class ReportCompleteFragment : KBaseFragment<FragmentReportCompleteBinding, EmptyViewModel>() {

    private val mViewModel by activityViewModels<ReportViewModel>()

    override fun onInit() {
        binding.run {
            descEdit.filters = arrayOf(InputFilter.LengthFilter(DEFAULT_CHARACTER_LIMIT))
            reportButton.singleClick(this@ReportCompleteFragment::doReport)
        }
    }

    private fun doReport(view: View) {
        val comment = binding.descEdit.text.toString().trim()
        mViewModel.report(comment) {
            requireContext().showDialogOnlyConfirmWithIcon(
                message = getString(R.string.report_users_succ, it),
                R.drawable.icon_post_success,
                getString(R.string.button_report_done)
            ) {
                onBackPressed()
            }
        }
    }

    companion object {
        private const val DEFAULT_CHARACTER_LIMIT = 200
    }
}