package com.shannon.openvoice.business.main

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.view.KeyEvent
import android.view.View
import android.view.WindowManager
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import com.blankj.utilcode.util.LogUtils
import com.google.firebase.ktx.Firebase
import com.google.firebase.messaging.RemoteMessage
import com.google.firebase.messaging.ktx.messaging
import com.shannon.android.lib.base.activity.KBaseActivity
import com.shannon.android.lib.base.adapter.SimpleActivityPageAdapter
import com.shannon.android.lib.base.viewmodel.EmptyViewModel
import com.shannon.android.lib.constant.ConfigConstants
import com.shannon.android.lib.constant.ConfigConstants.VARIABLE.isUpgradePopupDisplay
import com.shannon.android.lib.extended.*
import com.shannon.android.lib.util.PreferencesUtil
import com.shannon.android.lib.util.ThemeUtil
import com.shannon.android.lib.util.ToastUtil
import com.shannon.openvoice.FunApplication
import com.shannon.openvoice.FunApplication.Companion.appViewModel
import com.shannon.openvoice.R
import com.shannon.openvoice.business.compose.ComposeActivity
import com.shannon.openvoice.business.creation.CreationFragment
import com.shannon.openvoice.business.creation.record.RecordingNotesActivity
import com.shannon.openvoice.business.main.chat.ConversationFragment
import com.shannon.openvoice.business.main.home.HomeFragment
import com.shannon.openvoice.business.main.mine.account.AccountActivity
import com.shannon.openvoice.business.main.mine.account.AccountManager.Companion.accountManager
import com.shannon.openvoice.business.main.notification.NotificationActivity
import com.shannon.openvoice.business.main.trade.TradeFragment
import com.shannon.openvoice.business.main.web.WebActivity
import com.shannon.openvoice.business.search.SearchActivity
import com.shannon.openvoice.business.timeline.listener.RefreshableFragment
import com.shannon.openvoice.components.MainNavigationView
import com.shannon.openvoice.databinding.ActivityMainBinding
import com.shannon.openvoice.dialog.InvitationCodeDialog
import com.shannon.openvoice.event.UniversalEvent
import com.shannon.openvoice.extended.loadAvatar
import com.shannon.openvoice.extended.startActivityIntercept
import com.shannon.openvoice.model.AccountBean
import com.shannon.openvoice.util.notification.NotificationHelperUtils
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import timber.log.Timber

