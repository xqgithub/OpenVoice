package com.shannon.openvoice.business.report

import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.text.Spanned
import android.text.TextUtils
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.DrawableRes
import com.bumptech.glide.Glide
import com.shannon.android.lib.base.adapter.BaseFunBindingViewHolder
import com.shannon.android.lib.extended.*
import com.shannon.android.lib.util.ThemeUtil
import com.shannon.openvoice.R
import com.shannon.openvoice.components.MediaPreviewImageView
import com.shannon.openvoice.databinding.ItemMediaPreviewBinding
import com.shannon.openvoice.databinding.ItemReportStatusBinding
import com.shannon.openvoice.databinding.ItemStatusDetailBinding
import com.shannon.openvoice.model.Attachment
import com.shannon.openvoice.model.HashTag
import com.shannon.openvoice.model.StatusModel
import com.shannon.openvoice.util.BlurHashDecoder
import com.shannon.openvoice.util.TimestampUtils
import com.shannon.openvoice.util.setClickableText
import me.jingbin.library.adapter.BaseByViewHolder
import java.util.*

/**
 *
 * @ProjectName:    OpenVoice
 * @Package:        com.shannon.openvoice.business.report
 * @ClassName:      ReportViewHolder
 * @Description:     java类作用描述
 * @Author:         czhen
 * @CreateDate:     2022/8/2 11:37
 */
