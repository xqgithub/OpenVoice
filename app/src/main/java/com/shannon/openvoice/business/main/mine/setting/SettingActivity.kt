package com.shannon.openvoice.business.main.mine.setting

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.WindowManager
import androidx.core.content.ContentProviderCompat.requireContext
import androidx.core.view.GravityCompat
import com.blankj.utilcode.util.AppUtils
import com.shannon.android.lib.BuildConfig
import com.shannon.android.lib.base.activity.KBaseActivity
import com.shannon.android.lib.constant.ConfigConstants
import com.shannon.android.lib.extended.*
import com.shannon.android.lib.util.*
import com.shannon.openvoice.FunApplication
import com.shannon.openvoice.FunApplication.Companion.appViewModel
import com.shannon.openvoice.R
import com.shannon.openvoice.business.main.MainActivity
import com.shannon.openvoice.business.main.mine.MineViewModel
import com.shannon.openvoice.business.main.mine.aboutus.AboutUsActivity
import com.shannon.openvoice.business.main.mine.editprofile.EditProfileActivity
import com.shannon.openvoice.business.main.web.WebActivity
import com.shannon.openvoice.components.ImageAndTextUi
import com.shannon.openvoice.databinding.ActivitySettingBinding
import com.shannon.openvoice.dialog.LanguageDialog
import me.jessyan.autosize.AutoSizeConfig

/**
 * Date:2022/8/15
 * Time:15:49
 * author:dimple
 */
class SettingActivity : KBaseActivity<ActivitySettingBinding, MineViewModel>() {
    //缓存工具类
    val cacheUtil by lazy { CacheUtil(this) }

    //是否重启MainActivity
    private var restartActivitiesOnExit: Boolean = false