class MainActivity : KBaseActivity<ActivityMainBinding, EmptyViewModel>(),
    MainNavigationView.OnTabSelectedListener {

    private val fragments = arrayListOf<Fragment>()
    private val mainTitle = arrayListOf(
        R.string.empty_string,
        R.string.empty_string,
        R.string.empty_string,
        R.string.empty_string,
        R.string.Chat
    )

    override fun onInit() {
        setNavigationBarColor(ThemeUtil.getColor(this, R.attr.navigationBarBackground))
        Timber.d("MainActivity init ")

        initDialog()

        EventBus.getDefault().register(this)
        setTitleText(mainTitle[0])
        initFragments()
        binding.run {
            userAvatarView.loadAvatar(accountManager.getAccountData().avatar)
            viewPager.adapter = SimpleActivityPageAdapter(this@MainActivity, fragments)
            viewPager.isUserInputEnabled = false
            viewPager.offscreenPageLimit = fragments.size
            navigationView.setOnTabSelectedListener(this@MainActivity)
            userAvatarView.singleClick {
                startActivityIntercept {
                    appViewModel.accountVerifyCredentials(this@MainActivity) { bean ->
                        accountManager.updateAccount(bean)
                        EventBus.getDefault().post(bean)
                    }
                    startActivity(
                        AccountActivity.newIntent(
                            context,
                            accountManager.getAccountData().id
                        )
                    )

                }
            }
            searchLayout.singleClick {
                startActivityIntercept {
                    startActivity(SearchActivity.newIntent(context))
                }
            }
            createVoiceoverView.singleClick {
                creatorLayout.gone()
                startActivityIntercept {
                    startActivity(ComposeActivity.newIntent(context))
                }
            }
            creatorLayout.singleClick { creatorLayout.gone() }
            createModelView.setOnClickListener {
                creatorLayout.gone()
                startActivityIntercept {
                    if (PreferencesUtil.getBool(
                            PreferencesUtil.Constant.VOICE_MODEL_INVITE_ENABLED,
                            true
                        )
                    ) {
                        InvitationCodeDialog(
                            this@MainActivity,
                            width = 340.dp,
                            height = WindowManager.LayoutParams.WRAP_CONTENT
                        ).apply {
                            toCreation { invitationCode ->
                                appViewModel.verifyInviteCode(
                                    invitationCode,
                                    this@MainActivity
                                ) { isSuccess, inviteCodeBean ->
                                    if (isSuccess) {
                                        startActivity(
                                            RecordingNotesActivity.newIntent(
                                                context,
                                                inviteCodeBean!!.id
                                            )
                                        )
                                        dismissDialog()
                                    }
                                }
                            }
                            show()
                        }
                    } else {
                        startActivity(RecordingNotesActivity.newIntent(context))
                    }
                }
            }

            activityButton.singleClick {
                jump2Activity()
            }

        }


        //google fcm 初始化
        logRegToken()
    }

    private fun initFragments() {
        fragments.addArray(
            HomeFragment.newInstance(),
            TradeFragment.newInstance(),
            Fragment(),
            CreationFragment.newInstance(),
            ConversationFragment.newInstance()
        )
    }

    override fun allowTabSelect(position: Int): Boolean {
        return startActivityIntercept(position == 0 || position == 1)
    }

    override fun onTabSelected(position: Int) {
        Timber.d("onTabSelected --> $position")
        startActivityIntercept(position == 0 || position == 1) {
            if (position == 2) {
                binding.creatorLayout.visibility(!binding.creatorLayout.isVisible)
            } else {
                if (binding.creatorLayout.isVisible) binding.creatorLayout.gone()
                setTitleText(mainTitle[position])
                binding.run {
                    val currentItem = viewPager.currentItem
                    if (currentItem != position) {
                        viewPager.setCurrentItem(position, false)
                        visibilityActivity(
                            PreferencesUtil.getString(
                                PreferencesUtil.Constant.ACTIVITY_URL,
                                ""
                            )
                        )
                        searchLayout.visibility(position != 4)
                    } else {
                        val fragment = fragments[position]
                        if (fragment is RefreshableFragment) {
                            fragment.refreshContent()
                        }
                    }
                }
            }
        }
    }

    override fun onTabUnselected(position: Int) {
        Timber.d("onTabUnselected --> $position")
    }

    override fun onBackPressed() {
        moveTaskToBack(true)
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent): Boolean {
        when (keyCode) {
            KeyEvent.KEYCODE_BACK -> {
                onBackPressed()
                return true
            }
        }
        return super.onKeyDown(keyCode, event)
    }

    override fun onResume() {
        super.onResume()

        appViewModel.apply {

            systemConfiguration(this@MainActivity) {
                visibilityActivity(it.activityUrl)
            }

            isAPPForegroundHeartBeat(true)
        }


        //google fcm 消息接收内容
        intent.extras?.let {
            for (key in it.keySet()) {
                val value = intent.extras?.get(key)
                LogUtils.i("=-=  Key: $key Value: $value")
            }
        }

        //调用接口推送 google fcm token
        appViewModel.reportDeviceToken()

    }

    private fun visibilityActivity(activityUrl: String) {
        binding.activityButton.visibility(
            activityUrl.isNotEmpty() && binding.viewPager.currentItem != 4,
            View.INVISIBLE
        )
    }


    override fun onDestroy() {
        super.onDestroy()
        EventBus.getDefault().unregister(this)
        Timber.e("onDestroy")
    }


//    //公告弹框build
//    private var announcementDialogBuild: WindowWrapper? = null
//
//    //弹框优先级
//    val popup_priority_announcement = 10
//
//
//    //更新弹框build
//    private var updateDialogBuild: WindowWrapper? = null
//
//    //弹框优先级
//    val popup_priority_update = 9

    /**
     * 初始化弹框
     */
    private fun initDialog() {
//        announcementDialogBuild = WindowWrapper.Builder()
//            .priority(popup_priority_announcement)
//            .windowType(WindowType.DIALOG)
//            .window(systemAnnouncementDialog)
//            .setWindowName(systemAnnouncementDialog.className)
//            .setCanShow(true)
//            .build()

        //如果有公告，弹出公告
//        if (PreferencesUtil.getBool(PreferencesUtil.Constant.SYSTEM_ANNOUNCEMENT_ENABLED, false)) {
//            SystemAnnouncementDialog(this@MainActivity, width = 340.dp, height = 418.dp).apply {
//                setBG(R.attr.navigationBarBackground)
//                setAnnouncementContent(
//                    PreferencesUtil.getString(
//                        PreferencesUtil.Constant.SYSTEM_ANNOUNCEMENT,
//                        ""
//                    )
//                )
//                clicksbGoToCreation() {
//                    showUpdateDialog()
//                }
//                show()
//            }
//        } else {
//            showUpdateDialog()
//        }
        showUpdateDialog()
    }

    /**
     * 显示更新弹框
     */

    private fun showUpdateDialog() {
        appViewModel.apply {
            if (isUpgradePopupDisplay) {
                updateVersions(this@MainActivity) {
                    updatePreparation(this@MainActivity, it)
                    isUpgradePopupDisplay = false
                }
            }
        }
    }

    private fun jump2Activity() {
        val activityUrl = PreferencesUtil.getString(
            PreferencesUtil.Constant.ACTIVITY_URL,
            ""
        )//https://www.openvoiceover.com/
        intentToJump(
            this@MainActivity,
            WebActivity::class.java,
            bundle = Bundle().apply {
                putString(ConfigConstants.CONSTANT.webUrl, activityUrl)
            })
    }

    /**  google fcm message  start **/
    /**
     * 检索当前注册令牌
     */
    fun logRegToken() {
        // [START log_reg_token]
        Firebase.messaging.token.addOnCompleteListener { task ->
            if (!task.isSuccessful) {
                LogUtils.i("=-= Fetching FCM registration token failed ${task.exception}")
                return@addOnCompleteListener
            }
            // Get new FCM registration token
            val token = task.result
            // Log and toast
            val msg = "=-= FCM Registration token: $token"
            LogUtils.d(msg)
        }
        // [END log_reg_token]
        askNotificationPermission()
    }


    /**
     * android13 申请google message 通知权限
     */
    private fun askNotificationPermission() {
        // This is only necessary for API level >= 33 (TIRAMISU)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(
                    this@MainActivity,
                    Manifest.permission.POST_NOTIFICATIONS
                ) == PackageManager.PERMISSION_GRANTED
            ) {
                // FCM SDK (and your app) can post notifications.

            } else if (ActivityCompat.shouldShowRequestPermissionRationale(
                    this@MainActivity,
                    Manifest.permission.POST_NOTIFICATIONS
                )
            ) {
                // TODO: display an educational UI explaining to the user the features that will be enabled
                //       by them granting the POST_NOTIFICATION permission. This UI should provide the user
                //       "OK" and "No thanks" buttons. If the user selects "OK," directly request the permission.
                //       If the user selects "No thanks," allow the user to continue without notifications.
            } else {
                // Directly ask for the permission
                registerForActivityResult(
                    ActivityResultContracts.RequestPermission()
                ) { isGranted: Boolean ->
                    if (isGranted) {
                        // FCM SDK (and your app) can post notifications.
                    } else {
                        // TODO: Inform user that that your app will not show notifications.
                        ToastUtil.showCenter("your app will not show notifications")
                    }
                }.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }

    /**  google fcm message  end **/


    @Subscribe(threadMode = ThreadMode.MAIN)
    fun eventBusAccountBean(event: AccountBean) {
        binding.userAvatarView.loadAvatar(event.avatar)
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun eventBusLogin(event: UniversalEvent) {
        if (event.actionType == UniversalEvent.loginSuccess) {
            binding.userAvatarView.loadAvatar(accountManager.getAccountData().avatar)
        } else if (event.actionType == UniversalEvent.signOut) {
            binding.userAvatarView.loadAvatar("")
        } else if (event.actionType == UniversalEvent.sendNotification) {//接收发送通知消息
            val rmNotification = event.message as RemoteMessage.Notification
            if (NotificationHelperUtils.getInstance()
                    .isNotifacationEnabled(FunApplication.getInstance().applicationContext)
            ) {//判断是否有通知栏权限
                NotificationHelperUtils.getInstance().sendNotification2(
                    this@MainActivity,
                    NotificationActivity::class.java,
                    0,
                    rmNotification.title,
                    rmNotification.body
                )
            } else {
                //去打开通知权限
                NotificationHelperUtils.getInstance().openPermission(this@MainActivity)
            }
        }
    }

    companion object {

        fun newIntent(context: Context) = Intent(context, MainActivity::class.java)
    }
}