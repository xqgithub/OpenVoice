package com.shannon.openvoice.business.main.mine.editprofile

import android.Manifest
import android.content.Intent
import android.os.Build
import android.text.Editable
import android.widget.ImageView
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.core.net.toUri
import androidx.fragment.app.Fragment
import com.bigkoo.pickerview.builder.TimePickerBuilder
import com.bigkoo.pickerview.view.TimePickerView
import com.blankj.utilcode.util.FileUtils
import com.blankj.utilcode.util.TimeUtils
import com.luck.picture.lib.basic.PictureSelector
import com.luck.picture.lib.config.SelectMimeType
import com.luck.picture.lib.interfaces.OnPermissionsInterceptListener
import com.luck.picture.lib.interfaces.OnRequestPermissionListener
import com.permissionx.guolindev.PermissionX
import com.shannon.android.lib.base.activity.KBaseActivity
import com.shannon.android.lib.components.ShapeableButton
import com.shannon.android.lib.extended.*
import com.shannon.android.lib.util.ThemeUtil
import com.shannon.android.lib.util.ToastUtil
import com.shannon.openvoice.FunApplication.Companion.appViewModel
import com.shannon.openvoice.R
import com.shannon.openvoice.business.main.mine.MineViewModel
import com.shannon.openvoice.business.main.mine.account.AccountManager.Companion.accountManager
import com.shannon.openvoice.databinding.ActivityEditprofileBinding
import com.shannon.openvoice.extended.loadAvatar
import com.shannon.openvoice.extended.loadAvatar2
import com.shannon.openvoice.extended.loadUri
import com.shannon.openvoice.util.GlideEngine
import com.shannon.openvoice.util.ImageFileCompressEngine
import com.shannon.openvoice.util.ImageFileCropEngine
import com.shannon.openvoice.util.MaxTextTwoLengthFilter
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import org.greenrobot.eventbus.EventBus
import java.text.SimpleDateFormat

/**
 * Date:2022/8/9
 * Time:9:34
 * author:dimple
 * 我的---编辑个人资料
 */
class EditProfileActivity : KBaseActivity<ActivityEditprofileBinding, MineViewModel>() {


    //生日日期选择器
    private var birthdayTimePicker: TimePickerView? = null

    //生日---年
    private var birthdayYear = -1

    //生日---月
    private var birthdayMonth = -1

    //生日---天
    private var birthdayDay = -1

    private enum class PickType {
        AVATAR,
        HEADER
    }

    companion object {
        const val AVATAR_SIZE = 400F
        const val HEADER_WIDTH = 1500F
        const val HEADER_HEIGHT = 500F
        const val RESULTCODE_PHOTO_HEADER = 0x9999
        const val RESULTCODE_PHOTO_AVATAR = 0x9998
    }

