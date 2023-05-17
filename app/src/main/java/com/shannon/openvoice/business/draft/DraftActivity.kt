package com.shannon.openvoice.business.draft

import android.content.Context
import android.content.Intent
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import com.shannon.android.lib.base.activity.KBaseActivity
import com.shannon.android.lib.extended.bindEmptyView
import com.shannon.android.lib.extended.dp
import com.shannon.android.lib.extended.itemDivider
import com.shannon.android.lib.extended.showConfirmDialog
import com.shannon.openvoice.R
import com.shannon.openvoice.business.compose.ComposeActivity
import com.shannon.openvoice.databinding.ActivityDraftBinding
import com.shannon.openvoice.db.DraftModel
import com.shannon.openvoice.model.ComposeOptions
import me.jingbin.library.decoration.SpacesItemDecoration

/**
 *
 * @Package:        com.shannon.openvoice.business.draft
 * @ClassName:      DraftActivity
 * @Description:    草稿箱
 * @Author:         czhen
 * @CreateDate:     2022/8/15 9:20
 */
class DraftActivity : KBaseActivity<ActivityDraftBinding, DraftViewModel>() {

    private val draftAdapter = DraftAdapter(this::onItemClick, this::deleteDraft)
    override fun onInit() {
        setTitleText(R.string.drafts)
        binding.run {
            recyclerView.layoutManager = LinearLayoutManager(context)
            recyclerView.adapter = draftAdapter
            recyclerView.itemDivider(headerNoShowSize = 0)
        }
        viewModel.fetchDraftList().observe(this) {
            if (it.isEmpty()) bindEmptyView()
            draftAdapter.setNewDataIgnoreSize(it)
        }
    }

    private fun onItemClick(position: Int) {
        val draft = draftAdapter.data[position]
        startActivity(
            ComposeActivity.newIntentWith(
                context, ComposeOptions(
                    draft.id,
                    draft.content,
                    visibility = draft.visibility,
                    inReplyToId = draft.inReplyToId,
                    replyingStatusAuthor = draft.inReplyAuthor,
                    draftAttachments = draft.attachments,
                    voiceModelId = draft.voiceModelId
                )
            )
        )
    }

    private fun deleteDraft(position: Int) {
        showConfirmDialog(message = getString(R.string.tips_delete_draft)) {
            viewModel.deleteDraft(draftAdapter.data[position].id)
            draftAdapter.removeData(position)
            if (draftAdapter.itemCount == 0) bindEmptyView()
        }
    }

    private fun bindEmptyView() {
        binding.recyclerView.bindEmptyView(
            R.drawable.ic_development, R.string.desc_blank_draft,
            height = ViewGroup.LayoutParams.MATCH_PARENT
        )
    }


    companion object {

        fun newIntent(context: Context) = Intent(context, DraftActivity::class.java)
    }
}