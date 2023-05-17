package com.shannon.openvoice.business.guide

import android.os.Bundle
import androidx.core.content.ContextCompat
import com.shannon.android.lib.base.activity.KBaseFragment
import com.shannon.openvoice.FunApplication.Companion.appViewModel
import com.shannon.openvoice.R
import com.shannon.openvoice.business.guide.GuideViewModel.Kind.*
import com.shannon.openvoice.databinding.FragmentGuidepagesBinding
import com.shannon.openvoice.event.UniversalEvent
import org.greenrobot.eventbus.EventBus

/**
 * Date:2022/11/1
 * Time:10:32
 * author:dimple
 * 引导页内容
 */
class GuidePagesFragment : KBaseFragment<FragmentGuidepagesBinding, GuideViewModel>() {

    private lateinit var guideKind: GuideViewModel.Kind

    companion object {
        private const val kind_type = "kind_type"


        fun newInstance(kind: GuideViewModel.Kind): GuidePagesFragment {
            val fragment = GuidePagesFragment()
            fragment.arguments = Bundle().apply {
                putString(kind_type, kind.name)
            }
            return fragment
        }
    }


    override fun onInit() {
        guideKind = valueOf(arguments?.getString(kind_type)!!)

        binding.apply {
            appViewModel.setTopViewHeight(requireActivity(), clMain, R.id.ivGuideMap)
//            appViewModel.setBottomViewHeight(requireActivity(), clMain, R.id.vBottomPlaceholder)
            ivGuideMap.apply {
                setImageDrawable(
                    ContextCompat.getDrawable(
                        requireContext(),
                        when (guideKind) {
                            RecommendedPage -> TODO()
                            AuthoringPage -> TODO()
                            RecommendedPageOne -> R.drawable.guide1
                            RecommendedPageTwo -> R.drawable.guide2
                            RecommendedPageThree -> R.drawable.guide3
                            AuthoringPageOne -> R.drawable.guide4
                            AuthoringPageTwo -> R.drawable.guide5
                            AuthoringPageThree -> R.drawable.guide6
                            AuthoringPageFourth -> R.drawable.guide7
                        }
                    )
                )

                setOnClickListener {
                    when (guideKind) {
                        RecommendedPage -> TODO()
                        AuthoringPage -> TODO()
                        RecommendedPageOne -> EventBus.getDefault().post(
                            UniversalEvent(UniversalEvent.guidePageKind, RecommendedPageOne)
                        )
                        RecommendedPageTwo -> EventBus.getDefault().post(
                            UniversalEvent(UniversalEvent.guidePageKind, RecommendedPageTwo)
                        )
                        RecommendedPageThree -> EventBus.getDefault().post(
                            UniversalEvent(UniversalEvent.guidePageKind, RecommendedPageThree)
                        )
                        AuthoringPageOne -> EventBus.getDefault().post(
                            UniversalEvent(UniversalEvent.guidePageKind, AuthoringPageOne)
                        )
                        AuthoringPageTwo -> EventBus.getDefault().post(
                            UniversalEvent(UniversalEvent.guidePageKind, AuthoringPageTwo)
                        )
                        AuthoringPageThree -> EventBus.getDefault().post(
                            UniversalEvent(UniversalEvent.guidePageKind, AuthoringPageThree)
                        )
                        AuthoringPageFourth -> EventBus.getDefault().post(
                            UniversalEvent(UniversalEvent.guidePageKind, AuthoringPageFourth)
                        )
                    }
                }
            }
        }
    }
}