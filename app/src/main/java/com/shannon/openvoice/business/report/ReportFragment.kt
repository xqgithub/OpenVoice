package com.shannon.openvoice.business.report

import android.view.View
import androidx.core.app.ActivityOptionsCompat
import androidx.core.view.ViewCompat
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.shannon.android.lib.base.activity.KBaseFragment
import com.shannon.android.lib.base.viewmodel.EmptyViewModel
import com.shannon.android.lib.extended.addMoreData
import com.shannon.android.lib.extended.clearAnimations
import com.shannon.android.lib.extended.itemDivider
import com.shannon.android.lib.extended.singleClick
import com.shannon.openvoice.business.main.mine.account.AccountActivity
import com.shannon.openvoice.business.main.viewmedia.ViewMediaActivity
import com.shannon.openvoice.business.status.StatusListActivity
import com.shannon.openvoice.databinding.FragmentReportBinding
import com.shannon.openvoice.model.Attachment
import com.shannon.openvoice.model.AttachmentViewData
import com.shannon.openvoice.model.StatusModel
import com.shannon.openvoice.util.looksLikeMastodonUrl
import com.shannon.openvoice.util.openLink

/**
 *
 * @Package:        com.shannon.openvoice.business.report
 * @ClassName:      ReportFragment
 * @Description:     作用描述
 * @Author:         czhen
 * @CreateDate:     2022/8/23 9:21
 */
class ReportFragment : KBaseFragment<FragmentReportBinding, EmptyViewModel>(), AdapterHandler {

    private val mViewModel by activityViewModels<ReportViewModel>()
    private val mAdapter by lazy { ReportAdapter(requireContext(), this) }

    override fun onInit() {
        binding.run {
            recyclerView.layoutManager = LinearLayoutManager(requireContext())
            recyclerView.adapter = mAdapter
            recyclerView.clearAnimations()
            recyclerView.itemDivider()
            recyclerView.setOnRefreshListener {
                mViewModel.accountStatusesReport(true, this@ReportFragment::observerRefresh)
            }
            recyclerView.setOnLoadMoreListener {
                mViewModel.accountStatusesReport(false, this@ReportFragment::observerLoad)
            }
            continueButton.singleClick(this@ReportFragment::continueClick)
        }
        mViewModel.accountStatusesReport(false, this::observerRefresh)
    }

    private fun observerRefresh(data: List<StatusModel>) {
        val isRefresh = mAdapter.data.isEmpty()
        if (isRefresh) mAdapter.setNewDataIgnoreSize(data) else {
            mAdapter.addData(0, data)
            binding.recyclerView.isRefreshing = false
        }
    }

    private fun observerLoad(data: List<StatusModel>) {
        mAdapter.addMoreData(data)
    }

    private fun continueClick(view: View) {
        val activity = requireActivity()
        if (activity is ReportActivity) {
            activity.replaceCompleteFragment()
        }
    }

    override fun showMedia(v: View?, imageIndex: Int, itemPosition: Int) {
        val status = mAdapter.getItemData(itemPosition).actionableStatus
        when (status.attachments[imageIndex].type) {
            Attachment.Type.GIFV, Attachment.Type.VIDEO, Attachment.Type.IMAGE, Attachment.Type.AUDIO -> {
                val attachments = AttachmentViewData.list(status)
                val intent = ViewMediaActivity.newIntent(context, attachments, imageIndex)
                if (v != null) {
                    val url = status.attachments[imageIndex].url
                    ViewCompat.setTransitionName(v, url)
                    val options = ActivityOptionsCompat.makeSceneTransitionAnimation(
                        requireActivity(),
                        v,
                        url
                    )
                    startActivity(intent, options.toBundle())
                } else {
                    startActivity(intent)
                }
            }
            Attachment.Type.UNKNOWN -> {
            }
        }
    }


    override fun setStatusChecked(itemPosition: Int, isChecked: Boolean) {
        mViewModel.setStatusChecked(mAdapter.getItemData(itemPosition).id, isChecked)
        binding.continueButton.isEnabled = mViewModel.hasChecked()
    }

    override fun isStatusChecked(id: String): Boolean {
        return mViewModel.isStatusChecked(id)
    }

    override fun onViewTag(tag: String) {
        requireContext().startActivity(StatusListActivity.newHashtagIntent(requireContext(), tag))
    }

    override fun onViewAccount(id: String) {
        requireContext().startActivity(AccountActivity.newIntent(requireContext(), id))
    }

    override fun onViewUrl(url: String) {
        if (!looksLikeMastodonUrl(url)) {
            requireContext().openLink(url)
        }
    }
}