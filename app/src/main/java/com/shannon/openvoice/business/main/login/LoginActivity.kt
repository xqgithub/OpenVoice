package com.shannon.openvoice.business.main.login

import android.os.CountDownTimer
import android.text.Editable
import android.text.method.HideReturnsTransformationMethod
import android.text.method.PasswordTransformationMethod
import android.view.Gravity
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.core.widget.doAfterTextChanged
import com.bigkoo.pickerview.builder.TimePickerBuilder
import com.bigkoo.pickerview.view.TimePickerView
import com.shannon.android.lib.base.activity.KBaseActivity
import com.shannon.android.lib.components.ShapeableButton
import com.shannon.android.lib.constant.ConfigConstants
import com.shannon.android.lib.extended.*
import com.shannon.android.lib.util.PreferencesUtil
import com.shannon.android.lib.util.PreferencesUtil.Constant.ALREADY_LOGGED_REGISTERED_MAIL
import com.shannon.android.lib.util.ThemeUtil
import com.shannon.android.lib.util.ToastUtil
import com.shannon.openvoice.FunApplication.Companion.appViewModel
import com.shannon.openvoice.R
import com.shannon.openvoice.business.main.enum.ActivityType
import com.shannon.openvoice.business.main.mine.account.AccountManager.Companion.accountManager
import com.shannon.openvoice.components.ImageAndTextUi
import com.shannon.openvoice.components.TextAndTextUi
import com.shannon.openvoice.databinding.ActivityLoginBinding
import com.shannon.openvoice.event.UniversalEvent
import com.shannon.openvoice.util.MaxTextTwoLengthFilter
import org.greenrobot.eventbus.EventBus
import java.text.SimpleDateFormat

/**
 * Date:2022/8/10
 * Time:17:40
 * author:dimple
 * 登录页面/忘记密码页面
 */
class LoginActivity : KBaseActivity<ActivityLoginBinding, LoginViewModel>() {


    //登录状态的密码的值
    private var login_pwd = ""

    //密码状态是否显示
    private var pwdIsVisible = false

    //确认密码状态是否显示
    private var pwdIsVisible2 = false

    //注册页面提示是否勾选
    private var promptCheck = false

    var activityType = ActivityType.login

    //生日日期选择器
    private var birthdayTimePicker: TimePickerView? = null

    //生日---年
    private var birthdayYear = -1

    //生日---月
    private var birthdayMonth = -1

    //生日---天
    private var birthdayDay = -1

    private var countDownTimerVerificationCode: CountDownTimer? = null


    override fun onInit() {
        activityType =
            intent?.getSerializableExtra(ConfigConstants.CONSTANT.activityType) as ActivityType
        setUiFromActivityType()
        dataEcho()

//        appViewModel.getCurrentLanguage(this)
    }

