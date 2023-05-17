package com.shannon.openvoice.business.status

import android.content.Context
import android.content.Intent
import androidx.fragment.app.commit
import com.shannon.android.lib.base.activity.KBaseActivity
import com.shannon.android.lib.base.viewmodel.EmptyViewModel
import com.shannon.openvoice.R
import com.shannon.openvoice.business.timeline.TimelineFragment
import com.shannon.openvoice.business.timeline.TimelineViewModel
import com.shannon.openvoice.databinding.ActivityStatusListBinding

/**
 *
 * @Package:        com.shannon.openvoice.business.status
 * @ClassName:      StatusListActivity
 * @Description:     动态列表(话题、点赞通用)
 * @Author:         czhen
 * @CreateDate:     2022/8/8 10:48
 */
class StatusListActivity : KBaseActivity<ActivityStatusListBinding, EmptyViewModel>() {

    private lateinit var timelineKind: TimelineViewModel.Kind
    private var hashtag = ""

    private lateinit var mFragment: TimelineFragment

    override fun onInit() {
        timelineKind = TimelineViewModel.Kind.valueOf(intent.getStringExtra(EXTRA_KIND)!!)
        hashtag = intent.getStringExtra(EXTRA_HASHTAG) ?: ""
        val title = when (timelineKind) {
            TimelineViewModel.Kind.FAVOURITES -> getString(R.string.collections)
            TimelineViewModel.Kind.TAG -> getString(R.string.topics)//getString(R.string.title_tag).format(hashtag)
            else -> getString(R.string.app_name)
        }
        setTitleText(title)
        if (supportFragmentManager.findFragmentById(R.id.fragmentContainerView) == null) {
            supportFragmentManager.commit {
                mFragment = if (timelineKind == TimelineViewModel.Kind.TAG) {
                    TimelineFragment.newHashtagInstance(arrayListOf(hashtag))
                } else {
                    TimelineFragment.newInstance(timelineKind)
                }
                replace(R.id.fragmentContainerView, mFragment)
            }
        }
    }

    companion object {

        private const val EXTRA_KIND = "kind"
        private const val EXTRA_HASHTAG = "tag"

        fun newFavouritesIntent(context: Context) =
            Intent(context, StatusListActivity::class.java).apply {
                putExtra(EXTRA_KIND, TimelineViewModel.Kind.FAVOURITES.name)
            }

        fun newHashtagIntent(context: Context, hashtag: String) =
            Intent(context, StatusListActivity::class.java).apply {
                putExtra(EXTRA_KIND, TimelineViewModel.Kind.TAG.name)
                putExtra(EXTRA_HASHTAG, hashtag)
            }
    }
}