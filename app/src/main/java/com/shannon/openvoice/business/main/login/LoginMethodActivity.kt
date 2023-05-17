package com.shannon.openvoice.business.main.login

import android.content.Intent
import android.os.Bundle
import android.os.Message
import android.text.method.LinkMovementMethod
import android.view.View
import androidx.core.content.ContextCompat
import androidx.drawerlayout.widget.DrawerLayout
import com.shannon.android.lib.base.activity.KBaseActivity
import com.shannon.android.lib.constant.ConfigConstants
import com.shannon.android.lib.exception.XException
import com.shannon.android.lib.extended.dp
import com.shannon.android.lib.extended.singleClick
import com.shannon.android.lib.util.PreferencesUtil
import com.shannon.android.lib.util.ToastUtil
import com.shannon.android.lib.web3.dapp.DappProxy
import com.shannon.android.lib.web3.dapp.OnCallbackData
import com.shannon.openvoice.FunApplication
import com.shannon.openvoice.R
import com.shannon.openvoice.business.main.enum.ActivityType
import com.shannon.openvoice.business.main.mine.account.AccountManager
import com.shannon.openvoice.business.pay.WalletListActivity
import com.shannon.openvoice.components.ImageAndTextUi
import com.shannon.openvoice.databinding.ActivityLoginMethodBinding
import com.shannon.openvoice.event.UniversalEvent
import com.shannon.openvoice.extended.loadAvatar
import com.shannon.openvoice.extended.startActivityIntercept
import com.shannon.openvoice.model.OAuthToken
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import timber.log.Timber
import java.text.MessageFormat
import java.text.SimpleDateFormat
import java.time.Instant
import java.util.*
import kotlin.math.sign

/**
 *
 * @Package:        com.shannon.openvoice.business.main.login
 * @ClassName:      LoginMethodActivity
 * @Description:     作用描述
 * @Author:         czhen
 * @CreateDate:     2023/1/29 10:03
 */
