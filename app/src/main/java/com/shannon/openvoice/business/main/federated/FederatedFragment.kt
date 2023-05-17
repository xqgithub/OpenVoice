package com.shannon.openvoice.business.main.federated

import com.shannon.android.lib.base.activity.KBaseFragment
import com.shannon.android.lib.base.viewmodel.EmptyViewModel
import com.shannon.openvoice.databinding.FragmentFederatedBinding
import timber.log.Timber

/**
 *
 * @ProjectName:    OpenVoice
 * @Package:        com.shannon.openvoice.business.main.federated
 * @ClassName:      FederatedFragment
 * @Description:     推荐
 * @Author:         czhen
 * @CreateDate:     2022/7/25 16:51
 */
class FederatedFragment:KBaseFragment<FragmentFederatedBinding,EmptyViewModel>() {

    companion object {
        fun newInstance(): FederatedFragment {
            return FederatedFragment()
        }
    }

    override fun onInit() {

    }

    override fun onLazyInit() {
        Timber.d("onLazyInit")
    }

    override fun onResume() {
        super.onResume()
        Timber.d("onResume")
    }

    override fun onPause() {
        super.onPause()
        Timber.d("onPause")
    }
}