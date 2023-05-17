package com.shannon.openvoice.dialog

import android.Manifest
import android.os.Build
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.luck.picture.lib.basic.PictureSelector
import com.luck.picture.lib.config.SelectMimeType
import com.luck.picture.lib.entity.LocalMedia
import com.luck.picture.lib.interfaces.OnPermissionsInterceptListener
import com.luck.picture.lib.interfaces.OnRequestPermissionListener
import com.luck.picture.lib.interfaces.OnResultCallbackListener
import com.permissionx.guolindev.PermissionX
import com.shannon.android.lib.base.activity.BottomSheetFixedHeightDialog
import com.shannon.android.lib.extended.addArray
import com.shannon.android.lib.extended.inflate
import com.shannon.android.lib.extended.singleClick
import com.shannon.openvoice.R
import com.shannon.openvoice.databinding.DialogChooseMediaBinding
import com.shannon.openvoice.util.GlideEngine
import com.shannon.openvoice.util.ImageFileCompressEngine
import timber.log.Timber
import java.util.Arrays

/**
 *
 * @Package:        com.shannon.openvoice.dialog
 * @ClassName:      MediaChooseDialog
 * @Description:     媒体选择
 * @Author:         czhen
 * @CreateDate:     2022/8/10 16:17
 */
class MediaChooseDialog(
    val mContext: AppCompatActivity,
    private val maxSelectNum: Int,
    private val mediaCallback: OnResultCallbackListener<LocalMedia>
) :
    BottomSheetFixedHeightDialog(mContext, R.style.TransparentBottomDialog) {

    private val binding by inflate<DialogChooseMediaBinding>()

    init {
        setContentView(binding.root)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding.apply {
            cameraView.singleClick(this@MediaChooseDialog::onClick)
            imageView.singleClick(this@MediaChooseDialog::onClick)

            cancelButton.singleClick { dismiss() }
        }
    }


    private fun onClick(v: View) {
        val permissions = arrayListOf<String>()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            permissions.addArray(
                Manifest.permission.CAMERA,
                Manifest.permission.READ_MEDIA_IMAGES,
                Manifest.permission.READ_MEDIA_VIDEO
            )
        } else {
            permissions.addArray(
                Manifest.permission.CAMERA,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            )
        }
        PermissionX.init(mContext).permissions(permissions)
            .request { allGranted, _, deniedList ->
                Timber.d("PermissionX: allGranted = $allGranted ; deniedList = $deniedList")
                if (allGranted) {
                    when (v.id) {
                        R.id.cameraView -> {
                            PictureSelector.create(mContext)
                                .openCamera(SelectMimeType.ofImage())
                                .forResult(mediaCallback)
                        }
                        R.id.imageView -> {
                            PictureSelector.create(mContext)
                                .openGallery(if (maxSelectNum == 4) SelectMimeType.ofAll() else SelectMimeType.ofImage())
                                .setImageEngine(GlideEngine.instance)
                                .setMaxSelectNum(maxSelectNum)
                                .setMaxVideoSelectNum(1)
                                .isWithSelectVideoImage(false)
                                .isPreviewImage(true)
                                .isDisplayCamera(false)
                                .setCompressEngine(ImageFileCompressEngine())
                                .setPermissionsInterceptListener(object :
                                    OnPermissionsInterceptListener {
                                    override fun requestPermission(
                                        fragment: Fragment,
                                        permissionArray: Array<out String>,
                                        call: OnRequestPermissionListener
                                    ) {
                                        call.onCall(
                                            permissions.toArray(emptyArray<String>()),
                                            true
                                        )
                                    }

                                    override fun hasPermissions(
                                        fragment: Fragment?,
                                        permissionArray: Array<out String>?
                                    ): Boolean {
                                        return true
                                    }

                                })
                                .forResult(mediaCallback)
                        }
                    }
                }
            }

        dismiss()
    }


    companion object {
        const val REQUEST_CAMERA_MEDIA = 1001
        const val REQUEST_IMAGE_VIDEO_MEDIA = 1002
    }
}