    @SuppressLint("StringFormatInvalid")
    override fun onInit() {
        restartActivitiesOnExit = intent.getBooleanExtra("restartActivitiesOnExit", false)

        binding.apply {
//            toolsbar.apply {
//                titleView.text = resources.getString(R.string.preferences)
//                appViewModel.setTopViewHeight(
//                    this@SettingActivity,
//                    clMain,
//                    R.id.toolsbar,
//                    5.dp
//                )
//                vDividingLine.gone()
//            }
            appViewModel.setTopViewHeight(
                this@SettingActivity,
                clMain,
                R.id.toolsbar,
                5.dp
            )
            tvTitle.text = getString(R.string.preferences)


            ibBack.singleClick {
                if (restartActivitiesOnExit) {
                    val _flag = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
                    intentToJump(
                        this@SettingActivity,
                        MainActivity::class.java,
                        isFinish = true,
                        flag = _flag
                    )
                } else {
                    finish()
                }
            }

            itMyAccount.apply {
                setUIFormatType(ImageAndTextUi.ImageAndTextType.left_right)
                setAvatarDataFromRes(R.drawable.ic_opv_setting_myaccount, 24.dp, 24.dp)
                setTextData(
                    getString(R.string.your_account),
                    16f,
                    _textColorInt = ThemeUtil.getColor(
                        this@SettingActivity,
                        android.R.attr.textColorPrimary
                    )
                )
                changeTvContentPositionToAvatar(12.dp)
                singleClick {
//                    ToastUtil.showCenter("进入到我的账号页面")
                    intentToJump(this@SettingActivity, MyAccountActivity::class.java)
                }
            }

            if (BuildConfig.BUILD_TYPE == ConfigConstants.BuildType.RELEASE)
                languageGroup.gone()

            itLanguage.apply {
                setUIFormatType(ImageAndTextUi.ImageAndTextType.left_right)
                setAvatarDataFromRes(R.drawable.ic_opv_setting_language, 24.dp, 24.dp)
                setTextData(
                    getString(
                        R.string.language,
                        appViewModel.getLanguageEntriesFromValues(this@SettingActivity)
                    ),
                    16f,
                    _textColorInt = ThemeUtil.getColor(
                        this@SettingActivity,
                        android.R.attr.textColorPrimary
                    )
                )
                changeTvContentPositionToAvatar(12.dp)
                singleClick {
                    LanguageDialog(
                        this@SettingActivity,
                        width = WindowManager.LayoutParams.MATCH_PARENT,
                        height = (0.5 * AutoSizeConfig.getInstance().screenHeight).toInt()
                    ).apply {
                        setBG(R.attr.bulletFrameBackground)
                        tvCancelOnClick()
                        show()
                    }
                }
            }

            itClearCache.apply {
                setUIFormatType(ImageAndTextUi.ImageAndTextType.left_right)
                setAvatarDataFromRes(R.drawable.ic_opv_setting_clear_cache, 24.dp, 24.dp)
                setTextData(
                    getString(R.string.clear_cache),
                    16f,
                    _textColorInt = ThemeUtil.getColor(
                        this@SettingActivity,
                        android.R.attr.textColorPrimary
                    )
                )
                changeTvContentPositionToAvatar(12.dp)
                singleClick {
                    showConfirmDialog(
                        message = getString(R.string.tips_clear_cache)
                    ) {
                        cacheUtil.clearAppCache()
                        tvCache.text = cacheUtil.cacheSize
                    }
                }

                tvCache.text = cacheUtil.cacheSize
            }

            itUpdate.apply {
                setUIFormatType(ImageAndTextUi.ImageAndTextType.left_right)
                setAvatarDataFromRes(R.drawable.ic_opv_setting_update, 24.dp, 24.dp)
                setTextData(
                    getString(R.string.check_updates),
                    16f,
                    _textColorInt = ThemeUtil.getColor(
                        this@SettingActivity,
                        android.R.attr.textColorPrimary
                    )
                )
                changeTvContentPositionToAvatar(12.dp)
                singleClick {
//                    showDialogOnlyConfirm(
//                        title = getString(
//                            R.string.tittle_update,
//                            AppUtils.getAppVersionName()
//                        ),
//                        message = "这里的内容根据后台决定",
//                        okText = getString(R.string.live_update),
//                        isCanceledOnTouchOutside = true
//                    ) {
//                        if (!appViewModel.launchAppStoreDetail(
//                                this@SettingActivity,
//                                AppUtils.getAppPackageName()
//                            )
//                        ) {
//                            ToastUtil.showCenter("未安装google商店")
//                        }
//                    }
                    appViewModel.apply {
                        updateVersions(this@SettingActivity) {
                            if (!updatePreparation(this@SettingActivity, it)) {
                                ToastUtil.showCenter(
                                    getString(R.string.tips_no_update),
                                    "#40A4ACAC"
                                )
                            }
                        }
                    }
                }
            }

            itAboutUs.apply {
                setUIFormatType(ImageAndTextUi.ImageAndTextType.left_right)
                setAvatarDataFromRes(R.drawable.ic_opv_setting_aboutus, 24.dp, 24.dp)
                setTextData(
                    getString(R.string.about_us),
                    16f,
                    _textColorInt = ThemeUtil.getColor(
                        this@SettingActivity,
                        android.R.attr.textColorPrimary
                    )
                )
                changeTvContentPositionToAvatar(12.dp)
                singleClick {
                    intentToJump(this@SettingActivity, AboutUsActivity::class.java)
                }
            }

            itUserAgreement.apply {
                setUIFormatType(ImageAndTextUi.ImageAndTextType.left_right)
                setAvatarDataFromRes(R.drawable.ic_opv_setting_user_agreement, 24.dp, 24.dp)
                setTextData(
                    getString(R.string.user_agreement_1),
                    16f,
                    _textColorInt = ThemeUtil.getColor(
                        this@SettingActivity,
                        android.R.attr.textColorPrimary
                    )
                )
                changeTvContentPositionToAvatar(12.dp)
                singleClick {
                    intentToJump(
                        this@SettingActivity,
                        WebActivity::class.java,
                        bundle = Bundle().apply {
                            putString(
                                ConfigConstants.CONSTANT.webUrl,
                                ConfigConstants.CONSTANT.userAgreementValue
                            )
                            putString(
                                ConfigConstants.CONSTANT.webTitleName,
                                getString(R.string.user_agreement_1)
                            )
                        })
                }
            }

            itPrivacyPolicy.apply {
                setUIFormatType(ImageAndTextUi.ImageAndTextType.left_right)
                setAvatarDataFromRes(R.drawable.ic_opv_setting_privacy_policy, 24.dp, 24.dp)
                setTextData(
                    getString(R.string.privacy_policy_1),
                    16f,
                    _textColorInt = ThemeUtil.getColor(
                        this@SettingActivity,
                        android.R.attr.textColorPrimary
                    )
                )
                changeTvContentPositionToAvatar(12.dp)
                singleClick {
                    intentToJump(
                        this@SettingActivity,
                        WebActivity::class.java,
                        bundle = Bundle().apply {
                            putString(
                                ConfigConstants.CONSTANT.webUrl,
                                ConfigConstants.CONSTANT.privacyPolicyValue
                            )
                            putString(
                                ConfigConstants.CONSTANT.webTitleName,
                                getString(R.string.privacy_policy_1)
                            )
                        })
                }
            }
        }
    }

    override fun onBackPressed() {
        if (restartActivitiesOnExit) {
            val _flag = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
            intentToJump(
                this@SettingActivity,
                MainActivity::class.java,
                isFinish = true,
                flag = _flag
            )
        } else {
            finish()
        }
    }

}