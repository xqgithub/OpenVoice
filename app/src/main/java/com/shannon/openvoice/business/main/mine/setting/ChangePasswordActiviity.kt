package com.shannon.openvoice.business.main.mine.setting

import android.text.method.HideReturnsTransformationMethod
import android.text.method.PasswordTransformationMethod
import androidx.core.content.ContextCompat
import androidx.core.widget.doAfterTextChanged
import com.shannon.android.lib.base.activity.KBaseActivity
import com.shannon.android.lib.extended.contentHighlight
import com.shannon.android.lib.extended.dp
import com.shannon.android.lib.extended.gone
import com.shannon.android.lib.extended.singleClick
import com.shannon.android.lib.util.ToastUtil
import com.shannon.openvoice.FunApplication
import com.shannon.openvoice.FunApplication.Companion.appViewModel
import com.shannon.openvoice.R
import com.shannon.openvoice.business.main.mine.MineViewModel
import com.shannon.openvoice.databinding.ActivityChangepasswordBinding

/**
 * Date:2022/8/15
 * Time:16:59
 * author:dimple
 * 修改密码页面
 */
class ChangePasswordActiviity : KBaseActivity<ActivityChangepasswordBinding, MineViewModel>() {


    //旧密码状态是否显示
    private var oldPwdIsVisible = false

    //新密码状态是否显示
    private var newPwdIsVisible = false

    //确认新密码状态是否显示
    private var confirmNewPwdIsVisible = false


    override fun onInit() {
        binding.apply {
            toolsbar.apply {
                titleView.text = resources.getString(R.string.change_pwd)
                FunApplication.appViewModel.setTopViewHeight(
                    this@ChangePasswordActiviity,
                    clMain,
                    R.id.toolsbar,
                    5.dp
                )
                vDividingLine.gone()
            }

            tvOldPassword.text =
                contentHighlight(
                    "${getString(R.string.asterisk)}${getString(R.string.old_pwd)}",
                    arrayOf(getString(R.string.asterisk)),
                    arrayOf(
                        ContextCompat.getColor(
                            this@ChangePasswordActiviity,
                            R.color.color_FF947B
                        )
                    )
                )
            tvNewPassword.text = contentHighlight(
                "${getString(R.string.asterisk)}${getString(R.string.newpwd)}",
                arrayOf(getString(R.string.asterisk)),
                arrayOf(ContextCompat.getColor(this@ChangePasswordActiviity, R.color.color_FF947B))
            )
            tvConfirmNewPassword.text = contentHighlight(
                "${getString(R.string.asterisk)}${getString(R.string.desc_verify_newpwd)}",
                arrayOf("${getString(R.string.asterisk)}"),
                arrayOf(ContextCompat.getColor(this@ChangePasswordActiviity, R.color.color_FF947B))
            )

            sbSureisEnabled()

            etOldPassword.apply {
                doAfterTextChanged {
                    sbSureisEnabled()
                }
            }

            etNewPwd.apply {
                doAfterTextChanged {
                    sbSureisEnabled()
                }
            }

            etConfirmNewPassword.apply {
                doAfterTextChanged {
                    sbSureisEnabled()
                }
            }

            ivEye.apply {
                singleClick {
                    if (oldPwdIsVisible) {
                        //准备隐藏密码
                        oldPwdIsVisible = false
                        etOldPassword.transformationMethod =
                            PasswordTransformationMethod.getInstance()
                        setImageDrawable(
                            ContextCompat.getDrawable(
                                this@ChangePasswordActiviity,
                                R.drawable.ic_opv_pwd_hide
                            )
                        )
                    } else {
                        //准备显示密码
                        oldPwdIsVisible = true
                        etOldPassword.transformationMethod =
                            HideReturnsTransformationMethod.getInstance()
                        setImageDrawable(
                            ContextCompat.getDrawable(
                                this@ChangePasswordActiviity,
                                R.drawable.ic_opv_pwd_show
                            )
                        )
                    }
                }
            }

            ivEye2.apply {
                singleClick {
                    if (newPwdIsVisible) {
                        //准备隐藏密码
                        newPwdIsVisible = false
                        etNewPwd.transformationMethod =
                            PasswordTransformationMethod.getInstance()
                        setImageDrawable(
                            ContextCompat.getDrawable(
                                this@ChangePasswordActiviity,
                                R.drawable.ic_opv_pwd_hide
                            )
                        )
                    } else {
                        //准备显示密码
                        newPwdIsVisible = true
                        etNewPwd.transformationMethod =
                            HideReturnsTransformationMethod.getInstance()
                        setImageDrawable(
                            ContextCompat.getDrawable(
                                this@ChangePasswordActiviity,
                                R.drawable.ic_opv_pwd_show
                            )
                        )
                    }
                }
            }

            ivEye3.apply {
                singleClick {
                    if (confirmNewPwdIsVisible) {
                        //准备隐藏密码
                        confirmNewPwdIsVisible = false
                        etConfirmNewPassword.transformationMethod =
                            PasswordTransformationMethod.getInstance()
                        setImageDrawable(
                            ContextCompat.getDrawable(
                                this@ChangePasswordActiviity,
                                R.drawable.ic_opv_pwd_hide
                            )
                        )
                    } else {
                        //准备显示密码
                        confirmNewPwdIsVisible = true
                        etConfirmNewPassword.transformationMethod =
                            HideReturnsTransformationMethod.getInstance()
                        setImageDrawable(
                            ContextCompat.getDrawable(
                                this@ChangePasswordActiviity,
                                R.drawable.ic_opv_pwd_show
                            )
                        )
                    }
                }
            }

            sbSure.singleClick {
                if (checkPasswordFormat()) {
//                    ToastUtil.showCenter("准备去修改密码")
                    val old = etOldPassword.text.toString().trim()
                    val new = etNewPwd.text.toString().trim()
                    val confirmNew = etConfirmNewPassword.text.toString().trim()
                    viewModel.updatePassword(
                        old,
                        new,
                        confirmNew,
                        this@ChangePasswordActiviity
                    ) { isSuccess ->
                        if (isSuccess) {
                            ToastUtil.showCenter(getString(R.string.pwd_modified_succ))
                            appViewModel.jump2MainPage(this@ChangePasswordActiviity)
                            finish()
                        }
                    }
                }
            }
        }
    }

    /**
     * 检查按钮是否可以点击
     */
    private fun sbSureisEnabled() {
        binding.apply {
            sbSure.isEnabled = etOldPassword.text.toString().trim().isNotEmpty() &&
                    etNewPwd.text.toString().trim().isNotEmpty() &&
                    etConfirmNewPassword.text.toString().trim().isNotEmpty()
        }
    }

    /**
     * 校验密码格式
     */
    private fun checkPasswordFormat(): Boolean {
        binding.apply {
            val old = etOldPassword.text.toString().trim()
            val new = etNewPwd.text.toString().trim()
            val confirmNew = etConfirmNewPassword.text.toString().trim()
            //1.判断旧密码是否正确
            //2.判断新密码的格式是否正确
            val regex = "^(?![0-9]+$)(?![a-zA-Z]+$)[0-9A-Za-z]{6,16}$".toRegex()
            if (!new.matches(regex)) {
                ToastUtil.showCenter(resources.getString(R.string.pwd_error))
                return false
            }
            //3.判断新密码和确认新密码是否一致
            if (new != confirmNew) {
                ToastUtil.showCenter(resources.getString(R.string.password_disaccord))
                return false
            }
            return true
        }
    }
}