    override fun onInit() {
        binding.apply {
            toolsbar.titleView.text = resources.getString(R.string.edit_data)
            appViewModel.setTopViewHeight(this@EditProfileActivity, clMain, R.id.toolsbar, 5.dp)

            tvSave.setOnClickListener {

//                ToastUtil.showCenter("准备开始保存")
                val avatar = if (!isBlankPlus(viewModel.avatarData.value)) {
                    val avatarBody = FileUtils.getFileByPath(viewModel.avatarData.value!!)
                        .asRequestBody("image/png".toMediaTypeOrNull())
                    MultipartBody.Part.createFormData(
                        "avatar",
                        randomAlphanumericString(12),
                        avatarBody
                    )
                } else {
                    null
                }

                val header = if (!isBlankPlus(viewModel.headerData.value)) {
                    val headerBody = FileUtils.getFileByPath(viewModel.headerData.value!!)
                        .asRequestBody("image/png".toMediaTypeOrNull())
                    MultipartBody.Part.createFormData(
                        "header",
                        randomAlphanumericString(12),
                        headerBody
                    )
                } else {
                    null
                }
                val displayName =
                    if (etNickname.text.toString().trim().isNotEmpty()) etNickname.text.toString()
                        .trim().toRequestBody(MultipartBody.FORM) else null

                val birthday = if (tvBirthdayValue.text.toString().trim()
                        .contains("/")
                ) tvBirthdayValue.text.toString().trim().toRequestBody(MultipartBody.FORM) else null

                val profileInformation =
                    if (etBio.text.toString().trim().isNotEmpty()) etBio.text.toString().trim()
                        .toRequestBody(MultipartBody.FORM) else "".toRequestBody(MultipartBody.FORM)

                viewModel.accountUpdateCredentials(
                    displayName,
                    birthday,
                    profileInformation,
                    avatar,
                    header,
                    this@EditProfileActivity
                )
            }

            sivUserBackgroundImage.apply {
                loadAvatar2(accountManager.getAccountData().header)
                singleClick {
                    pickMedia(PickType.HEADER)
                }
            }
            sivUserAvatar.apply {
                loadAvatar(accountManager.getAccountData().avatar)
                singleClick {
                    pickMedia(PickType.AVATAR)
                }
            }

            etNickname.apply {
                text = Editable.Factory.getInstance()
                    .newEditable(accountManager.getAccountData().name)
                filters = arrayOf(MaxTextTwoLengthFilter(this@EditProfileActivity, 30))
            }

            tvBirthdayValue.text =
                if (isBlankPlus(accountManager.getAccountData().birthday)) "" else TimeUtils.date2String(
                    accountManager.getAccountData().birthday,
                    "yyyy/MM/dd"
                )

            clBirthday.singleClick {
                initCustomTimePicker()
                birthdayTimePicker?.show()
                hideSoftKeyboard(this@EditProfileActivity)
            }

            etBio.apply {
                text = Editable.Factory.getInstance()
                    .newEditable(accountManager.getAccountData().note.parseAsMastodonHtml())
                filters = arrayOf(MaxTextTwoLengthFilter(this@EditProfileActivity, 100))
            }
        }
        dataEcho()
    }

    /**
     * 数据回显
     */
    private fun dataEcho() {
        viewModel.apply {
            headerData.observe(this@EditProfileActivity) {
                binding.sivUserBackgroundImage.loadUri(FileUtils.getFileByPath(it).toUri())
            }
            avatarData.observe(this@EditProfileActivity) {
                binding.sivUserAvatar.loadUri(FileUtils.getFileByPath(it).toUri())
            }
            accountUpdateCredentialsLive.observe(this@EditProfileActivity) {
                accountManager.updateAccount(it)
                EventBus.getDefault().post(it)
                accountManager.getAccountData()
                finish()
            }
        }
    }