open class ReportViewHolder(
    val binding: ItemReportStatusBinding,
    private val adapterHandler: AdapterHandler
) :
    BaseFunBindingViewHolder<StatusModel, ItemReportStatusBinding>(
        binding
    ) {

    private val mContext = binding.root.context
    private val mediaPreviewUnloaded: Drawable
    private val mediaLabels = arrayListOf<TextView>()
    private val mediaPreviews = arrayListOf<MediaPreviewImageView>()
    private val mediaOverlays = arrayListOf<ImageView>()

    private val mediaPreviewBinding: ItemMediaPreviewBinding

    init {
        binding.run {
            mediaPreviewBinding = ItemMediaPreviewBinding.bind(root)
            mediaPreviewBinding.run {
                mediaLabels.addArray(
                    statusMediaLabel0,
                    statusMediaLabel1,
                    statusMediaLabel2,
                    statusMediaLabel3
                )
                mediaPreviews.addArray(
                    statusMediaPreview0,
                    statusMediaPreview1,
                    statusMediaPreview2,
                    statusMediaPreview3
                )
                mediaOverlays.addArray(
                    statusMediaOverlay0,
                    statusMediaOverlay1,
                    statusMediaOverlay2,
                    statusMediaOverlay3
                )
            }
        }
        mediaPreviewUnloaded =
            ColorDrawable(ThemeUtil.getColor(mContext, R.attr.colorBackgroundAccent))

        binding.checkbox.setOnCheckedChangeListener { _, isChecked ->
            adapterHandler.setStatusChecked(holderPosition, isChecked)
        }
    }

    override fun onBindView(binding: ItemReportStatusBinding, bean: StatusModel, position: Int) {
    }

    override fun onBaseBindView(
        holder: BaseByViewHolder<StatusModel>?,
        bean: StatusModel,
        position: Int
    ) {
        holderBindView(bean, position)
    }

    private fun holderBindView(bean: StatusModel, position: Int) {
        val actionableStatus = bean.actionableStatus
        binding.run {
            checkbox.isChecked = adapterHandler.isStatusChecked(bean.id)
            setTimestampInfo(actionableStatus.createdAt)
            val attachments = actionableStatus.attachments
            if (hasPreviewAttachment(attachments)) {
                setMediaPreviews(attachments)
                if (attachments.isEmpty()) {
                    hideSensitiveMediaWarning()
                }
                mediaLabels.forEach {
                    it.gone()
                }
            } else {
                setMediaLabel(attachments)
                mediaPreviews[0].visibility = View.GONE
                mediaPreviews[1].visibility = View.GONE
                mediaPreviews[2].visibility = View.GONE
                mediaPreviews[3].visibility = View.GONE
                hideSensitiveMediaWarning()
            }
            setStatusContent(
                actionableStatus.content.parseAsAppHtml(),
                actionableStatus.mentions,
                actionableStatus.tags
            )
        }
    }

    private fun setTimestampInfo(createAt: Date) {
        val then = createAt.time
        val now = System.currentTimeMillis()
        binding.statusTimestampInfo.text =
            TimestampUtils.getRelativeTimeSpanString(mContext, then, now)
    }

    private fun setStatusContent(
        content: Spanned,
        mentions: List<StatusModel.Mention>?,
        tags: List<HashTag>?
    ) {
        setClickableText(binding.statusContent, content, mentions, tags, adapterHandler)
        if (TextUtils.isEmpty(binding.statusContent.text)) {
            binding.statusContent.gone()
        } else {
            binding.statusContent.visible()
        }
    }

    protected fun hasPreviewAttachment(attachments: List<Attachment>): Boolean {
        for (attachment in attachments) {
            if (attachment.type === Attachment.Type.AUDIO || attachment.type === Attachment.Type.UNKNOWN) {
                return false
            }
        }
        return true
    }

    protected fun hideSensitiveMediaWarning() {
        mediaPreviewBinding.statusSensitiveMediaWarning.gone()
        mediaPreviewBinding.statusSensitiveMediaButton.gone()
    }

    protected fun setMediaLabel(attachments: List<Attachment>) {
        for (i in mediaLabels.indices) {
            val mediaLabel = mediaLabels[i]
            if (i < attachments.size) {
                val attachment = attachments[i]
                mediaLabel.visibility = View.VISIBLE

                val drawableResId: Int = getLabelIcon(attachments[0].type)
                mediaLabel.setCompoundDrawablesWithIntrinsicBounds(drawableResId, 0, 0, 0)
                setAttachmentClickListener(mediaLabel, i)
            } else {
                mediaLabel.visibility = View.GONE
            }
        }
    }


    private fun setMediaPreviews(attachments: List<Attachment>) {
        val attachmentSize = Math.min(attachments.size, StatusModel.MAX_MEDIA_ATTACHMENTS)
        val mediaPreviewHeight = 100.dp
        if (attachmentSize <= 2) {
            mediaPreviews[0].layoutParams.height = mediaPreviewHeight * 2
            mediaPreviews[1].layoutParams.height = mediaPreviewHeight * 2
        } else {
            mediaPreviews[0].layoutParams.height = mediaPreviewHeight
            mediaPreviews[1].layoutParams.height = mediaPreviewHeight
            mediaPreviews[2].layoutParams.height = mediaPreviewHeight
            mediaPreviews[3].layoutParams.height = mediaPreviewHeight
        }

        if (attachmentSize > 0) {
            for (i in 0 until attachmentSize) {
                val attachment = attachments[i]
                val imageView = mediaPreviews[i]
                imageView.visible()

                loadImage(
                    imageView,
                    attachment.previewUrl,
                    attachment.meta,
                    null
                )
                val mediaType = attachment.type
                if ((mediaType == Attachment.Type.VIDEO || mediaType == Attachment.Type.GIFV)) {
                    mediaOverlays[i].visible()
                } else {
                    mediaOverlays[i].gone()
                }
                setAttachmentClickListener(imageView, i)
            }
        }

        mediaPreviewBinding.statusSensitiveMediaWarning.gone()
        mediaPreviewBinding.statusSensitiveMediaButton.gone()

        for (i in attachmentSize until StatusModel.MAX_MEDIA_ATTACHMENTS) {
            mediaPreviews[i].gone()
        }
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

    private fun setAttachmentClickListener(imageView: View, index: Int) {
        imageView.singleClick {
            adapterHandler.showMedia(it, index, holderPosition)
        }
    }

    @DrawableRes
    private fun getLabelIcon(type: Attachment.Type): Int {
        return when (type) {
            Attachment.Type.IMAGE -> R.drawable.icon_photo_24dp
            Attachment.Type.GIFV, Attachment.Type.VIDEO -> R.drawable.icon_video_24dp
            Attachment.Type.AUDIO -> R.drawable.icon_music_box_24dp
            else -> R.drawable.icon_attach_file_24dp
        }
    }

    val holderPosition: Int
        get() {
            return if (layoutPosition >= byRecyclerView.customTopItemViewCount) {
                layoutPosition - byRecyclerView.customTopItemViewCount
            } else {
                0
            }
        }

}