    /**
     * 设置UI根据activityType
     */
    private fun setUiFromActivityType() {
        binding.apply {
            sbGoToLoginisEnabled()

            navigationButton.visibility(ActivityType.login == activityType || ActivityType.signUp == activityType)

            ivlogo.visibility(activityType == ActivityType.login)

            tvTitle.apply {
                text = when (activityType) {
                    ActivityType.login -> getString(R.string.login)
                    ActivityType.forgotPassword -> {
                        contentHighlight(
                            "${getString(R.string.retrieve_pwd)}",
                            arrayOf(getString(R.string.password)),
                            arrayOf(
                                ContextCompat.getColor(
                                    this@LoginActivity,
                                    R.color.color_7FF7EC
                                )
                            )
                        )
                    }
                    ActivityType.signUp -> {
                        contentHighlight(
                            "${getString(R.string.sign_up)}",
                            arrayOf(getString(R.string.product_name)),
                            arrayOf(
                                ContextCompat.getColor(
                                    this@LoginActivity,
                                    R.color.color_7FF7EC
                                )
                            )
                        )
                    }
                    ActivityType.setPassword -> {
                        contentHighlight(
                            "${getString(R.string.set_pwd)}",
                            arrayOf(getString(R.string.password)),
                            arrayOf(
                                ContextCompat.getColor(
                                    this@LoginActivity,
                                    R.color.color_7FF7EC
                                )
                            )
                        )
                    }
                    ActivityType.changePassword -> {
                        contentHighlight(
                            "${getString(R.string.change_pwd)}",
                            arrayOf(getString(R.string.password)),
                            arrayOf(
                                ContextCompat.getColor(
                                    this@LoginActivity,
                                    R.color.color_7FF7EC
                                )
                            )
                        )
                    }
                }
                visibility(activityType != ActivityType.login)
            }

            when (activityType) {
                ActivityType.login -> {
                    etMail.text = Editable.Factory.getInstance()
                        .newEditable(PreferencesUtil.getString(ALREADY_LOGGED_REGISTERED_MAIL, ""))
                    etPwd.text = Editable.Factory.getInstance()
                        .newEditable(login_pwd)
                    guide2.setGuidelinePercent(0f)

                }
                else -> {
                    guide2.setGuidelinePercent(0.15f)
                }
            }

            sclNickName.apply {
                if (activityType == ActivityType.signUp) this.visible() else this.gone()
            }
            sclMail.apply {
                if (activityType == ActivityType.setPassword || activityType == ActivityType.changePassword) this.gone() else this.visible()
            }

            sclPwd.apply {
                if (activityType == ActivityType.forgotPassword || activityType == ActivityType.signUp) this.gone() else this.visible()
            }
            sclPwdConfirm.apply {
                if (activityType == ActivityType.setPassword || activityType == ActivityType.changePassword) this.visible() else this.gone()
            }
            sclBirthday.apply {
                if (activityType == ActivityType.signUp) this.visible() else this.gone()
                singleClick {
                    initCustomTimePicker()
                    birthdayTimePicker?.show()
                    hideSoftKeyboard(this@LoginActivity)
                }
            }

            tvChangePwdHint.apply {
                visibility(
                    activityType == ActivityType.changePassword ||
                            activityType == ActivityType.signUp ||
                            activityType == ActivityType.setPassword
                )
                if (activityType == ActivityType.changePassword || activityType == ActivityType.setPassword) {
                    text = contentHighlight(
                        getString(R.string.desc_code_email, etMail.text.toString().trim()),
                        arrayOf("${etMail.text.toString().trim()}."),
                        arrayOf(
                            ThemeUtil.getColor(
                                this@LoginActivity,
                                android.R.attr.colorPrimary
                            )
                        )
                    )
                } else if (activityType == ActivityType.signUp) {
                    text = getString(R.string.desc_sign_up)
                }
            }

            sclVerificationCode.apply {
                if (activityType == ActivityType.changePassword || activityType == ActivityType.setPassword) {
                    this.visible()
                    stCountDown.visible()

                    tvChangePwdHint2.apply {
                        visible()
                        text = contentHighlight(
                            "${getString(R.string.no_code2)}${getString(R.string.no_code)}${
                                getString(
                                    R.string.no_code3
                                )
                            }",
                            arrayOf(getString(R.string.no_code2), getString(R.string.no_code3)),
                            arrayOf(
                                ThemeUtil.getColor(
                                    this@LoginActivity,
                                    android.R.attr.colorPrimary
                                )
                            )
                        )
                    }

                    if (!isBlankPlus(countDownTimerVerificationCode)) {
                        countDownTimerVerificationCode?.cancel()
                        countDownTimerVerificationCode = null
                    }
                    countDownTimerVerificationCode = object : CountDownTimer(60 * 1000L, 1000) {
                        override fun onTick(millisUntilFinished: Long) {
                            val time = millisUntilFinished / 1000L
                            stCountDown.text = "${time}s"
                        }

                        override fun onFinish() {
                            stCountDown.text = "0s"
                            stCountDown.invisible()
                            ivVerificationCodeResend.apply {
                                visible()
                                singleClick {
                                    if (activityType == ActivityType.setPassword) {
                                        viewModel.registerSendEmail(
                                            etMail.text.toString().trim(),
                                            this@LoginActivity
                                        ) {
                                            stCountDown.visible()
                                            ivVerificationCodeResend.gone()
                                            countDownTimerVerificationCode?.start()
                                        }
                                    } else {
                                        viewModel.forgotPwdSendEmail(
                                            etMail.text.toString().trim(),
                                            this@LoginActivity
                                        ) {
//                                            ToastUtil.showCenter("准备重新开始发送验证码")
                                            stCountDown.visible()
                                            ivVerificationCodeResend.gone()
                                            countDownTimerVerificationCode?.start()
                                        }
                                    }


                                }
                            }
                        }
                    }
                    countDownTimerVerificationCode?.start()

                } else {
                    this.gone()
                    stCountDown.gone()
                    ivVerificationCodeResend.gone()
                    tvChangePwdHint2.gone()
                }
            }

            etNickName.apply {
//                text = Editable.Factory.getInstance().newEditable("")
                filters = arrayOf(MaxTextTwoLengthFilter(this@LoginActivity, 30))
                doAfterTextChanged {
                    sbGoToLoginisEnabled()
                }
            }

            etMail.apply {
//                text = Editable.Factory.getInstance().newEditable("")
                doAfterTextChanged {
                    sbGoToLoginisEnabled()
                }
            }

            etPwd.apply {
//                text = Editable.Factory.getInstance().newEditable("")
                doAfterTextChanged {
                    when (activityType) {
                        ActivityType.login -> login_pwd = it.toString()
                    }
                    sbGoToLoginisEnabled()
                }
            }

            etPwdConfirm.apply {
//                text = Editable.Factory.getInstance().newEditable("")
                doAfterTextChanged {
                    sbGoToLoginisEnabled()
                }
            }

            etVerificationCode.apply {
                doAfterTextChanged {
                    sbGoToLoginisEnabled()
                }
            }


            ivEye.apply {
                singleClick {
                    if (pwdIsVisible) {
                        //准备隐藏密码
                        pwdIsVisible = false
                        etPwd.transformationMethod = PasswordTransformationMethod.getInstance()
                        setImageDrawable(
                            ContextCompat.getDrawable(
                                this@LoginActivity,
                                R.drawable.ic_opv_pwd_hide
                            )
                        )
                    } else {
                        //准备显示密码
                        pwdIsVisible = true
                        etPwd.transformationMethod = HideReturnsTransformationMethod.getInstance()
                        setImageDrawable(
                            ContextCompat.getDrawable(
                                this@LoginActivity,
                                R.drawable.ic_opv_pwd_show
                            )
                        )
                    }
                }
            }
            ivEye2.apply {
                singleClick {
                    if (pwdIsVisible2) {
                        //准备隐藏密码
                        pwdIsVisible2 = false
                        etPwdConfirm.transformationMethod =
                            PasswordTransformationMethod.getInstance()
                        setImageDrawable(
                            ContextCompat.getDrawable(
                                this@LoginActivity,
                                R.drawable.ic_opv_pwd_hide
                            )
                        )
                    } else {
                        //准备显示密码
                        pwdIsVisible2 = true
                        etPwdConfirm.transformationMethod =
                            HideReturnsTransformationMethod.getInstance()
                        setImageDrawable(
                            ContextCompat.getDrawable(
                                this@LoginActivity,
                                R.drawable.ic_opv_pwd_show
                            )
                        )
                    }
                }
            }



            ttForgotPasswordBackLogin.apply {
                if (activityType == ActivityType.login ||
                    activityType == ActivityType.signUp
                ) this.visible() else this.invisible()

                setUIFormatType(TextAndTextUi.TextAndTextType.left_right)
                setTextOneData(
                    content = resources.getString(
                        if (activityType == ActivityType.login) R.string.forgot_pwd else R.string.hava_account_login
                    ),
                    _textSize = if (activityType == ActivityType.login) 14f else 12f,
                    _textColorInt = ContextCompat.getColor(this@LoginActivity, R.color.color_8C8F8F)
                )

                setTextData(
                    _visibility = if (activityType == ActivityType.login) View.GONE else View.VISIBLE,
                    content = resources.getString(R.string.login),
                    _textSize = 12f,
                    _textColorInt = ContextCompat.getColor(this@LoginActivity, R.color.color_7FF7EC)
                )

                //设置文字背景
                setDynamicShapeRectangle(
                    arrayOf(getTextData()), 1.dp, "#7FF7EC",
                    CornerRadiusRightTop = 32f.dp,
                    CornerRadiusLeftTop = 32f.dp,
                    CornerRadiusLeftBottom = 32f.dp,
                    CornerRadiusRightBottom = 32f.dp,
                    _orientation = null,
                    _bgcolors = arrayOf(
                        R.color.transparent
                    )
                )
                //设置文字的边距
                getTextData().setPadding(8.dp, 1.dp, 8.dp, 1.dp)


                setOnClickListener {
                    when (activityType) {
                        ActivityType.login -> {
                            activityType = ActivityType.forgotPassword
                            etMail.text = Editable.Factory.getInstance()
                                .newEditable("")
                            etPwd.text = Editable.Factory.getInstance().newEditable("")
                            etPwdConfirm.text = Editable.Factory.getInstance().newEditable("")

                            ivEye.apply {
                                pwdIsVisible = false
                                etPwd.transformationMethod =
                                    PasswordTransformationMethod.getInstance()
                                setImageDrawable(
                                    ContextCompat.getDrawable(
                                        this@LoginActivity,
                                        R.drawable.ic_opv_pwd_hide
                                    )
                                )
                            }

                            ivEye2.apply {
                                pwdIsVisible2 = false
                                etPwdConfirm.transformationMethod =
                                    PasswordTransformationMethod.getInstance()
                                setImageDrawable(
                                    ContextCompat.getDrawable(
                                        this@LoginActivity,
                                        R.drawable.ic_opv_pwd_hide
                                    )
                                )
                            }

                            setUiFromActivityType()
                        }
                        ActivityType.signUp -> {
                            activityType = ActivityType.login
                            setUiFromActivityType()
                        }
                    }
                }
            }

            tvPwdRule.apply {
                if (activityType == ActivityType.setPassword) this.visible() else this.gone()
            }

            sbBack.apply {
                if (activityType == ActivityType.forgotPassword ||
                    activityType == ActivityType.setPassword ||
                    activityType == ActivityType.changePassword
                ) this.visible() else this.gone()

                background = ContextCompat.getDrawable(
                    this@LoginActivity,
                    R.drawable.shape_primary_background4
                )

                singleClick {
                    when (activityType) {
                        ActivityType.forgotPassword -> {
                            activityType = ActivityType.login
                            setUiFromActivityType()
                        }
                        ActivityType.setPassword -> {

                            if (!isBlankPlus(countDownTimerVerificationCode)) {
                                countDownTimerVerificationCode?.cancel()
                                countDownTimerVerificationCode = null
                            }

                            activityType = ActivityType.signUp
                            setUiFromActivityType()
                        }
                        ActivityType.changePassword -> {
                            if (!isBlankPlus(countDownTimerVerificationCode)) {
                                countDownTimerVerificationCode?.cancel()
                                countDownTimerVerificationCode = null
                            }
                            activityType = ActivityType.forgotPassword
                            setUiFromActivityType()
                        }
                    }
                }
            }

            sbGoToLogin.apply {
                text = when (activityType) {
                    ActivityType.login -> resources.getString(R.string.login)
                    ActivityType.forgotPassword -> resources.getString(R.string.button_next)
                    ActivityType.signUp -> resources.getString(R.string.button_next)
                    ActivityType.setPassword -> resources.getString(R.string.regist)
                    ActivityType.changePassword -> getString(R.string.sure)
                }
                singleClick {
                    if (!checksbGoToLoginFormat()) return@singleClick
                    when (activityType) {
                        ActivityType.login -> {
                            viewModel.userLogin(
                                etMail.text.toString().trim(),
                                etPwd.text.toString().trim(),
                                this@LoginActivity
                            ) { oAuthToken ->
                                accountManager.upDateToken(oAuthToken.appToken)
                                appViewModel.accountVerifyCredentials(
                                    this@LoginActivity,
                                    onErrorAction = {
                                        //如果账号校验失败就将Token清除掉，避免用户重启应该后会直接进入到首页
                                        accountManager.upDateToken("")
                                    }) { bean ->
                                    accountManager.updateAccount(bean)
                                    PreferencesUtil.putString(
                                        ALREADY_LOGGED_REGISTERED_MAIL,
                                        bean.email
                                    )
                                    EventBus.getDefault()
                                        .post(UniversalEvent(UniversalEvent.loginSuccess, null))
                                    finish()
                                }
                            }
                        }
                        ActivityType.forgotPassword -> {
                            val mail = etMail.text.toString().trim()
                            viewModel.forgotPwdSendEmail(mail, this@LoginActivity) {
                                etPwd.text = Editable.Factory.getInstance().newEditable("")
                                etPwdConfirm.text = Editable.Factory.getInstance().newEditable("")
                                activityType = ActivityType.changePassword
                                setUiFromActivityType()
                            }
                        }
                        ActivityType.signUp -> {
                            val mail = etMail.text.toString().trim()
                            viewModel.registerSendEmail(mail, this@LoginActivity) {
                                etPwd.text = Editable.Factory.getInstance().newEditable("")
                                etPwdConfirm.text = Editable.Factory.getInstance().newEditable("")
                                etVerificationCode.text =
                                    Editable.Factory.getInstance().newEditable("")
                                activityType = ActivityType.setPassword
                                setUiFromActivityType()
                            }
                        }
                        ActivityType.setPassword -> {
                            val username = etNickName.text.toString().trim()
                            val mail = etMail.text.toString().trim()
                            val pwd = etPwd.text.toString().trim()
                            val birthday = tvBirthday.text.toString().trim()
                            val verificationCode = etVerificationCode.text.toString().trim()
                            viewModel.emailRegistration(
                                this@LoginActivity,
                                username,
                                mail,
                                pwd,
                                birthday,
                                promptCheck,
                                verificationCode,
                                this@LoginActivity
                            )
                        }
                        ActivityType.changePassword -> {
                            val verificationCode = etVerificationCode.text.toString().trim()
                            val pwd = etPwd.text.toString().trim()
                            val pwdConfirm = etPwdConfirm.text.toString().trim()
                            viewModel.resetPassword(
                                verificationCode,
                                pwd,
                                pwdConfirm,
                                this@LoginActivity
                            ) { isSuccess ->
                                if (isSuccess) {
                                    showDialogOnlyConfirm(
                                        message = getString(R.string.tips_reset_succ),
                                        isCanceledOnTouchOutside = false
                                    ) {
                                        if (isSuccess) {
                                            etPwd.text =
                                                Editable.Factory.getInstance().newEditable("")
                                            etNickName.text =
                                                Editable.Factory.getInstance().newEditable("")
                                            etMail.text =
                                                Editable.Factory.getInstance().newEditable("")
                                            etPwdConfirm.text =
                                                Editable.Factory.getInstance().newEditable("")
                                            tvBirthday.text = getString(R.string.birthday)
                                            etVerificationCode.text =
                                                Editable.Factory.getInstance().newEditable("")
                                            activityType = ActivityType.login

                                            if (!isBlankPlus(countDownTimerVerificationCode)) {
                                                countDownTimerVerificationCode?.cancel()
                                                countDownTimerVerificationCode = null
                                            }
                                            setUiFromActivityType()
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }


            ttToRegister.apply {
                visibility(activityType == ActivityType.login)
                setUIFormatType(TextAndTextUi.TextAndTextType.left_right)
                setTextOneData(
                    content = resources.getString(R.string.no_account_regist),
                    _textSize = 12f,
                    _textColorInt = ContextCompat.getColor(this@LoginActivity, R.color.color_FDFDFD)
                )
                setTextData(
                    content = resources.getString(R.string.regist),
                    _textSize = 12f,
                    _textColorInt = ContextCompat.getColor(this@LoginActivity, R.color.color_7FF7EC)
                )

                //设置文字背景
                setDynamicShapeRectangle(
                    arrayOf(getTextData()), 1.dp, "#7FF7EC",
                    CornerRadiusRightTop = 32f.dp,
                    CornerRadiusLeftTop = 32f.dp,
                    CornerRadiusLeftBottom = 32f.dp,
                    CornerRadiusRightBottom = 32f.dp,
                    _orientation = null,
                    _bgcolors = arrayOf(
                        R.color.transparent
                    )
                )
                //设置文字的边距
                getTextData().setPadding(8.dp, 1.dp, 8.dp, 1.dp)



                singleClick {
                    when (activityType) {
                        ActivityType.login -> {
                            activityType = ActivityType.signUp


                            etMail.text = Editable.Factory.getInstance()
                                .newEditable("")
                            etPwd.text = Editable.Factory.getInstance().newEditable("")
                            etPwdConfirm.text = Editable.Factory.getInstance().newEditable("")

                            ivEye.apply {
                                pwdIsVisible = false
                                etPwd.transformationMethod =
                                    PasswordTransformationMethod.getInstance()
                                setImageDrawable(
                                    ContextCompat.getDrawable(
                                        this@LoginActivity,
                                        R.drawable.ic_opv_pwd_hide
                                    )
                                )
                            }

                            ivEye2.apply {
                                pwdIsVisible2 = false
                                etPwdConfirm.transformationMethod =
                                    PasswordTransformationMethod.getInstance()
                                setImageDrawable(
                                    ContextCompat.getDrawable(
                                        this@LoginActivity,
                                        R.drawable.ic_opv_pwd_hide
                                    )
                                )
                            }

                            etNickName.text = Editable.Factory.getInstance().newEditable("")
                            tvBirthday.text = getString(R.string.birthday)

                            promptCheck = false

                            setUiFromActivityType()
                        }
                    }
                }
            }

            itPrompt.apply {
                if (activityType == ActivityType.signUp || activityType == ActivityType.login) this.visible() else this.gone()
                setUIFormatType(ImageAndTextUi.ImageAndTextType.left_right)
                setAvatarDataFromRes(
                    imgId = if (promptCheck) R.drawable.ic_opv_signup_on else R.drawable.ic_opv_signup_off,
                    24.dp,
                    24.dp
                )
                setTextData(
                    _spannableString = viewModel.setUserAgreementPrivacyPolicy(
                        this@LoginActivity,
                        getString(R.string.tips_check_agreement),
                        arrayOf(
                            getString(R.string.user_agreement_2),
                            getString(R.string.privacy_policy_2)
                        ),
                        arrayOf(
                            ContextCompat.getColor(this@LoginActivity, R.color.color_7FF7EC)
                        ),
                        enableClickEvent = true
                    ),
                    _textSize = 12f,
                    _textColorInt =
                    ContextCompat.getColor(this@LoginActivity, R.color.color_FDFDFD)
                )
                singleClick {
                    if (promptCheck) {
                        promptCheck = false
                        setAvatarDataFromRes(imgId = R.drawable.ic_opv_signup_off, 24.dp, 24.dp)
                    } else {
                        promptCheck = true
                        setAvatarDataFromRes(imgId = R.drawable.ic_opv_signup_on, 24.dp, 24.dp)
                    }
                    sbGoToLoginisEnabled()
                }
            }
        }
    }

    /**
     * 数据回显
     */
    private fun dataEcho() {

        binding.apply {
            viewModel.emailRegistrationLive.observe(this@LoginActivity) { bean ->
                ToastUtil.showCustomizeToast(
                    R.drawable.ic_opv_toast_success,
                    resources.getString(R.string.tips_succ_application),
                    resources.getString(R.string.tips_email_activation),
                    Gravity.CENTER
                )
                sbGoToLogin.postDelayed({

                    PreferencesUtil.putString(
                        ALREADY_LOGGED_REGISTERED_MAIL,
                        etMail.text.toString().trim()
                    )

                    etPwd.text = Editable.Factory.getInstance().newEditable("")
                    etNickName.text = Editable.Factory.getInstance().newEditable("")
                    etMail.text = Editable.Factory.getInstance().newEditable("")
                    etPwdConfirm.text = Editable.Factory.getInstance().newEditable("")
                    tvBirthday.text = getString(R.string.birthday)
                    activityType = ActivityType.login
                    setUiFromActivityType()
                }, 2000)
            }
        }


    }

    /**
     * 检查登录按钮是否可以点击
     */
    private fun sbGoToLoginisEnabled() {
        binding.apply {
            when (activityType) {
                ActivityType.login -> {
                    sbGoToLogin.isEnabled = etMail.text.toString().trim().isNotEmpty() &&
                            etPwd.text.toString().trim().isNotEmpty() && promptCheck
                }
                ActivityType.signUp -> {
                    sbGoToLogin.isEnabled =
                        etMail.text.toString().trim().isNotEmpty() &&
                                etNickName.text.toString().trim().isNotEmpty() &&
                                tvBirthday.text.contains("/") &&
                                promptCheck
                }
                ActivityType.setPassword -> {
                    sbGoToLogin.isEnabled =
                        etPwd.text.toString().trim().isNotEmpty() &&
                                etPwdConfirm.text.toString().trim().isNotEmpty()
                }
                ActivityType.forgotPassword -> {
                    sbGoToLogin.isEnabled =
                        etMail.text.toString().trim().isNotEmpty()
                }
                ActivityType.changePassword -> {
                    sbGoToLogin.isEnabled =
                        etVerificationCode.text.toString().trim().isNotEmpty() &&
                                etPwd.text.toString().trim().isNotEmpty() &&
                                etPwdConfirm.text.toString().trim().isNotEmpty()
                }
            }
        }
    }

    /**
     * 检查格式是否正确
     */
    private fun checksbGoToLoginFormat(): Boolean {
        binding.apply {
            val _etMail = etMail.text.toString().trim()
//            val mailRegex = "\\w+([-+.]\\w+)*@\\w+([-.]\\w+)*\\.\\w+([-.]\\w+)*".toRegex()
//            val mailRegex = "^[a-zA-Z0-9_-]+@[a-zA-Z0-9_-]+(\\.[a-zA-Z0-9_-]+)+\$".toRegex()
            val mailRegex = "^[A-Z0-9a-z._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,4}\$".toRegex()

            when (activityType) {
                ActivityType.login -> {
                    if (!_etMail.matches(mailRegex)) {
                        ToastUtil.showCenter(getString(R.string.tips_invalid_email), "#40A4ACAC")
                        return false
                    }
                }
                ActivityType.signUp -> {
                    val _etNickName = etNickName.text.toString().trim()
                    if (_etNickName.length < 6 || _etNickName.length > 30) {
                        ToastUtil.showCenter(getString(R.string.tips_length_exceeded), "#40A4ACAC")
                        return false
                    }


                    var _isLetter = false
                    _etNickName.forEach { _char ->
                        if (Character.isLetter(_char)) {
                            _isLetter = true
                        }
                    }
                    if (!(_isLetter)) {
                        ToastUtil.showCenter(getString(R.string.tips_length_exceeded), "#40A4ACAC")
                        return false
                    }


                    if (!_etMail.matches(mailRegex)) {
                        ToastUtil.showCenter(getString(R.string.tips_invalid_email), "#40A4ACAC")
                        return false
                    }


                    //判断生日不能大于16岁
                    val _birthday = tvBirthday.text.toString().trim()
                    if (_birthday.contains("/")) {
                        var selectedDate: java.util.Calendar = java.util.Calendar.getInstance()
                        val _year = selectedDate.get(java.util.Calendar.YEAR)
                        val _month = selectedDate.get(java.util.Calendar.MONTH) + 1
                        val _day = selectedDate.get(java.util.Calendar.DAY_OF_MONTH)

                        val currenttimestamp =
                            appViewModel.date2TimeStamp("${_year}-${_month}-${_day}")
                        val selecttimestamp =
                            appViewModel.date2TimeStamp(_birthday.replace("/", "-"))
                        val ageDifference = timePartsCollection(currenttimestamp - selecttimestamp)
                        if (ageDifference["day"]!! < (16 * 365) + 4
                        ) {
                            ToastUtil.showCenter(getString(R.string.age_mismatch), "#40A4ACAC")
                            return false
                        }
                    }
                }
//                ActivityType.setPassword -> {
//                    val _etPwd = etPwd.text.toString().trim()
//                    val _etPwdConfirm = etPwdConfirm.text.toString().trim()
//                    if (_etPwd.length < 6) {
//                        ToastUtil.showCenter(getString(R.string.pwd_error))
//                        return false
//                    }
//
//                    var _isDigit = false
//                    var _isLetter = false
//                    _etPwd.forEach { _char ->
//                        if (Character.isDigit(_char)) {
//                            _isDigit = true
//                        }
//
//                        if (Character.isLetter(_char)) {
//                            _isLetter = true
//                        }
//                    }
//                    if (!(_isDigit && _isLetter)) {
//                        ToastUtil.showCenter(getString(R.string.pwd_error))
//                        return false
//                    }
//
//                    if (_etPwd != _etPwdConfirm) {
//                        ToastUtil.showCenter(getString(R.string.password_disaccord))
//                        return false
//                    }
//                }
                ActivityType.forgotPassword -> {
                    if (!_etMail.matches(mailRegex)) {
                        ToastUtil.showCenter(getString(R.string.tips_invalid_email), "#40A4ACAC")
                        return false
                    }
                }
                ActivityType.setPassword, ActivityType.changePassword -> {
                    val _etPwd = etPwd.text.toString().trim()
                    val _etPwdConfirm = etPwdConfirm.text.toString().trim()
                    if (_etPwd.length < 6) {
                        ToastUtil.showCenter(getString(R.string.pwd_error), "#40A4ACAC")
                        return false
                    }

                    var _isDigit = false
                    var _isLetter = false
                    _etPwd.forEach { _char ->
                        if (Character.isDigit(_char)) {
                            _isDigit = true
                        }

                        if (Character.isLetter(_char)) {
                            _isLetter = true
                        }
                    }
                    if (!(_isDigit && _isLetter)) {
                        ToastUtil.showCenter(getString(R.string.pwd_error), "#40A4ACAC")
                        return false
                    }

                    if (_etPwd != _etPwdConfirm) {
                        ToastUtil.showCenter(getString(R.string.password_disaccord), "#40A4ACAC")
                        return false
                    }
                }
            }
            return true
        }
    }

    /**
     * 初始化时间选择器
     */
    private fun initCustomTimePicker() {
        birthdayTimePicker = null
        //系统当前时间
        var selectedDate: java.util.Calendar = java.util.Calendar.getInstance()
        if (!isBlankPlus(
                binding.tvBirthday.text.toString().trim()
            ) && binding.tvBirthday.text.toString().trim().contains("/")
        ) {
            binding.tvBirthday.text.toString().trim().apply {
                this.split("/").apply {
                    birthdayYear = this[0].toInt()
                    birthdayMonth = this[1].toInt()
                    birthdayDay = this[2].toInt()
                }
            }
        } else {
            birthdayYear = selectedDate.get(java.util.Calendar.YEAR)
            birthdayMonth = selectedDate.get(java.util.Calendar.MONTH) + 1
            birthdayDay = selectedDate.get(java.util.Calendar.DAY_OF_MONTH)
        }
        selectedDate.set(birthdayYear, birthdayMonth - 1, birthdayDay)
        val startDate = java.util.Calendar.getInstance()
        startDate.set(1900, 0, 1)
        val endDate = java.util.Calendar.getInstance()
        endDate.set(
            endDate.get(java.util.Calendar.YEAR),
            endDate.get(java.util.Calendar.MONTH),
            endDate.get(java.util.Calendar.DAY_OF_MONTH)
        )
        //时间选择器 ，自定义布局
        birthdayTimePicker = TimePickerBuilder(this@LoginActivity) { date, v ->//选中事件回调
            val format: String = SimpleDateFormat("yyyy-MM-dd").format(date)
            format.split("-").apply {
                birthdayYear = this[0].toInt()
                birthdayMonth = this[1].toInt()
                birthdayDay = this[2].toInt()
            }
            binding.tvBirthday.text = "${birthdayYear}/${birthdayMonth}/${birthdayDay}"
            sbGoToLoginisEnabled()
        }.setDate(selectedDate)
            .setRangDate(startDate, endDate)
            .setLayoutRes(R.layout.view_picker_time) { v ->
                val cl_main: ConstraintLayout = v.findViewById(R.id.cl_main) as ConstraintLayout
                val tv_confirm: TextView = v.findViewById(R.id.sbSure) as ShapeableButton
                val tv_cancel: TextView = v.findViewById(R.id.sbCancel) as ShapeableButton
                val tv_month_day: TextView = v.findViewById(R.id.tv_month_day) as TextView
                val iv_close: ImageView = v.findViewById(R.id.iv_close) as ImageView
                appViewModel.setBottomViewHeight(this@LoginActivity, cl_main, R.id.sbSure, 20.dp)


                //设置背景
                setDynamicShapeRectangle(
                    arrayOf(cl_main),
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

                tv_cancel.apply {
                    background = ContextCompat.getDrawable(
                        this@LoginActivity,
                        R.drawable.shape_primary_background3
                    )
                }

                tv_month_day.text = "${birthdayYear}/${birthdayMonth}/${birthdayDay}"

                tv_confirm.singleClick {
                    birthdayTimePicker!!.returnData()
                    birthdayTimePicker!!.dismiss()
                }
                tv_cancel.singleClick {
                    birthdayTimePicker!!.dismiss()
                }
                iv_close.setOnClickListener {
                    birthdayTimePicker!!.dismiss()
                }
            }
            .setContentTextSize(16)
            .setType(booleanArrayOf(true, true, true, false, false, false))
            .setLabel("", "", "", "时", "分", "秒")
            .setLineSpacingMultiplier(20f)
            .setTextXOffset(0, 0, 0, 40, 0, -40)
            .isCenterLabel(false) //是否只显示中间选中项的label文字，false则每项item全部都带有label。
            .setBgColor(ContextCompat.getColor(this@LoginActivity, R.color.transparent))
            .setDividerColor(ContextCompat.getColor(this@LoginActivity, R.color.transparent))
            .setTextColorCenter(
                ThemeUtil.getColor(
                    this@LoginActivity,
                    R.attr.textColorPrimary
                )
            )
            .setTextColorOut(
                ContextCompat.getColor(this@LoginActivity, R.color.color_A1A7AF)
            )
            .build()
    }

}