class LoginMethodActivity : KBaseActivity<ActivityLoginMethodBinding, LoginViewModel>(),
    OnCallbackData {

    private val mDappProxy by lazy { DappProxy.instance }
    private val s = "metamask.github.io wants you to sign in with your Ethereum account:\n" +
            "0xee24ddb5aa5f1dc7cf50498b3d97289095b1cdc4\n" +
            "\n" +
            "I accept the MetaMask Terms of Service: https://community.metamask.io/tos\n" +
            "\n" +
            "URI: https://metamask.github.io\n" +
            "Version: 1\n" +
            "Chain ID: 1\n" +
            "Nonce: 32891757\n" +
            "Issued At: 2021-09-30T16:25:24.000Z"
    private val messageContentPattern =
        "www.openvoice.com wants you to sign in with your Ethereum account:\n{0}\n\nSign in with Ethereum to the app.\n\nURI: https://www.openvoice.com\nVersion: 1\nChain ID: {1}\nNonce: {2}\nIssued At: {3}"
    private var messageContent: String? = null
    private var walletAccount: String? = null
    private var loginNonce: String? = null
    private var authToken: OAuthToken? = null

    private var promptCheck = false
    private var backupAppToken = ""

    override fun onRestart() {
        super.onRestart()
        onInterrupt()
    }

    override fun onInit() {
        mDappProxy.initialize(context, lifecycle, this)
        mDappProxy.removeSessions()
        mDappProxy.openLoginConnect()
        EventBus.getDefault().register(this)
        binding.apply {
            setUserAgreementPrivacyPolicy()
            walletLoginButton.singleClick(this@LoginMethodActivity::walletLogin)
            emailLoginButton.singleClick(this@LoginMethodActivity::emailLogin)
        }
    }

    private fun setUserAgreementPrivacyPolicy() {
        binding.apply {
            promptView.apply {
                setUIFormatType(ImageAndTextUi.ImageAndTextType.left_right)
                setAvatarDataFromRes(
                    imgId = if (promptCheck) R.drawable.ic_opv_signup_on else R.drawable.ic_opv_signup_off,
                    24.dp,
                    24.dp
                )
                singleClick {
                    if (promptCheck) {
                        promptCheck = false
                        setAvatarDataFromRes(imgId = R.drawable.ic_opv_signup_off, 24.dp, 24.dp)
                    } else {
                        promptCheck = true
                        setAvatarDataFromRes(imgId = R.drawable.ic_opv_signup_on, 24.dp, 24.dp)
                    }
                }

                setTextData(
                    _spannableString = viewModel.setUserAgreementPrivacyPolicy(
                        this@LoginMethodActivity,
                        getString(R.string.tips_check_agreement),
                        arrayOf(
                            getString(R.string.user_agreement_2),
                            getString(R.string.privacy_policy_2)
                        ),
                        arrayOf(
                            ContextCompat.getColor(this@LoginMethodActivity, R.color.color_7FF7EC)
                        ),
                        enableClickEvent = true
                    ),
                    _textSize = 12f,
                    _textColorInt =
                    ContextCompat.getColor(this@LoginMethodActivity, R.color.color_B8B8D2)
                )
            }
        }
    }

    private fun emailLogin(view: View) {
        val intent = Intent(context, LoginActivity::class.java)
        intent.putExtras(Bundle().apply {
            putSerializable(
                ConfigConstants.CONSTANT.activityType,
                ActivityType.login
            )
        })
        startActivity(intent)
    }

    private fun walletLogin(view: View) {
        if (!promptCheck) {
            ToastUtil.showToast(getString(R.string.tips_check_agreement))
            return
        }
        if (authToken != null) {
            improveUserInformation(authToken!!.appToken)
        } else if (loginNonce != null) {
            signLoginMessage()
        } else if (walletAccount.isNullOrBlank()) {
            mDappProxy.switchWallet()
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun eventBusLogin(event: UniversalEvent) {
        if (event.actionType == UniversalEvent.loginSuccess) {
            onBackPressed()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        EventBus.getDefault().unregister(this)
        mDappProxy.closeSocket()
    }

    override fun onSessionStateUpdated(account: String?) {
        if (walletAccount.isNullOrEmpty()) {
            account?.apply {
                walletAccount = this
                binding.walletLoginButton.isEnabled = false
                viewModel.nonce {
                    loginNonce = it
                    signLoginMessage()
                }
            }
        }
    }

    /**
     * 签名登录信息
     */
    private fun signLoginMessage() {
        val format = SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS", Locale.getDefault())
        format.timeZone = TimeZone.getTimeZone("UTC")
        messageContent = MessageFormat.format(
            messageContentPattern,
            walletAccount,
            mDappProxy.getWalletChainId(),
            loginNonce,
            format.format(Date()).replace(" ", "T").plus("Z")
        )
        Timber.e(messageContent)
        mDappProxy.sendLoginSignTypedData(messageContent!!, loginNonce!!)
    }

    override fun onSignature(signature: String) {
        viewModel.signIn(messageContent!!, signature, onResult = {
            authToken = it
            login(it.appToken)
        }, onError = {
            if (it is XException && it.errorCode == 403) {
                walletAccount = null
                loginNonce = null
                mDappProxy.closeSocket()
            }
            onInterrupt()
        })
    }

    override fun onInterrupt() {
        binding.walletLoginButton.isEnabled = true
    }

    override fun onSocketState(state: OnCallbackData.SocketState) {
    }

    override fun noWalletFound() {
        startActivity(WalletListActivity.newIntent(context))
    }

    private fun login(appToken: String) {
        backupAppToken = PreferencesUtil.getString(PreferencesUtil.Constant.APP_TOKEN, "")
        PreferencesUtil.putString(PreferencesUtil.Constant.APP_TOKEN, appToken)
        FunApplication.appViewModel.accountVerifyCredentials(
            this@LoginMethodActivity,
            onErrorAction = {
                PreferencesUtil.putString(PreferencesUtil.Constant.APP_TOKEN, backupAppToken)
                //如果账号校验失败就将Token清除掉，避免用户重启应该后会直接进入到首页
                AccountManager.accountManager.upDateToken("")
                onInterrupt()
            }) { bean ->
            if (bean.profileCompleted) {
                //正常登录后需要将oAuth Token恢复，否则用户在注销账号不能再登录
                PreferencesUtil.putString(PreferencesUtil.Constant.APP_TOKEN, backupAppToken)
                AccountManager.accountManager.upDateToken(appToken)
                AccountManager.accountManager.updateAccount(bean)
                EventBus.getDefault()
                    .post(UniversalEvent(UniversalEvent.loginSuccess, null))
            } else {
                improveUserInformation(appToken)
            }
        }
    }

    private fun improveUserInformation(appToken: String) {
        onInterrupt()
        startActivity(ImproveInformationActivity.newIntent(context, appToken, backupAppToken))
    }
}