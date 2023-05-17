package com.shannon.openvoice.business.pay

import android.content.Context
import android.content.Intent
import com.shannon.android.lib.base.activity.KBaseActivity
import com.shannon.android.lib.base.viewmodel.EmptyViewModel
import com.shannon.android.lib.extended.singleClick
import com.shannon.android.lib.util.ToastUtil
import com.shannon.openvoice.R
import com.shannon.openvoice.databinding.ActivityWalletListBinding
import com.shannon.openvoice.extended.launchAppStoreDetail

/**
 *
 * @Package:        com.shannon.openvoice.business.pay
 * @ClassName:      WalletListActivity
 * @Description:     作用描述
 * @Author:         czhen
 * @CreateDate:     2022/11/11 11:15
 */
class WalletListActivity : KBaseActivity<ActivityWalletListBinding, EmptyViewModel>() {
    override fun onInit() {
        binding.run {
            setTitleText(R.string.wallet)

            walletItemMetamask.singleClick {
                if (!launchAppStoreDetail("io.metamask")) {
                    ToastUtil.showCenter(getString(R.string.tips_no_wallet))
                }
            }
            walletItemTrust.singleClick {
                if (!launchAppStoreDetail("com.wallet.crypto.trustapp")) {
                    ToastUtil.showCenter(getString(R.string.tips_no_wallet))
                }
            }
            walletItemRainbow.singleClick {
                if (!launchAppStoreDetail("me.rainbow")) {
                    ToastUtil.showCenter(getString(R.string.tips_no_wallet))
                }
            }
        }
    }

    companion object {

        fun newIntent(context: Context) = Intent(context, WalletListActivity::class.java)
    }

}