    private fun pickMedia(pickType: PickType) {
        val permissions = arrayListOf<String>()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            permissions.addArray(Manifest.permission.READ_MEDIA_IMAGES)
        } else {
            permissions.addArray(Manifest.permission.WRITE_EXTERNAL_STORAGE)
        }
        PermissionX.init(this).permissions(permissions)
            .request { allGranted, _, _ ->
                if (allGranted) {
                    when (pickType) {
                        PickType.AVATAR -> {
                            openGallery(
                                permissions.toArray(emptyArray<String>()),
                                Pair(AVATAR_SIZE, AVATAR_SIZE), RESULTCODE_PHOTO_AVATAR
                            )
                        }
                        PickType.HEADER -> {
                            openGallery(
                                permissions.toArray(emptyArray<String>()),
                                Pair(HEADER_WIDTH, HEADER_HEIGHT), RESULTCODE_PHOTO_HEADER
                            )
                        }
                    }
                }
            }
    }

    private fun openGallery(
        permissions: Array<out String>,
        withAspectRatio: Pair<Float, Float>,
        requestCode: Int
    ) {
        PictureSelector.create(this@EditProfileActivity)
            .openGallery(SelectMimeType.ofImage())
            .setImageEngine(GlideEngine.instance)
            .setMaxSelectNum(1)
            .isPreviewImage(true)
            .isDisplayCamera(false)
            .setCompressEngine(ImageFileCompressEngine())
            .setCropEngine(ImageFileCropEngine(withAspectRatio.first, withAspectRatio.second))
            .setPermissionsInterceptListener(object :
                OnPermissionsInterceptListener {
                override fun requestPermission(
                    fragment: Fragment,
                    permissionArray: Array<out String>,
                    call: OnRequestPermissionListener
                ) {
                    call.onCall(permissions, true)
                }

                override fun hasPermissions(
                    fragment: Fragment?,
                    permissionArray: Array<out String>?
                ): Boolean {
                    return true
                }

            })
            .forResult(requestCode)
    }


    /**
     * activity 回调
     */
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == RESULT_OK && (requestCode == RESULTCODE_PHOTO_HEADER || requestCode == RESULTCODE_PHOTO_AVATAR)) {
            val localMedia = PictureSelector.obtainSelectorList(data)
            if (localMedia != null && localMedia.size > 0) {
//                val picPathUri = FileUtils.getFileByPath(localMedia[0].cutPath).toUri()
                if (requestCode == RESULTCODE_PHOTO_HEADER) {
                    viewModel.headerData.value = localMedia[0].cutPath
                } else {
                    viewModel.avatarData.value = localMedia[0].cutPath
                }
            }
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
                binding.tvBirthdayValue.text.toString().trim()
            ) && binding.tvBirthdayValue.text.toString().trim().contains("/")
        ) {
            binding.tvBirthdayValue.text.toString().trim().apply {
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
        birthdayTimePicker = TimePickerBuilder(this@EditProfileActivity) { date, v ->//选中事件回调
            val _birthday: String = SimpleDateFormat("yyyy-MM-dd").format(date)

            if (_birthday.contains("-")) {
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
                    ToastUtil.showCenter(getString(R.string.age_dissatisfaction))
                    return@TimePickerBuilder
                }
            }

            _birthday.split("-").apply {
                birthdayYear = this[0].toInt()
                birthdayMonth = this[1].toInt()
                birthdayDay = this[2].toInt()
            }
            binding.tvBirthdayValue.text = "${birthdayYear}/${birthdayMonth}/${birthdayDay}"

        }.setDate(selectedDate)
            .setRangDate(startDate, endDate)
            .setLayoutRes(R.layout.view_picker_time) { v ->
                val cl_main: ConstraintLayout = v.findViewById(R.id.cl_main) as ConstraintLayout
                val tv_confirm: TextView = v.findViewById(R.id.sbSure) as ShapeableButton
                val tv_cancel: TextView = v.findViewById(R.id.sbCancel) as ShapeableButton
                val tv_month_day: TextView = v.findViewById(R.id.tv_month_day) as TextView
                val iv_close: ImageView = v.findViewById(R.id.iv_close) as ImageView
                appViewModel.setBottomViewHeight(
                    this@EditProfileActivity,
                    cl_main,
                    R.id.sbSure,
                    20.dp
                )


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
                        this@EditProfileActivity,
                        R.drawable.shape_primary_background3
                    )
                }

//                tv_month_day.text = "${birthdayYear}年${birthdayMonth}月${birthdayDay}日"
//                LogUtils.i(
//                    "birthdayYear =-= ${birthdayYear}",
//                    "birthdayMonth =-= ${birthdayMonth}",
//                    "birthdayDay =-= ${birthdayDay}"
//                )
                tv_month_day.text =
                    "${birthdayYear}/${appViewModel.oneDigitWithZero(birthdayMonth)}/${
                        appViewModel.oneDigitWithZero(
                            birthdayDay
                        )
                    }"

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
            .setBgColor(ContextCompat.getColor(this@EditProfileActivity, R.color.transparent))
            .setDividerColor(
                ContextCompat.getColor(
                    this@EditProfileActivity,
                    R.color.transparent
                )
            )
            .setTextColorCenter(
                ThemeUtil.getColor(
                    this@EditProfileActivity,
                    R.attr.textColorPrimary
                )
            )
            .setTextColorOut(
                ContextCompat.getColor(this@EditProfileActivity, R.color.color_A1A7AF)
            )
            .build()
    }


}