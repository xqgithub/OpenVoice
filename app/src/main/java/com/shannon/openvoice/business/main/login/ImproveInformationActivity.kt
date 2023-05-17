package com.shannon.openvoice.business.main.login

import android.content.Context
import android.content.Intent
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.core.widget.doAfterTextChanged
import com.bigkoo.pickerview.builder.TimePickerBuilder
import com.bigkoo.pickerview.view.TimePickerView
import com.blankj.utilcode.util.KeyboardUtils
import com.shannon.android.lib.base.activity.KBaseActivity
import com.shannon.android.lib.components.ShapeableButton
import com.shannon.android.lib.extended.*
import com.shannon.android.lib.util.PreferencesUtil
import com.shannon.android.lib.util.ThemeUtil
import com.shannon.android.lib.util.ToastUtil
import com.shannon.openvoice.FunApplication
import com.shannon.openvoice.R
import com.shannon.openvoice.business.main.mine.account.AccountManager
import com.shannon.openvoice.databinding.ActivityImproveInformationBinding
import com.shannon.openvoice.event.UniversalEvent
import com.shannon.openvoice.util.MaxTextTwoLengthFilter
import org.greenrobot.eventbus.EventBus
import timber.log.Timber
import java.text.SimpleDateFormat
import java.util.*

/**
 *
 * @Package:        com.shannon.openvoice.business.main.login
 * @ClassName:      ImproveInformationActivity
 * @Description:     作用描述
 * @Author:         czhen
 * @CreateDate:     2023/2/2 17:38
 */
