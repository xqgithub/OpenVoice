package com.shannon.openvoice.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.net.Uri
import android.widget.ImageView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import com.luck.picture.lib.engine.CropFileEngine
import com.shannon.openvoice.R
import com.yalantis.ucrop.UCrop
import com.yalantis.ucrop.UCropImageEngine

/**
 *
 * @Package:        com.shannon.openvoice.util
 * @ClassName:      ImageFileCropEngine
 * @Description:     作用描述
 * @Author:         czhen
 * @CreateDate:     2023/3/23 15:01
 */
class ImageFileCropEngine(private val withAspectRatioX: Float,private val withAspectRatioY: Float) : CropFileEngine {
    override fun onStartCrop(
        fragment: Fragment,
        srcUri: Uri,
        destinationUri: Uri,
        dataSource: ArrayList<String>,
        requestCode: Int
    ) {
        val options: UCrop.Options = buildOptions(fragment.requireContext(),withAspectRatioX,withAspectRatioY)
        val uCrop = UCrop.of(srcUri, destinationUri, dataSource)
        uCrop.withOptions(options)
        uCrop.setImageEngine(object : UCropImageEngine {
            override fun loadImage(context: Context, url: String, imageView: ImageView) {
                if (!ActivityCompatHelper.assertValidRequest(context)) {
                    return
                }
                Glide.with(context).load(url).override(180, 180).into(imageView)
            }

            override fun loadImage(
                context: Context,
                url: Uri,
                maxWidth: Int,
                maxHeight: Int,
                call: UCropImageEngine.OnCallbackListener<Bitmap>
            ) {
                Glide.with(context).asBitmap().load(url).override(maxWidth, maxHeight)
                    .into(object : CustomTarget<Bitmap?>() {
                        override fun onResourceReady(
                            resource: Bitmap,
                            transition: Transition<in Bitmap?>?
                        ) {
                            call.onCall(resource)
                        }

                        override fun onLoadCleared(placeholder: Drawable?) {
                            call.onCall(null)
                        }
                    })
            }
        })
        uCrop.start(fragment.requireActivity(), fragment, requestCode)
    }


    private fun buildOptions(context: Context,withAspectRatioX: Float, withAspectRatioY: Float): UCrop.Options {
        val options = UCrop.Options()
        options.setHideBottomControls(true)
        options.setFreeStyleCropEnabled(false)
        options.setShowCropFrame(true)
        options.setShowCropGrid(false)
        options.withAspectRatio(withAspectRatioX, withAspectRatioY)
        options.isCropDragSmoothToCenter(false)
        options.setMaxScaleMultiplier(100f)

        options.setStatusBarColor(ContextCompat.getColor(context, R.color.color_393a3e))
        options.setToolbarColor(ContextCompat.getColor(context, R.color.color_393a3e))
        options.setToolbarWidgetColor(ContextCompat.getColor(context, R.color.white))
        return options
    }
}