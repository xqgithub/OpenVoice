package com.shannon.openvoice.business.timeline

import android.content.Context
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.text.TextUtils
import android.view.View
import com.blankj.utilcode.util.ScreenUtils
import com.bumptech.glide.Glide
import com.shannon.android.lib.base.adapter.BaseFunBindingRecyclerViewAdapter
import com.shannon.android.lib.extended.dp
import com.shannon.android.lib.extended.gone
import com.shannon.android.lib.extended.singleClick
import com.shannon.android.lib.extended.visible
import com.shannon.android.lib.util.ThemeUtil
import com.shannon.openvoice.R
import com.shannon.openvoice.business.timeline.listener.StatusActionListener
import com.shannon.openvoice.components.MediaPreviewImageView
import com.shannon.openvoice.databinding.StatusMediaItemBinding
import com.shannon.openvoice.model.Attachment
import com.shannon.openvoice.util.BlurHashDecoder

/**
 *
 * @Package:        com.shannon.openvoice.business.timeline
 * @ClassName:      MediaAdapter
 * @Description:     作用描述
 * @Author:         czhen
 * @CreateDate:     2023/3/2 17:25
 */
class MediaAdapter(val mContext: Context, private val actionListener: StatusActionListener) :
    BaseFunBindingRecyclerViewAdapter<Attachment, StatusMediaItemBinding>() {
    private val screenWidth: Int by lazy { ScreenUtils.getScreenWidth() - 72.dp }
    private val mediaPreviewUnloaded: Drawable
    private var holderPosition: Int = 0

    init {
        mediaPreviewUnloaded =
            ColorDrawable(ThemeUtil.getColor(mContext, R.attr.colorBackgroundAccent))
    }

    fun setHolderPosition(position: Int) {
        this.holderPosition = position
    }

    override fun bindView(binding: StatusMediaItemBinding, attachment: Attachment, position: Int) {
        val lp = binding.statusMediaPreview0.layoutParams
        when (itemCount) {
            1 -> {
                lp.height = 160.dp
            }
            2, 4 -> {
                val height = screenWidth / 2
                lp.height = height
            }
            3 -> {
                val height = screenWidth / 3
                lp.height = height
            }
        }
        loadImage(
            binding.statusMediaPreview0,
            attachment.previewUrl,
            attachment.meta,
            null
        )
        val mediaType = attachment.type
        if ((mediaType == Attachment.Type.VIDEO || mediaType == Attachment.Type.GIFV)) {
            binding.statusMediaOverlay0.visible()
        } else {
            binding.statusMediaOverlay0.gone()
        }
        setAttachmentClickListener(binding.statusMediaPreview0, position, attachment, true)
    }

    private fun loadImage(
        imageView: MediaPreviewImageView,
        previewUrl: String?,
        meta: Attachment.MetaData?,
        blur: String?
    ) {

        val placeholder = if (blur != null) decodeBlurHash(blur) else mediaPreviewUnloaded

        if (TextUtils.isEmpty(previewUrl)) {
            imageView.removeFocalPoint()
            Glide.with(imageView).load(placeholder).centerInside().into(imageView)
        } else {
            val focus = meta?.focus
            if (focus != null) {
                imageView.setFocalPoint(focus)
                Glide.with(imageView).load(previewUrl).placeholder(placeholder).centerInside()
                    .addListener(imageView).into(imageView)
            } else {
                imageView.removeFocalPoint()
                Glide.with(imageView).load(previewUrl).placeholder(placeholder).centerInside()
                    .into(imageView)
            }
        }

    }

    private fun decodeBlurHash(blur: String): BitmapDrawable {
        return BitmapDrawable(mContext.resources, BlurHashDecoder.decode(blur, 32, 32, 1f))
    }

    private fun setAttachmentClickListener(
        imageView: View,
        index: Int,
        attachment: Attachment,
        animateTransition: Boolean
    ) {
        imageView.singleClick {
            actionListener.onViewMedia(
                holderPosition,
                index,
                if (animateTransition) it else null
            )
        }
    }
}