class ImproveInformationActivity :
    KBaseActivity<ActivityImproveInformationBinding, LoginViewModel>() {

    private lateinit var accessToken: String
    private lateinit var backupAppToken: String

    //生日日期选择器
    private var birthdayTimePicker: TimePickerView? = null

    //生日---年
    private var birthdayYear = -1

    //生日---月
    private var birthdayMonth = -1

    //生日---天
    private var birthdayDay = -1

    override fun onInit() {
        accessToken = intent.getStringExtra(ACCESS_TOKEN) ?: ""
        backupAppToken = intent.getStringExtra(BACKUP_APP_TOKEN) ?: ""

//        setTitleText(R.string.perfect_account)
        binding.apply {
            userNameEdit.filters =
                arrayOf(MaxTextTwoLengthFilter(this@ImproveInformationActivity, 30))
            userNameEdit.doAfterTextChanged { sureButtonEnable() }
            userNameEdit.showSoftInputOnFocus = true
            userNameEdit.onFocusChangeListener =
                View.OnFocusChangeListener { view, hasFocus ->
                    if (!hasFocus) {
                        KeyboardUtils.hideSoftInput(view)
                    }
                }
            sclBirthday.singleClick {
                userNameEdit.clearFocus()
                root.requestFocus()
                initCustomTimePicker()
                birthdayTimePicker?.show()
                hideSoftKeyboard(this@ImproveInformationActivity)
            }
            sureButton.singleClick(this@ImproveInformationActivity::submitInfo)


            tvTitle.text = contentHighlight(
                "${getString(R.string.perfect_account)}",
                arrayOf(getString(R.string.account)),
                arrayOf(
                    ContextCompat.getColor(
                        this@ImproveInformationActivity,
                        R.color.color_7FF7EC
                    )
                )
            )
        }
    }

    private fun sureButtonEnable() {
        binding.apply {
            sureButton.isEnabled =
                userNameEdit.text.toString().trim().isNotEmpty() &&
                        birthdayView.text.contains("/")
        }
    }

    private fun submitInfo(v: View) {
        if (verificationInfo()) {
            val username = binding.userNameEdit.text.toString().trim()
            val birthday = binding.birthdayView.text.toString().trim()
            showLoading()
            viewModel.updateAccountInfo(username, birthday) {
                FunApplication.appViewModel.accountVerifyCredentials(
                    this@ImproveInformationActivity,
                    onErrorAction = {
                        //如果账号校验失败就将Token清除掉，避免用户重启应该后会直接进入到首页
                        AccountManager.accountManager.upDateToken("")
                    }) { bean ->
                    dismissLoading()
                    AccountManager.accountManager.upDateToken(accessToken)
                    AccountManager.accountManager.updateAccount(bean)
                    PreferencesUtil.putString(PreferencesUtil.Constant.APP_TOKEN, backupAppToken)
                    EventBus.getDefault()
                        .post(UniversalEvent(UniversalEvent.loginSuccess, null))
                    finish()
                }
            }
        }
    }

    private fun verificationInfo(): Boolean {
        val userName = binding.userNameEdit.text.toString().trim()
        if (userName.length < 6 || userName.length > 30) {
            ToastUtil.showCenter(getString(R.string.tips_length_exceeded))
            return false
        }

        var isLetter = false
        userName.forEach { _char ->
            if (Character.isLetter(_char)) {
                isLetter = true
            }
        }
        if (!(isLetter)) {
            ToastUtil.showCenter(getString(R.string.tips_length_exceeded))
            return false
        }

        //判断生日不能大于16岁
        val birthday = binding.birthdayView.text.toString().trim()
        if (birthday.contains("/")) {
            val selectedDate = Calendar.getInstance()
            val year = selectedDate.get(Calendar.YEAR)
            val month = selectedDate.get(Calendar.MONTH) + 1
            val day = selectedDate.get(Calendar.DAY_OF_MONTH)

            val currentTimestamp =
                FunApplication.appViewModel.date2TimeStamp("${year}-${month}-${day}")
            val selectTimestamp =
                FunApplication.appViewModel.date2TimeStamp(birthday.replace("/", "-"))
            val ageDifference = timePartsCollection(currentTimestamp - selectTimestamp)
            if (ageDifference["day"]!! < (16 * 365) + 4
            ) {
                ToastUtil.showCenter(getString(R.string.age_mismatch))
                return false
            }
        }

        return true
    }

    /**
     * 初始化时间选择器
     */
    private fun initCustomTimePicker() {
        birthdayTimePicker = null
        //系统当前时间
        val selectedDate = Calendar.getInstance()
        if (!isBlankPlus(
                binding.birthdayView.text.toString().trim()
            ) && binding.birthdayView.text.toString().trim().contains("/")
        ) {
            binding.birthdayView.text.toString().trim().apply {
                this.split("/").apply {
                    birthdayYear = this[0].toInt()
                    birthdayMonth = this[1].toInt()
                    birthdayDay = this[2].toInt()
                }
            }
        } else {
            birthdayYear = selectedDate.get(Calendar.YEAR)
            birthdayMonth = selectedDate.get(Calendar.MONTH) + 1
            birthdayDay = selectedDate.get(Calendar.DAY_OF_MONTH)
        }
        selectedDate.set(birthdayYear, birthdayMonth - 1, birthdayDay)
        val startDate = Calendar.getInstance()
        startDate.set(1900, 0, 1)
        val endDate = Calendar.getInstance()
        endDate.set(
            endDate.get(Calendar.YEAR),
            endDate.get(Calendar.MONTH),
            endDate.get(Calendar.DAY_OF_MONTH)
        )
        //时间选择器 ，自定义布局
        birthdayTimePicker = TimePickerBuilder(context) { date, v ->//选中事件回调
            val format: String = SimpleDateFormat("yyyy-MM-dd").format(date)
            format.split("-").apply {
                birthdayYear = this[0].toInt()
                birthdayMonth = this[1].toInt()
                birthdayDay = this[2].toInt()
            }
            binding.birthdayView.text = "${birthdayYear}/${birthdayMonth}/${birthdayDay}"
            sureButtonEnable()
        }.setDate(selectedDate)
            .setRangDate(startDate, endDate)
            .setLayoutRes(R.layout.view_picker_time) { v ->
                val mainLayout: ConstraintLayout = v.findViewById(R.id.cl_main) as ConstraintLayout
                val tvConfirm: TextView = v.findViewById(R.id.sbSure) as ShapeableButton
                val tvCancel: TextView = v.findViewById(R.id.sbCancel) as ShapeableButton
                val tvMonthDay: TextView = v.findViewById(R.id.tv_month_day) as TextView
                val ivClose: ImageView = v.findViewById(R.id.iv_close) as ImageView
                FunApplication.appViewModel.setBottomViewHeight(
                    this,
                    mainLayout,
                    R.id.sbSure,
                    20.dp
                )


                //设置背景
                setDynamicShapeRectangle(
                    arrayOf(mainLayout),
                    CornerRadiusLeftTop = 16f.dp,
                    CornerRadiusRightTop = 16f.dp,
//                    _bgcolors = arrayOf(
//                        ThemeUtil.getTypedValue(
//                            this@EditProfileActivity,
//                            R.attr.navigationBarBackground
//                        ).resourceId
//                    )
                    bgColor = arrayOf("#1F1F1F")
                )



                tvCancel.apply {
                    background = ContextCompat.getDrawable(
                        context,
                        R.drawable.shape_primary_background3
                    )
                }

                tvMonthDay.text = "${birthdayYear}/${birthdayMonth}/${birthdayDay}"

                tvConfirm.singleClick {
                    birthdayTimePicker!!.returnData()
                    birthdayTimePicker!!.dismiss()
                }
                tvCancel.singleClick {
                    birthdayTimePicker!!.dismiss()
                }
                ivClose.setOnClickListener {
                    birthdayTimePicker!!.dismiss()
                }
            }
            .setContentTextSize(16)
            .setType(booleanArrayOf(true, true, true, false, false, false))
            .setLabel("", "", "", "时", "分", "秒")
            .setLineSpacingMultiplier(20f)
            .setTextXOffset(0, 0, 0, 40, 0, -40)
            .isCenterLabel(false) //是否只显示中间选中项的label文字，false则每项item全部都带有label。
            .setBgColor(ContextCompat.getColor(context, R.color.transparent))
            .setDividerColor(ContextCompat.getColor(context, R.color.transparent))
            .setTextColorCenter(
                ThemeUtil.getColor(
                    context,
                    R.attr.textColorPrimary
                )
            )
            .setTextColorOut(
                ContextCompat.getColor(context, R.color.color_A1A7AF)
            )
            .build()
    }

    companion object {

        private const val ACCESS_TOKEN = "accessToken"
        private const val BACKUP_APP_TOKEN = "backupAppToken"

        fun newIntent(context: Context, accessToken: String, backupAppToken: String) =
            Intent(context, ImproveInformationActivity::class.java).apply {
                putExtra(ACCESS_TOKEN, accessToken)
                putExtra(BACKUP_APP_TOKEN, backupAppToken)

            }
    }
}