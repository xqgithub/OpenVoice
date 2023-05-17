package com.shannon.openvoice.business.main.mine.aboutus

import android.content.Intent
import android.net.Uri
import androidx.core.content.ContextCompat
import com.blankj.utilcode.util.AppUtils
import com.blankj.utilcode.util.LogUtils
import com.shannon.android.lib.base.activity.KBaseActivity
import com.shannon.android.lib.extended.dp
import com.shannon.android.lib.extended.gone
import com.shannon.android.lib.extended.singleClick
import com.shannon.android.lib.util.ThemeUtil
import com.shannon.openvoice.BuildConfig
import com.shannon.openvoice.FunApplication
import com.shannon.openvoice.R
import com.shannon.openvoice.business.main.mine.MineViewModel
import com.shannon.openvoice.components.ImageAndTextUi
import com.shannon.openvoice.components.TextAndTextUi
import com.shannon.openvoice.databinding.ActivityAboutusBinding


/**
 * Date:2022/8/16
 * Time:16:25
 * author:dimple
 * 关于我们页面
 */
class AboutUsActivity : KBaseActivity<ActivityAboutusBinding, MineViewModel>() {

    override fun onInit() {
        binding.apply {
            toolsbar.apply {
                titleView.text = resources.getString(R.string.about_us)
                FunApplication.appViewModel.setTopViewHeight(
                    this@AboutUsActivity,
                    clMain,
                    R.id.toolsbar,
                    5.dp
                )
                vDividingLine.gone()
            }

            tvVersionName.text = "V ${AppUtils.getAppVersionName()}${BuildConfig.versionBuild}"

            itDiscord.apply {
                setUIFormatType(ImageAndTextUi.ImageAndTextType.top_bottom)
                setAvatarDataFromRes(
                    imgId = R.drawable.ic_opv_aboutus_discord,
                    32.dp,
                    32.dp
                )

                setTextData(
                    content = getString(R.string.wechat),
                    _textSize = 12f,
                    _textColorString = ThemeUtil.getTypedValue(
                        this@AboutUsActivity,
                        android.R.attr.textColorSecondary
                    ).coerceToString().toString()
                )
                changeSivAvatarPosition()

                singleClick {
                    openByBrowser("https://discord.gg/Envd723t")
                }
            }

            itTelegram.apply {
                setUIFormatType(ImageAndTextUi.ImageAndTextType.top_bottom)
                setAvatarDataFromRes(
                    imgId = R.drawable.ic_opv_aboutus_telegram,
                    32.dp,
                    32.dp
                )

                setTextData(
                    content = getString(R.string.business_contact),
                    _textSize = 12f,
                    _textColorString = ThemeUtil.getTypedValue(
                        this@AboutUsActivity,
                        android.R.attr.textColorSecondary
                    ).coerceToString().toString()
                )
                changeSivAvatarPosition()

                singleClick {
                    openByBrowser("https://t.me/+xzZjVLRS_X9iNmE1")
                }
            }

            itTwitter.apply {
                setUIFormatType(ImageAndTextUi.ImageAndTextType.top_bottom)
                setAvatarDataFromRes(
                    imgId = R.drawable.ic_opv_aboutus_twitter,
                    32.dp,
                    32.dp
                )

                setTextData(
                    content = getString(R.string.weibo),
                    _textSize = 12f,
                    _textColorString = ThemeUtil.getTypedValue(
                        this@AboutUsActivity,
                        android.R.attr.textColorSecondary
                    ).coerceToString().toString()
                )
                changeSivAvatarPosition()

                singleClick {
                    openByBrowser("https://twitter.com/openvoiceover")
                }
            }
        }
    }

    /**
     * 浏览器打开
     */
    private fun openByBrowser(url: String) {
//        val intent = Intent()
//        intent.action = Intent.ACTION_VIEW
//        intent.data = Uri.parse(url)
//        if (intent.resolveActivity(packageManager) != null) {
//            val componentName = intent.resolveActivity(packageManager)
//            LogUtils.e("componentName = " + componentName.className)
//            startActivity(Intent.createChooser(intent, "Please select a browser"))
//        }

        val uri = Uri.parse(url)
        val intent = Intent(Intent.ACTION_VIEW, uri)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        startActivity(intent)
    }


}